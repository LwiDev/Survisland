package com.lwidev.survisland.api.menu.player;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Like {@link PlayerSelectionMenu}, but for pickers that must also list offline players
 * (e.g. assigning a team to someone who isn't currently connected). Shows an online/offline
 * badge on each head since offline targets can't support every action (inventory, TP, ...).
 */
public abstract class ProfileSelectionMenu extends SurvislandMenu {

    protected ProfileSelectionMenu(Player viewer, int rows, String title) {
        super(viewer, rows, title);
    }

    protected final void selectFrom(List<OfflinePlayer> candidates, BiConsumer<InventoryClickEvent, OfflinePlayer> onSelect) {
        paginate(candidates, this::profileItem, onSelect);
    }

    /** Same as {@link #selectFrom(List, BiConsumer)}, but laid out in a custom area instead of the full-width default. */
    protected final void selectFrom(int startRow, int startCol, int itemsPerRow, int pageRows, List<OfflinePlayer> candidates, BiConsumer<InventoryClickEvent, OfflinePlayer> onSelect) {
        paginate(startRow, startCol, itemsPerRow, pageRows, candidates, this::profileItem, onSelect);
    }

    private ItemStack profileItem(OfflinePlayer profile) {
        String name = profile.getName() != null ? profile.getName() : profile.getUniqueId().toString();
        return ItemBuilder.skull(profile.getUniqueId(), name).setName(Component.text(name, NamedTextColor.WHITE)).setLore(state(profile)).build();
    }

    private Component state(OfflinePlayer target) {
        Player online = target.getPlayer();
        if (online == null) return Component.text("Hors ligne", NamedTextColor.RED);
        return online.getGameMode() == GameMode.SPECTATOR ? Component.text("Spectateur", BrandUtils.SECONDARY) : Component.text("En ligne", NamedTextColor.GREEN);
    }
}
