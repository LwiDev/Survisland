package com.lwidev.survisland.api.menu;

import com.lwidev.survisland.api.command.SurvislandCommandManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registers the single Bukkit listener behind every {@link SurvislandMenu} and tracks
 * each player's currently open menu plus their navigation history. Call
 * {@link #register(JavaPlugin)} once from the plugin's {@code onEnable()} — mirrors
 * {@link SurvislandCommandManager#register}.
 */
public final class SurvislandMenuManager implements Listener {

    private static SurvislandMenuManager instance;

    private final Map<UUID, SurvislandMenu> openMenus = new HashMap<>();
    private final Map<UUID, Deque<SurvislandMenu>> history = new HashMap<>();

    private SurvislandMenuManager() {
    }

    public static void register(JavaPlugin plugin) {
        if (instance != null) {
            return;
        }
        instance = new SurvislandMenuManager();
        plugin.getServer().getPluginManager().registerEvents(instance, plugin);
    }

    static SurvislandMenuManager get() {
        if (instance == null) {
            throw new IllegalStateException("SurvislandMenuManager.register(plugin) was never called");
        }
        return instance;
    }

    void open(Player player, SurvislandMenu menu) {
        openMenus.put(player.getUniqueId(), menu);
        player.openInventory(menu.inventory());
    }

    void openSubMenu(Player player, SurvislandMenu subMenu) {
        SurvislandMenu current = openMenus.get(player.getUniqueId());
        if (current != null) {
            history.computeIfAbsent(player.getUniqueId(), id -> new ArrayDeque<>()).push(current);
        }
        open(player, subMenu);
    }

    void back(Player player) {
        Deque<SurvislandMenu> stack = history.get(player.getUniqueId());
        SurvislandMenu previous = stack != null ? stack.poll() : null;
        if (previous != null) {
            open(player, previous);
        } else {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        SurvislandMenu menu = openMenus.get(player.getUniqueId());
        if (menu == null || !menu.inventory().equals(event.getView().getTopInventory())) {
            return;
        }
        menu.handleClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        SurvislandMenu menu = openMenus.get(player.getUniqueId());
        if (menu != null && menu.inventory().equals(event.getInventory())) {
            openMenus.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        openMenus.remove(id);
        history.remove(id);
    }
}
