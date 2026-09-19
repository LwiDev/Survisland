package com.lwidev.survisland.services;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.Shutdownable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Détection et gestion de l'état afk des joueurs. Ne touche plus au tab list / à l'affichage
 * de la team — c'est la responsabilité de {@link TabListManager}, qui consulte {@link #isAfk}.
 */
public class AfkManager implements Listener, Shutdownable {

    private final Survisland plugin;
    private final ArrayList<UUID> playersAFK;
    public final HashMap<UUID, Long> lastActivity;
    private BukkitTask afkDetectionTask;

    public AfkManager(Survisland survisland) {
        this.plugin = survisland;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        playersAFK = new ArrayList<>();
        lastActivity = new HashMap<>();

        launchTaskAutoAfk();
    }

    @Override
    public void shutdown() {
        if (afkDetectionTask != null) {
            afkDetectionTask.cancel();
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
        playersAFK.remove(joueurAfk.getUniqueId());
        MessageUtils.sendInfoMessage(joueurAfk, "Vous n'êtes plus afk.");
    }

    /**
     * Fonction principale qui fait les vérifications puis passe le joueur en afk.
     * Le retour d'afk est détecté par le {@link #onMove} global, pas besoin d'un listener dédié.
     * @param joueurAfk Joueur afk
     */
    public void startAfk(Player joueurAfk) {
        UUID idJoueur = joueurAfk.getUniqueId();
        if (playersAFK.contains(idJoueur)) {
            MessageUtils.sendMessage(joueurAfk, "Vous êtes déjà afk.");
            return;
        }

        playersAFK.add(idJoueur);
        MessageUtils.sendSuccessMessage(joueurAfk, "Vous êtes maintenant afk.");
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
     * @param player joueur concerné
     * @return {@code true} si le joueur est actuellement considéré afk
     */
    public boolean isAfk(Player player) {
        return playersAFK.contains(player.getUniqueId());
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
     * Boucle périodique : détecte les joueurs inactifs depuis trop longtemps et les passe afk.
     */
    private void launchTaskAutoAfk() {
        long checkInterval = plugin.getConfig().getLong("afk.check-interval-ticks");
        afkDetectionTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long delayAfk = plugin.getConfig().getLong("afk.min-time-afk");
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID idPlayer = player.getUniqueId();
                long lastActivityTime = lastActivity.getOrDefault(idPlayer, now);
                if (!playersAFK.contains(idPlayer) && now - lastActivityTime > delayAfk) {
                    startAfk(player);
                }
            }
        }, 0L, checkInterval);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        lastActivity.put(joinEvent.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}