package com.lwidev.survisland.api.menu;

import com.lwidev.survisland.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Base for menus that let the viewer pick a player from a paginated list (e.g.
 * online players, spectators to add...). Subclasses just declare the candidate
 * list and what happens on selection — the player item and pagination are handled
 * here.
 */
public abstract class PlayerSelectionMenu extends SurvislandMenu {

    protected PlayerSelectionMenu(Player viewer, int rows, String title) {
        super(viewer, rows, title);
    }

    /** Declares the paginated player list using the default full-width layout. */
    protected final void selectFrom(List<Player> candidates, BiConsumer<InventoryClickEvent, Player> onSelect) {
        paginate(candidates, PlayerSelectionMenu::playerItem, onSelect);
    }

    private static ItemStack playerItem(Player target) {
        return ItemBuilder.skull(target.getUniqueId(), target.getName()).setName(Component.text(target.getName(), NamedTextColor.WHITE)).build();
    }
}
