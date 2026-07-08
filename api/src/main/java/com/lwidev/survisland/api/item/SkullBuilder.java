package com.lwidev.survisland.api.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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

    /** Decorative head from a raw base64 "textures" property blob (e.g. copied from minecraft-heads.com) — no real owner. */
    SkullBuilder(String base64Texture) {
        super(new ItemStack(Material.PLAYER_HEAD));
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures", base64Texture));
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
