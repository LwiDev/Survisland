package com.lwidev.survisland.api.menu.widget;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.MenuTheme;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.PluralUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Generic gamemode picker applied to one or several players at once (a single player's
 * detail page, or every member of a team). Applies the gamemode directly and reports
 * success to the viewer — callers only supply the target list and a refresh callback.
 */
public final class GameModeSelectMenu extends SurvislandMenu {

    public GameModeSelectMenu(Player viewer, Component title, List<Player> targets, Runnable onApplied) {
        super(viewer, 3, title);

        item(2, 2, gameModeItem(GameMode.SURVIVAL), _ -> apply(viewer, targets, GameMode.SURVIVAL, onApplied));
        item(2, 4, gameModeItem(GameMode.CREATIVE), _ -> apply(viewer, targets, GameMode.CREATIVE, onApplied));
        item(2, 6, gameModeItem(GameMode.ADVENTURE), _ -> apply(viewer, targets, GameMode.ADVENTURE, onApplied));
        item(2, 8, gameModeItem(GameMode.SPECTATOR), _ -> apply(viewer, targets, GameMode.SPECTATOR, onApplied));
    }

    private void apply(Player viewer, List<Player> targets, GameMode mode, Runnable onApplied) {
        targets.forEach(target -> target.setGameMode(mode));
        MessageUtils.sendSuccessMessage(viewer, "Vous avez défini le gamemode ", MessageUtils.highlight(label(mode), BrandUtils.TERTIARY), " à ", MessageUtils.highlight(PluralUtils.withCount(targets.size(), "joueur"), BrandUtils.SECONDARY), ".");
        onApplied.run();
    }

    private ItemStack gameModeItem(GameMode mode) {
        return ItemBuilder.of(material(mode)).setName(Component.text(label(mode), NamedTextColor.WHITE)).build();
    }

    private Material material(GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> Material.IRON_SWORD;
            case CREATIVE -> Material.COMMAND_BLOCK;
            case ADVENTURE -> Material.MAP;
            case SPECTATOR -> Material.ENDER_EYE;
        };
    }

    private String label(GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> "Survie";
            case CREATIVE -> "Créatif";
            case ADVENTURE -> "Aventure";
            case SPECTATOR -> "Spectateur";
        };
    }
}
