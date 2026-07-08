package com.lwidev.survisland.api.menu.player;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Live view/edit of another player's full equipment — storage, hotbar, armor and offhand — plus a
 * read-only glance at their health/food. Unlike {@code viewer.openInventory(target.getInventory())},
 * which only ever renders the 27+9 storage slots (the client doesn't draw the armor doll for someone
 * else's inventory), this builds a real chest GUI and mirrors every edit back onto {@code target} as
 * it happens (see {@link #onEditableSlotChanged()}) so it still feels like editing the real thing.
 */
public class PlayerInventoryMenu extends SurvislandMenu {

    private static final Component OFFHAND_LABEL = Component.text("Seconde main", NamedTextColor.GOLD);
    private static final Component HELMET_LABEL = Component.text("Casque", NamedTextColor.GOLD);
    private static final Component CHESTPLATE_LABEL = Component.text("Plastron", NamedTextColor.GOLD);
    private static final Component LEGGINGS_LABEL = Component.text("Jambières", NamedTextColor.GOLD);
    private static final Component BOOTS_LABEL = Component.text("Bottes", NamedTextColor.GOLD);

    private final Player target;

    public PlayerInventoryMenu(Player viewer, Player target) {
        super(viewer, 6, target.getName() + " — Inventaire");
        this.target = target;

        markEditable(1, 3, 1, 7); // armure + offhand
        markEditable(2, 1, 4, 9); // stockage
        markEditable(5, 1, 5, 9); // hotbar

        item(6, 4, healthItem());
        item(6, 6, foodItem());

        loadFromTarget();
    }

    private void loadFromTarget() {
        PlayerInventory inventory = target.getInventory();

        ItemStack[] storage = inventory.getStorageContents();
        for (int col = 1; col <= 9; col++) {
            item(5, col, storage[col - 1]);
        }
        for (int i = 0; i < 27; i++) {
            item(2 + i / 9, 1 + i % 9, storage[9 + i]);
        }

        ItemStack[] armor = inventory.getArmorContents();
        item(1, 3, ItemBuilder.of(inventory.getItemInOffHand()).addLore(OFFHAND_LABEL).build());
        item(1, 4, ItemBuilder.of(armor[3]).addLore(HELMET_LABEL).build()); // casque
        item(1, 5, ItemBuilder.of(armor[2]).addLore(CHESTPLATE_LABEL).build()); // plastron
        item(1, 6, ItemBuilder.of(armor[1]).addLore(LEGGINGS_LABEL).build()); // jambières
        item(1, 7, ItemBuilder.of(armor[0]).addLore(BOOTS_LABEL).build()); // bottes
    }

    @Override
    protected void onEditableSlotChanged() {
        if (!target.isOnline()) {
            return;
        }

        ItemStack[] storage = new ItemStack[36];
        for (int col = 1; col <= 9; col++) {
            storage[col - 1] = itemAt(5, col);
        }
        for (int i = 0; i < 27; i++) {
            storage[9 + i] = itemAt(2 + i / 9, 1 + i % 9);
        }
        target.getInventory().setStorageContents(storage);

        target.getInventory().setArmorContents(new ItemStack[]{
                unlabeled(itemAt(1, 7), BOOTS_LABEL),
                unlabeled(itemAt(1, 6), LEGGINGS_LABEL),
                unlabeled(itemAt(1, 5), CHESTPLATE_LABEL),
                unlabeled(itemAt(1, 4), HELMET_LABEL)});
        target.getInventory().setItemInOffHand(unlabeled(itemAt(1, 3), OFFHAND_LABEL));
    }

    /** Undoes the display-only label {@link #loadFromTarget()} stamps on armor/offhand items, so it never
     * leaks back onto the target's real gear on close (see {@link #onEditableSlotChanged()}). */
    private ItemStack unlabeled(ItemStack stack, Component label) {
        if (stack == null || !stack.hasItemMeta()) {
            return stack;
        }
        ItemMeta meta = stack.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty() || !lore.get(lore.size() - 1).equals(label.decoration(TextDecoration.ITALIC, false))) {
            return stack;
        }
        ItemStack clone = stack.clone();
        ItemMeta cloneMeta = clone.getItemMeta();
        List<Component> newLore = new ArrayList<>(lore.subList(0, lore.size() - 1));
        cloneMeta.lore(newLore.isEmpty() ? null : newLore);
        clone.setItemMeta(cloneMeta);
        return clone;
    }

    private ItemStack healthItem() {
        int health = (int) Math.ceil(target.getHealth());
        int maxHealth = (int) Math.ceil(Objects.requireNonNull(target.getAttribute(Attribute.MAX_HEALTH)).getValue());
        return ItemBuilder.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZmN2I5N2U2NDY1NTM0ZjM4YjJhZmI0MDUxYzk0OWYyNjczYTY0ZjE0NTNlZjY5ODY1OTQyNGFhNWM0ZWYxZiJ9fX0=")
                .setName(Component.text("Vie", NamedTextColor.RED)).addLore(Component.text(health + " / " + maxHealth, NamedTextColor.WHITE)).build();
    }

    private ItemStack foodItem() {
        return ItemBuilder.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ0NjVhOGE1NmUzYjkyYmVjMDY4YWE3ZjcwNTAwODM3ZTM4ZTIwMzM1YTlhY2M5Y2ZiMmRmNWY5MDBmNGVkNiJ9fX0=")
                .setName(Component.text("Faim", NamedTextColor.GOLD)).addLore(Component.text(target.getFoodLevel() + " / 20", NamedTextColor.WHITE)).build();
    }
}
