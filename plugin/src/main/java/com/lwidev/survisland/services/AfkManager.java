package com.lwidev.survisland.services;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.Shutdownable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AfkManager implements Listener, Shutdownable {

    private final String nameAfkTag = "AFK";
    private final Survisland plugin;
    private final ArrayList<UUID> playersAFK;
    public final HashMap<UUID, Long> lastActivity;
    private BukkitTask afkGlobalTask;

    public AfkManager(Survisland survisland) {
        this.plugin = survisland;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        playersAFK = new ArrayList<>();
        lastActivity = new HashMap<>();

        launchTaskAutoAfk();
    }

    @Override
    public void shutdown() {
        if (afkGlobalTask != null) {
            afkGlobalTask.cancel();
        }
        for (UUID idPlayer : new ArrayList<>(playersAFK)) {
            Player player = Bukkit.getPlayer(idPlayer);
            if (player != null) {
                stopSession(player);
            }
        }
        lastActivity.clear();
        playersAFK.clear();
    }

    /**
     * Évènement du joueur se déconnectant
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        stopSession(player);
        lastActivity.remove(player.getUniqueId());
    }

    /**
     * Stop la mise en Afk d'un joueur
     * @param joueurAfk joueur afk
     */
    private void stopSession(Player joueurAfk) {
        unSetAfkPayers(joueurAfk);
        playersAFK.remove(joueurAfk.getUniqueId());
        MessageUtils.sendInfoMessage(joueurAfk, "Vous n'êtes plus afk.");
    }

    /**
     * Le joueur est actuellement dans la Team Afk
     * On lui enlève la Team afk et on lui remet son ancienne team S'il en avait une
     * @param joueurAfk JoueurAfk
     */
    private void unSetAfkPayers(Player joueurAfk) {
        joueurAfk.removeScoreboardTag(nameAfkTag);
        joueurAfk.playerListName(buildTeamAwareName(joueurAfk, NamedTextColor.WHITE));
    }

    /**
     * Construit le nom du joueur en respectant le prefix/suffix/couleur de sa team, si elle en a une.
     * @param joueurAfk joueur concerné
     * @param fallbackColor couleur utilisée si la team n'a pas de couleur définie
     */
    private Component buildTeamAwareName(Player joueurAfk, NamedTextColor fallbackColor) {
        Team team = joueurAfk.getScoreboard().getEntryTeam(joueurAfk.getName());
        if (team == null) {
            return Component.text(joueurAfk.getName());
        }
        return Component.empty()
                .append(team.prefix())
                .append(Component.text(joueurAfk.getName()).color(team.hasColor() ? team.color() : fallbackColor))
                .append(team.suffix());
    }

    /**
     * Fonction principale qui fait les vérifications puis passe le joueur en afk.
     * Le retour d'afk est détecté par le {@link #onMove} global, pas besoin d'un listener dédié.
     * @param joueurAfk Joueur afk
     */
    public void startAfk(Player joueurAfk) {
        UUID idJoueur = joueurAfk.getUniqueId();
        if(playersAFK.contains(idJoueur)) {
            MessageUtils.sendMessage(joueurAfk,"Vous êtes déjà afk.");
            return;
        }

        playersAFK.add(idJoueur);
        setPlayerAFK(joueurAfk);
        MessageUtils.sendSuccessMessage(joueurAfk, "Vous êtes maintenant afk.");
    }

    /**
     * Recalcule le tab list du joueur selon son état afk actuel et sa team actuelle.
     * À appeler après tout changement affectant sa team (ajout/retrait/suppression) pour qu'il
     * ne reste pas figé sur l'ancien prefix/suffix/couleur tant que le joueur ne se
     * reconnecte pas ou ne bascule pas afk.
     * @param player joueur en ligne concerné
     */
    public void refreshDisplayName(Player player) {
        if (playersAFK.contains(player.getUniqueId())) {
            setPlayerAFK(player);
        } else {
            unSetAfkPayers(player);
        }
    }

    /**
     * Bascule l'état afk d'un joueur, à l'initiative d'un OP (ex : {@code /afk <joueur>}).
     * @param joueurAfk joueur ciblé
     * @return {@code true} si le joueur est afk après l'appel, {@code false} sinon
     */
    public boolean toggleAfk(Player joueurAfk) {
        boolean etaitAfk = playersAFK.contains(joueurAfk.getUniqueId());
        if (etaitAfk) {
            stopSession(joueurAfk);
        } else {
            startAfk(joueurAfk);
        }
        return !etaitAfk;
    }

    /**
     * Ajoute le joueur dans la team afk et sauvegarde son ancienne équipe
     * @param joueurAfk Joueur afk
     */
    private void setPlayerAFK(Player joueurAfk) {
        joueurAfk.playerListName(Component.text("💤  ").append(buildTeamAwareName(joueurAfk, NamedTextColor.GRAY)));
        joueurAfk.addScoreboardTag(nameAfkTag);
    }


    @EventHandler
    public void onMove(PlayerMoveEvent moveEvent) {
        Player player = moveEvent.getPlayer();
        UUID idPlayer = player.getUniqueId();
        lastActivity.put(idPlayer, System.currentTimeMillis());
        if (playersAFK.contains(idPlayer)) {
            stopSession(player);
        }
    }

    /**
     * Boucle périodique : détecte les nouveaux joueurs afk, et resynchronise le tab list de tout
     * le monde sur sa team actuelle. Ce deuxième point est nécessaire car {@code playerListName}
     * n'est plus mis à jour automatiquement par Minecraft une fois défini manuellement (voir
     * {@link #setPlayerAFK}/{@link #unSetAfkPayers}) — sans cette resynchronisation, un joueur
     * ayant déjà été afk resterait figé sur son ancienne team si elle change entre-temps (via un
     * datapack ou toute autre source externe à ce plugin).
     */
    private void launchTaskAutoAfk() {
        long checkIntereval = plugin.getConfig().getLong("afk.check-interval-ticks");
        afkGlobalTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long delayAfk = plugin.getConfig().getLong("afk.min-time-afk");
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID idPlayer = player.getUniqueId();
                long lastActivityTime = lastActivity.getOrDefault(idPlayer, now);
                if(!playersAFK.contains(idPlayer) && now - lastActivityTime > delayAfk) {
                    startAfk(player);
                } else {
                    refreshDisplayName(player);
                }
            }
        }, 0L, checkIntereval);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        unSetAfkPayers(joinEvent.getPlayer());
        lastActivity.put(joinEvent.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
