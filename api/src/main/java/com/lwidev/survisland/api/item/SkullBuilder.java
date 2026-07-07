package com.lwidev.survisland.api.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/** Player-head variant of {@link ItemBuilder}, built from a {@link PlayerProfile}. */
public class SkullBuilder extends ItemBuilder {

    SkullBuilder(UUID ownerId, String ownerName) {
        super(new ItemStack(Material.PLAYER_HEAD));
        PlayerProfile profile = Bukkit.createProfile(ownerId, ownerName);
        ((SkullMeta) meta).setPlayerProfile(profile);
    }

    @Override
    public SkullBuilder setName(Component name) {
        super.setName(name);
        return this;
    }

    @Override
    public SkullBuilder addLore(Component... lines) {
        super.addLore(lines);
        return this;
    }
}
