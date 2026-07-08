package com.lwidev.survisland.menu.game.announce;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.menu.widget.AnvilInputMenu;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.menu.MenuContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * One-click broadcast of a preset message, or a custom one typed via an anvil prompt.
 * Left-click sends it immediately; right-click opens {@link ScheduleAnnounceMenu} to pick a delay.
 */
public class AnnounceMenu extends SurvislandMenu {

    public AnnounceMenu(Player viewer, MenuContext ctx) {
        super(viewer, 4, "Annonces");

        List<String> presets = ctx.announcementService().presets();
        paginate(presets, this::presetItem, (event, message) -> {
            if (event.getClick().isRightClick()) {
                openSubMenu(new ScheduleAnnounceMenu(viewer, ctx, message));
            } else {
                ctx.announcementService().broadcastNow(message);
            }
        });

        item(4, 5, ItemBuilder.of(Material.WRITABLE_BOOK).setName(Component.text("Message personnalisé", BrandUtils.SECONDARY)).addLeftRightClickLore("Envoyer", "Programmer").build(),
                event -> {
                    boolean schedule = event.getClick().isRightClick();
                    AnvilInputMenu.open(ctx.plugin(), viewer, "Message d'annonce", "Votre message", text -> {
                        if (schedule) {
                            new ScheduleAnnounceMenu(viewer, ctx, text).open();
                        } else {
                            ctx.announcementService().broadcastNow(text);
                            new AnnounceMenu(viewer, ctx).open();
                        }
                    });
                });

    }

    private ItemStack presetItem(String message) {
        return ItemBuilder.of(Material.PAPER).setName(Component.text(message, NamedTextColor.WHITE)).addLeftRightClickLore("Envoyer", "Programmer").build();
    }
}
