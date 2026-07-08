package com.lwidev.survisland.services;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.Shutdownable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FollowManager implements Listener, Shutdownable {

    private record FollowSession(UUID targetId, BukkitTask task) {
    }

    private final Survisland plugin;
    private final Map<UUID, FollowSession> activeFollows = new HashMap<>();

    public FollowManager(Survisland plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startFollow(Player follower, Player target, double maxDistance, long checkIntervalTicks) {
        stopFollow(follower);

        UUID followerId = follower.getUniqueId();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!follower.isOnline()) {
                stopSession(followerId);
                return;
            }
            if (!target.isOnline()) {
                MessageUtils.sendErrorMessage(follower, "Le joueur que vous suiviez s'est déconnecté.");
                stopSession(followerId);
                return;
            }

            double distance = follower.getLocation().distance(target.getLocation());
            if (distance > maxDistance) {
                follower.teleport(target.getLocation().clone().setDirection(target.getLocation().getDirection()).add(-2, 2, 0));
            }
        }, 0L, checkIntervalTicks);

        activeFollows.put(followerId, new FollowSession(target.getUniqueId(), task));
    }

    public void stopFollow(Player follower) {
        stopSession(follower.getUniqueId());
    }

    private void stopSession(UUID followerId) {
        FollowSession session = activeFollows.remove(followerId);
        if (session != null) {
            session.task().cancel();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        stopSession(playerId);
        activeFollows.entrySet().removeIf(entry -> {
            boolean followsQuitter = entry.getValue().targetId().equals(playerId);
            if (followsQuitter) {
                entry.getValue().task().cancel();
            }
            return followsQuitter;
        });
    }

    @Override
    public void shutdown() {
        activeFollows.values().forEach(session -> session.task().cancel());
        activeFollows.clear();
    }
}
