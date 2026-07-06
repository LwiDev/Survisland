package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.utils.CompassTask;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class CampCommand extends SurvislandCommand {

    private final Survisland plugin;

    public CampCommand(Survisland plugin) {
        super("camp", "Afficher la direction vers votre campement", List.of("campement"), true, PermissionDefault.TRUE);
        this.plugin = plugin;

        executes(ctx -> {
            Player player = (Player) ctx.getSource().getSender();

            Location bedSpawnLocation = player.getRespawnLocation();
            if (bedSpawnLocation == null) {
                MessageUtils.sendErrorMessage(player, "Vous n'avez pas de campement défini !");
                MessageUtils.sendSecondaryMessage(player, "Dormez dans un lit pour définir votre point de spawn.");
                return Command.SINGLE_SUCCESS;
            }

            CompassTask.start(plugin, player, bedSpawnLocation);
            return Command.SINGLE_SUCCESS;
        });
    }
}
