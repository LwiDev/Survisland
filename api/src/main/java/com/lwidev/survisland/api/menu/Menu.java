package com.lwidev.survisland.api.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Utilitaire de menu (inventaire) basique pour Bukkit/Paper.
 *
 * <p><b>État : alpha.</b> Seule la base est en place (création, items, clic simple,
 * fermeture) — pas de pagination, pas de sous-menus, pas de gestion des menus
 * partagés entre plusieurs joueurs. À faire évoluer selon les besoins réels
 * des plugins qui l'utilisent.</p>
 */
@ApiStatus.Experimental
public class Menu implements Listener {

    private static final ConcurrentHashMap<UUID, Menu> activeMenus = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final Player player;
    private Inventory inventory;
    private BiConsumer<Integer, Boolean> clickHandler;

    public Menu(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public Menu create(String title, int size) {
        this.inventory = Bukkit.createInventory(null, size, title);
        return this;
    }

    public Menu setItem(int slot, Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        inventory.setItem(slot, item);
        return this;
    }

    public Menu setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        return this;
    }

    public Menu onClick(BiConsumer<Integer, Boolean> handler) {
        this.clickHandler = handler;
        return this;
    }

    public void open() {
        activeMenus.put(player.getUniqueId(), this);
        player.openInventory(inventory);
    }

    public void close() {
        player.closeInventory();
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = List.of(lore);
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker)) return;

        Menu menu = activeMenus.get(clicker.getUniqueId());
        if (menu == null || !event.getInventory().equals(menu.inventory)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (menu.clickHandler != null) {
            menu.clickHandler.accept(event.getSlot(), event.isRightClick());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            Menu menu = activeMenus.remove(player.getUniqueId());
            if (menu != null) {
                HandlerList.unregisterAll(menu);
            }
        }
    }
}
