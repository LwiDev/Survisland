package com.lwidev.survisland.api.menu;

import com.lwidev.survisland.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

/** Shared visual building blocks for {@link SurvislandMenu}s: filler, navigation items, click sound. */
public final class MenuTheme {

    public static final Sound CLICK_SOUND = Sound.UI_BUTTON_CLICK;

    private MenuTheme() {
    }

    public static ItemStack filler() {
        return ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).setName(Component.empty()).build();
    }

    public static ItemStack backItem() {
        return ItemBuilder.of(Material.ARROW).setName(Component.text("← Retour", NamedTextColor.GOLD)).build();
    }

    public static ItemStack previousPageItem() {
        return ItemBuilder.of(Material.ARROW).setName(Component.text("← Précédent", NamedTextColor.YELLOW)).build();
    }

    public static ItemStack nextPageItem() {
        return ItemBuilder.of(Material.ARROW).setName(Component.text("Suivant →", NamedTextColor.YELLOW)).build();
    }
}
