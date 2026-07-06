package com.lwidev.survisland.listeners;

import com.lwidev.survisland.utils.PauseManager;
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
        if (!PauseManager.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        // Empêcher les joueurs gelés de prendre TOUT type de dégâts (chute, feu, noyade, etc.)
        if (event.getEntity() instanceof Player player && PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        // Empêcher les joueurs gelés d'attaquer
        if (event.getDamager() instanceof Player damager && PauseManager.isFrozen(damager)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDismount(EntityDismountEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        if (event.getEntity() instanceof Player player && PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            PauseManager.freezePlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        if (event.getWhoClicked() instanceof Player player && PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAttemptPickupItem(PlayerAttemptPickupItemEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!PauseManager.isPaused()) {
            return;
        }

        Player player = event.getPlayer();
        if (PauseManager.isFrozen(player)) {
            event.setCancelled(true);
        }
    }
}
