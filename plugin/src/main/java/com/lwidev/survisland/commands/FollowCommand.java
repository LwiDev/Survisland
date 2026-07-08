package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.utils.FollowManager;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class FollowCommand extends SurvislandCommand {

    public FollowCommand(Survisland plugin, FollowManager followManager) {
        super("follow", "Suivre un joueur en mode spectateur", List.of(), true, PermissionDefault.TRUE);

        double maxDistance = plugin.getConfig().getDouble("follow.max-distance", 15);
        long checkIntervalTicks = plugin.getConfig().getLong("follow.check-interval-ticks", 10);

        subcommand("stop").executes(ctx -> {
            Player player = (Player) ctx.getSource().getSender();
            followManager.stopFollow(player);
            MessageUtils.sendSuccessMessage(player, "Suivi arrêté");
            return Command.SINGLE_SUCCESS;
        });

        argument("joueur", ArgumentTypes.player(), "un joueur en ligne à suivre", ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            Player player = (Player) sender;
            Player target = resolvePlayer(ctx, "joueur");

            if (player.getGameMode() != GameMode.SPECTATOR) {
                MessageUtils.sendErrorMessage(sender, "Cette commande est réservée aux joueurs en mode spectateur.");
                return -1;
            }
            if (target.getUniqueId().equals(player.getUniqueId())) {
                MessageUtils.sendErrorMessage(sender, "Vous ne pouvez pas vous suivre vous-même.");
                return -1;
            }

            followManager.startFollow(player, target, maxDistance, checkIntervalTicks);
            MessageUtils.sendSuccessMessage(sender, "Vous suivez maintenant ", MessageUtils.highlight(target.getName()), ".");
            return Command.SINGLE_SUCCESS;
        });
    }
}
