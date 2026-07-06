package com.lwidev.survisland.listeners;

import com.lwidev.survisland.utils.PauseTask;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class PauseListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        // Empêcher les joueurs gelés de prendre TOUT type de dégâts (chute, feu, noyade, etc.)
        if (event.getEntity() instanceof Player player && PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        // Empêcher les joueurs gelés d'attaquer
        if (event.getDamager() instanceof Player damager && PauseTask.isFrozen(damager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDismount(EntityDismountEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        if (event.getEntity() instanceof Player player && PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            PauseTask.freezePlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        if (event.getWhoClicked() instanceof Player player && PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttemptPickupItem(PlayerAttemptPickupItemEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!PauseTask.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseTask.isFrozen(player)) {
            event.setCancelled(true);
        }
    }
}
