package com.lwidev.survisland.menu.game.announce;

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

/** Pick a delay (preset or custom, in minutes) to schedule a given announcement message. */
public class ScheduleAnnounceMenu extends SurvislandMenu {

    public ScheduleAnnounceMenu(Player viewer, MenuContext ctx, String message) {
        super(viewer, 3, "Programmer l'annonce");

        item(2, 2, delayItem(10), _ -> scheduleAndReturn(viewer, ctx, message, 10));
        item(2, 3, delayItem(15), _ -> scheduleAndReturn(viewer, ctx, message, 15));
        item(2, 4, delayItem(20), _ -> scheduleAndReturn(viewer, ctx, message, 20));
        item(2, 5, delayItem(30), _ -> scheduleAndReturn(viewer, ctx, message, 30));

        item(2, 7, ItemBuilder.of(Material.WRITABLE_BOOK).setName(Component.text("Délai personnalisé", BrandUtils.SECONDARY)).build(),
                _ -> AnvilInputMenu.open(ctx.plugin(), viewer, "Délai en minutes", "45", text -> {
                    try {
                        int minutes = Integer.parseInt(text.trim());
                        ctx.announcementService().schedule(message, minutes * 60L * 20L);
                        MessageUtils.sendSuccessMessage(viewer, "Annonce programmée dans ", MessageUtils.highlight(PluralUtils.withCount(minutes, "minute"), BrandUtils.SECONDARY), ".");
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage(viewer, "Délai invalide : entrez un nombre de minutes.");
                    }
                    new AnnounceMenu(viewer, ctx).open();
                }));

    }

    private void scheduleAndReturn(Player viewer, MenuContext ctx, String message, int minutes) {
        ctx.announcementService().schedule(message, minutes * 60L * 20L);
        MessageUtils.sendSuccessMessage(viewer, "Annonce programmée dans ", MessageUtils.highlight(PluralUtils.withCount(minutes, "minute"), BrandUtils.SECONDARY), ".");
        new AnnounceMenu(viewer, ctx).open();
    }

    private ItemStack delayItem(int minutes) {
        return ItemBuilder.of(Material.CLOCK).setName(Component.text(PluralUtils.withCount(minutes, "minute"), BrandUtils.TERTIARY))
                .addClickLore("Programmer l'annonce")
                .build();
    }
}
