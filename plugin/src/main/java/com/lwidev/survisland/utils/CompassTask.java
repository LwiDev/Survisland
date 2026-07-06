package com.lwidev.survisland.utils;

import com.lwidev.survisland.Survisland;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CompassTask extends BukkitRunnable {
    
    private static final Map<UUID, CompassTask> activeTasks = new HashMap<>();
    
    private final Player player;
    private final Location targetLocation;
    private int ticksRemaining;
    
    private CompassTask(Player player, Location targetLocation) {
        this.player = player;
        this.targetLocation = targetLocation;
        this.ticksRemaining = 1200; // 60 secondes * 20 ticks
    }
    
    public static void start(Survisland plugin, Player player, Location targetLocation) {
        // Annuler la tâche existante si elle existe
        stop(player);
        
        CompassTask task = new CompassTask(player, targetLocation);
        activeTasks.put(player.getUniqueId(), task);
        task.runTaskTimer(plugin, 0L, 1L); // Démarrer immédiatement, répéter à chaque tick
    }
    
    public static void stop(Player player) {
        CompassTask existingTask = activeTasks.remove(player.getUniqueId());
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }
    }
    
    @Override
    public void run() {
        // Vérifier si le joueur est toujours en ligne
        if (!player.isOnline()) {
            cleanup();
            return;
        }
        
        // Vérifier si le temps est écoulé
        if (ticksRemaining <= 0) {
            cleanup();
            return;
        }
        
        // Calculer et afficher la direction
        Location playerLocation = player.getLocation();
        
        // Vérifier si le joueur est arrivé au campement
        double dx = targetLocation.getX() - playerLocation.getX();
        double dz = targetLocation.getZ() - playerLocation.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        
        if (distance < 2.0) {
            // Le joueur est arrivé, arrêter la tâche
            player.sendActionBar(Component.text("Vous êtes arrivé !", NamedTextColor.GREEN));
            cleanup();
            return;
        }
        
        Component directionComponent = DirectionUtils.getDirectionComponent(playerLocation, targetLocation);
        player.sendActionBar(directionComponent);
        
        ticksRemaining -= 1; // Décrémenter le nombre de ticks restants
    }
    
    private void cleanup() {
        activeTasks.remove(player.getUniqueId());
        this.cancel();
    }
    
    public static void cleanupAll() {
        for (CompassTask task : activeTasks.values()) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();
    }
}