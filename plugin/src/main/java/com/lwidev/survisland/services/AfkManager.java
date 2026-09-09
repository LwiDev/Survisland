package com.lwidev.survisland.services;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.Shutdownable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AfkManager implements Listener, Shutdownable {


    public static final String TEAM_AFK = "zteamAFK";
    private final Survisland plugin;
    private final HashMap<UUID, Listener> afkListeners;
    private final ArrayList<UUID> playersAFK;
    private final HashMap<UUID, Team> teamHashMap;
    public final HashMap<UUID, Long> lastActivity;
    private BukkitTask afkGlobalTask;

    public AfkManager(Survisland survisland) {
        this.plugin = survisland;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        afkListeners = new HashMap<>();
        teamHashMap = new HashMap<>();
        playersAFK = new ArrayList<>();
        lastActivity = new HashMap<>();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team afkTeam = scoreboard.getTeam(TEAM_AFK);
        if(afkTeam == null) {
            creationAfkTeam(scoreboard);
        }

        launchTaskAutoAfk();
    }

    private static void creationAfkTeam(Scoreboard scoreboard) {
        Team afkTeam;
        afkTeam = scoreboard.registerNewTeam(TEAM_AFK);
        afkTeam.color(NamedTextColor.GRAY);
        afkTeam.prefix(Component.text("|| ")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
        );
        afkTeam.suffix(Component.text(" AFK")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
        );
    }

    @Override
    public void shutdown() {
        for (UUID idPlayer : playersAFK) {
            HandlerList.unregisterAll(afkListeners.remove(idPlayer));
            stopSession(Bukkit.getPlayer(idPlayer));
        }
        if(afkGlobalTask != null) {
            afkGlobalTask.cancel();
        }
        lastActivity.clear();
        afkListeners.clear();
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
        afkListeners.remove(joueurAfk.getUniqueId());
        playersAFK.remove(joueurAfk.getUniqueId());
        MessageUtils.sendInfoMessage(joueurAfk, "Vous n'êtes plus afk.");
    }

    /**
     * Le joueur est actuellement dans la Team Afk
     * On lui enlève la Team afk et on lui remet son ancienne team S'il en avait une
     * @param joueurAfk JoueurAfk
     */
    private void unSetAfkPayers(Player joueurAfk) {
        Team team = joueurAfk.getScoreboard().getEntryTeam(joueurAfk.getName());
        Object valueOldTeam = teamHashMap.get(joueurAfk.getUniqueId());
        if (valueOldTeam != null) {
            Team oldTeamPlayer = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(
                    teamHashMap.get(joueurAfk.getUniqueId()).getName()
            );
            oldTeamPlayer.addEntry(joueurAfk.getName());
        }

        team.removeEntry(joueurAfk.getName());
        teamHashMap.remove(joueurAfk.getUniqueId());
    }

    /**
     * Fonction principale qui fait les vérifications puis créé un listener quand le joueur ne sera plus afk
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

        Listener listener = new Listener() {
            @EventHandler
            public void onMove(PlayerMoveEvent moveEvent) {
                if (!moveEvent.getPlayer().getUniqueId().equals(idJoueur))
                    return;

                stopSession(joueurAfk);
                HandlerList.unregisterAll(this);
            }
        };

        Bukkit.getPluginManager().registerEvents(listener, plugin);
        afkListeners.put(idJoueur, listener);
        MessageUtils.sendSuccessMessage(joueurAfk, "Vous êtes maintenant afk.");
    }

    /**
     * Ajoute le joueur dans la team afk et sauvegarde son ancienne équipe
     * @param joueurAfk Joueur afk
     */
    private void setPlayerAFK(Player joueurAfk) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = joueurAfk.getScoreboard().getEntryTeam(joueurAfk.getName());
        Team afkTeam = scoreboard.getTeam(TEAM_AFK);

        if(afkTeam == null) {
            creationAfkTeam(scoreboard);
        }

        teamHashMap.put(joueurAfk.getUniqueId(), team);
        if (team != null) {
            team.removeEntry(joueurAfk.getName());
        }
        afkTeam.addEntry(joueurAfk.getName());
    }


    @EventHandler
    public void onMove(PlayerMoveEvent moveEvent) {
        lastActivity.put(moveEvent.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

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
                }
            }
        }, 0L, checkIntereval);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        lastActivity.put(joinEvent.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
