package com.lwidev.survisland.menu.game;

import com.lwidev.survisland.api.item.ItemBuilder;
import com.lwidev.survisland.api.menu.SurvislandMenu;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.game.GameClockService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

/** Day/night cycle controls for the viewer's current world. */
public class TimeMenu extends SurvislandMenu {

    public TimeMenu(Player viewer) {
        super(viewer, 3, "Temps de jeu");

        World world = viewer.getWorld();

        item(2, 4, ItemBuilder.of(Material.SUNFLOWER).setName(Component.text("Passer au jour", BrandUtils.SECONDARY)).build(), _ -> {
                    GameClockService.setDay(world);
                    MessageUtils.sendSuccessMessage(viewer, "Il fait maintenant ", MessageUtils.highlight("jour", BrandUtils.SECONDARY), ".");
                });
        item(2, 5, ItemBuilder.of(Material.BLACK_CONCRETE).setName(Component.text("Passer à la nuit", BrandUtils.SECONDARY)).build(), _ -> {
                    GameClockService.setNight(world);
                    MessageUtils.sendSuccessMessage(viewer, "Il fait maintenant ", MessageUtils.highlight("nuit", BrandUtils.SECONDARY), ".");
                });
        item(2, 6, ItemBuilder.of(Material.CLOCK).setName(Component.text("Avancer d'une journée", BrandUtils.SECONDARY)).build(), _ -> {
                    GameClockService.skipToNextDay(world);
                    MessageUtils.sendSuccessMessage(viewer, "Une ", MessageUtils.highlight("journée complète", BrandUtils.SECONDARY), " s'est écoulée.");
                });
    }
}
