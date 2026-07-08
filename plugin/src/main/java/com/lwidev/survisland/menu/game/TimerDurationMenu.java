package com.lwidev.survisland.menu.game;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.menu.widget.AnvilInputMenu;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.PluralUtils;
import com.lwidev.survisland.menu.MenuContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Pick a duration (preset or custom, in minutes) to start a named "épreuve" timer. */
public class TimerDurationMenu extends SurvislandMenu {

    public TimerDurationMenu(Player viewer, MenuContext ctx, String name) {
        super(viewer, 3, "Durée — " + name);

        item(2, 3, presetItem(5), _ -> startAndReturn(viewer, ctx, name, 5));
        item(2, 4, presetItem(10), _ -> startAndReturn(viewer, ctx, name, 10));
        item(2, 5, presetItem(15), _ -> startAndReturn(viewer, ctx, name, 15));
        item(2, 6, presetItem(20), _ -> startAndReturn(viewer, ctx, name, 20));
        item(2, 7, presetItem(30), _ -> startAndReturn(viewer, ctx, name, 30));

        item(2, 5, ItemBuilder.of(Material.WRITABLE_BOOK).setName(Component.text("Durée personnalisée", BrandUtils.SECONDARY)).build(),
                _ -> AnvilInputMenu.open(ctx.plugin(), viewer, "Durée en minutes", "45", text -> {
                    try {
                        startAndReturn(viewer, ctx, name, Integer.parseInt(text.trim()));
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage(viewer, "Durée invalide : entrez un nombre de minutes.");
                        new GameMenu(viewer, ctx).open();
                    }
                }));
    }

    private void startAndReturn(Player viewer, MenuContext ctx, String name, int minutes) {
        ctx.timerService().start(name, minutes * 60L * 20L);
        MessageUtils.sendSuccessMessage(viewer, "Timer ", MessageUtils.highlight(name, BrandUtils.PRIMARY), " lancé pour ", MessageUtils.highlight(PluralUtils.withCount(minutes, "minute"), BrandUtils.SECONDARY), ".");
        new GameMenu(viewer, ctx).open();
    }

    private ItemStack presetItem(int minutes) {
        return ItemBuilder.of(Material.CLOCK).setName(Component.text(PluralUtils.withCount(minutes, "minute"), BrandUtils.TERTIARY)).build();
    }
}
