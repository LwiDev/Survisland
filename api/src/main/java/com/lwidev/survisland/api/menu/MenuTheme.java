package com.lwidev.survisland.api.menu;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.HeadsUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

/** Shared visual building blocks for {@link SurvislandMenu}s: filler, navigation items, click sound. */
public final class MenuTheme {

    public static final Sound CLICK_SOUND = Sound.UI_BUTTON_CLICK;

    private MenuTheme() {
    }

    /** {row offset from the nearest edge, column} pairs describing one quarter of the corner accent; mirrored on both axes in {@link #decorateCorners}. */
    private static final int[][] CORNER_ACCENT = {{0, 1}, {0, 2}, {0, 8}, {0, 9}, {1, 1}, {1, 9}};

    public static ItemStack filler() {
        return filler(Material.GRAY_STAINED_GLASS_PANE);
    }

    public static ItemStack filler(Material material) {
        return ItemBuilder.of(material).build();
    }

    /** Default filler accent in the four corners — called once from {@link SurvislandMenu}'s constructor. */
    static void decorateCorners(SurvislandMenu menu, int rows) {
        for (int[] accent : CORNER_ACCENT) {
            int rowOffset = accent[0];
            int col = accent[1];
            if (rowOffset > 0 && rows <= 1) {
                continue;
            }
            menu.item(1 + rowOffset, col, filler(Material.ORANGE_STAINED_GLASS_PANE));
            menu.item(rows - rowOffset, col, filler(Material.ORANGE_STAINED_GLASS_PANE));
        }
    }

    /** Wires up the back button, always in the bottom-left corner — called once from {@link SurvislandMenu}'s constructor. */
    static void addBackButton(SurvislandMenu menu, int rows) {
        menu.item(rows, 1, backItem(), _ -> menu.back());
    }

    /** Formats a menu title with the shared accent color. */
    public static Component formatTitle(String title) {
        return Component.text(title, BrandUtils.PRIMARY).decorate(TextDecoration.BOLD);
    }

    public static ItemStack backItem() {
        return ItemBuilder.skull(HeadsUtils.RED_LEFT_ARROW).setName(Component.text("← Retour", BrandUtils.SECONDARY)).build();
    }

    public static ItemStack previousPageItem() {
        return ItemBuilder.skull(HeadsUtils.GOLD_LEFT_ARROW).setName(Component.text("← Précédent", BrandUtils.TERTIARY)).build();
    }

    public static ItemStack nextPageItem() {
        return ItemBuilder.skull(HeadsUtils.GOLD_RIGHT_ARROW).setName(Component.text("Suivant →", BrandUtils.TERTIARY)).build();
    }

    /** On/off state item (e.g. a feature toggle button), consistent wool color + label across menus. */
    public static ItemStack toggleItem(String label, boolean enabled) {
        Material material = enabled ? Material.LIME_WOOL : Material.RED_WOOL;
        NamedTextColor stateColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
        String state = enabled ? "Activé" : "Désactivé";
        return ItemBuilder.of(material)
                .setName(Component.text(label, NamedTextColor.WHITE))
                .setLore(Component.text("État : ", NamedTextColor.GRAY).append(Component.text(state, stateColor)))
                .build();
    }
}
