package com.lwidev.survisland.api.item;

import com.lwidev.survisland.api.utils.BrandUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fluent builder for {@link ItemStack}s. Extend it (see {@link SkullBuilder}) for
 * item kinds that need more than name/lore — add a new variant only once a real
 * feature needs it, rather than up front.
 */
public class ItemBuilder {

    protected final ItemStack item;
    protected final ItemMeta meta;

    protected ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item.clone());
    }

    /** Player-head variant, resolved by owner UUID (works for offline/never-joined players too). */
    public static SkullBuilder skull(UUID ownerId, String ownerName) {
        return new SkullBuilder(ownerId, ownerName);
    }

    /** Decorative head variant from a raw base64 "textures" property blob (e.g. copied from minecraft-heads.com). */
    public static SkullBuilder skull(String base64Texture) {
        return new SkullBuilder(base64Texture);
    }

    public ItemBuilder setName(Component name) {
        meta.displayName(noItalic(name));
        return this;
    }

    public ItemBuilder setLore(Component... lore) {
        meta.lore(noItalic(Arrays.asList(lore)));
        return this;
    }

    public ItemBuilder addLore(Component... lines) {
        List<Component> existing = meta.lore();
        List<Component> lore = existing != null ? new ArrayList<>(existing) : new ArrayList<>();
        lore.addAll(noItalic(Arrays.asList(lines)));
        meta.lore(lore);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Consistent {@code "<left-click key> : .../<right-click key> : ..."} hint, for items whose two
     * click types do different things. Uses keybind components rather than hardcoded "Clic gauche"/
     * "Clic droit" text, so the label follows each player's own control bindings.
     */
    public ItemBuilder addLeftRightClickLore(String leftAction, String rightAction) {
        return addLore(
                keybind("attack").append(Component.text(" : ", NamedTextColor.GRAY)).append(Component.text(leftAction, BrandUtils.TERTIARY)),
                keybind("use").append(Component.text(" : ", NamedTextColor.GRAY)).append(Component.text(rightAction, BrandUtils.TERTIARY)));
    }

    /**
     * Consistent {@code "<click key> : ..."} hint, for items with a single click action worth
     * spelling out (any click triggers it, so the left-click keybind is shown as the default label).
     */
    public ItemBuilder addClickLore(String action) {
        return addLore(Component.empty(), keybind("attack").append(Component.text(" : ", NamedTextColor.GRAY)).append(Component.text(action, BrandUtils.TERTIARY)));
    }

    /** {@link #keybind(String, NamedTextColor)} in {@link NamedTextColor#GRAY}, the usual lore color. */
    public static Component keybind(String key) {
        return keybind(key, NamedTextColor.GRAY);
    }

    /** Vanilla keybind component (e.g. {@code keybind("attack")} → {@code key.attack}), so lore labels follow each player's own control bindings instead of a hardcoded name. */
    public static Component keybind(String key, NamedTextColor color) {
        return Component.keybind("key." + key, color);
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

    private static List<Component> noItalic(List<Component> components) {
        return components.stream().map(ItemBuilder::noItalic).collect(Collectors.toList());
    }
}
