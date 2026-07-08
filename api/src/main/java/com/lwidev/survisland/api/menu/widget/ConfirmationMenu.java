package com.lwidev.survisland.api.menu.widget;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.MenuTheme;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.HeadsUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Generic yes/no confirmation dialog, showing the item the confirmation is actually about (e.g.
 * the item about to be given) instead of a generic placeholder icon. Always open it via
 * {@code openSubMenu(...)} from the menu asking for confirmation, so cancelling (or confirming)
 * can return there via {@link #back()}.
 *
 * <pre>{@code
 * openSubMenu(new ConfirmationMenu(player, Component.text("Confirmer"), handItem,
 *         Component.text("Donner à tous les joueurs ?"),
 *         () -> targets.forEach(t -> t.getInventory().addItem(handItem.clone()))));
 * }</pre>
 */
public final class ConfirmationMenu extends SurvislandMenu {

    public ConfirmationMenu(Player viewer, Component title, ItemStack displayItem, Component question, Runnable onConfirm) {
        super(viewer, 3, title);
        item(rows, 1, MenuTheme.filler(Material.ORANGE_STAINED_GLASS_PANE)); // no "back" — Confirmer/Annuler already cover that, but keep the corner's filler instead of a gap

        item(2, 5, ItemBuilder.of(displayItem).setName(question).build());
        item(3, 4, ItemBuilder.skull(HeadsUtils.GREEN_CHECKMARK).setName(Component.text("Confirmer", NamedTextColor.GREEN)).build(),
                _ -> {
                    onConfirm.run();
                    back();
                });
        item(3, 6, ItemBuilder.skull(HeadsUtils.RED_X).setName(Component.text("Annuler", NamedTextColor.RED)).build(), _ -> back());
    }
}
