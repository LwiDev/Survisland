package com.lwidev.survisland.services;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.Shutdownable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AfkManager implements Listener, Shutdownable {

    private record AfkSession(UUID targetId, BukkitTask task) { }
    private final Survisland plugin;
    private final Map<UUID, AfkSession> activeAfk = new HashMap<>();

    /**
     * Constructor pour le suivi des joueurs AFK
     * @param survisland Plugin
     */
    public AfkManager(Survisland survisland) {
        this.plugin = survisland;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /** Fin de l'AFK Manager */
    @Override
    public void shutdown() {
        activeAfk.values().forEach(session -> session.task().cancel());
        activeAfk.clear();
    }

    /**
     * Évènement du joueur se déconnectant
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        stopSession(playerId);
        activeAfk.entrySet().removeIf(entry -> {
            boolean playerQuit = entry.getValue().targetId().equals(playerId);
            if (playerQuit) {
                entry.getValue().task().cancel();
            }
            return playerQuit;
        });
    }

    /**
     * Stop la mise en Afk d'un joueur
     * @param afkPlayerId UUID du joueur afk
     */
    private void stopSession(UUID afkPlayerId) {
        AfkSession session = activeAfk.remove(afkPlayerId);
        if (session != null) {
            session.task().cancel();
        }
    }

    /**
     * Appel lorsque que le joueur n'est plus AFK
     * À améliorer pour le faire dès qu'il bouge après avoir tapé la commande
     * @param joueurAfk Player qui était AFK
     */
    public void stopAfk(Player joueurAfk) {
        stopSession(joueurAfk.getUniqueId());
    }

    public void startAfk(Player joueurAfk) {

    }
}
