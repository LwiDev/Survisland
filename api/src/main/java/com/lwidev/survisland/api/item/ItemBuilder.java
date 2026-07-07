package com.lwidev.survisland.api.item;

import net.kyori.adventure.text.Component;
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
