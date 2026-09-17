package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.services.AfkManager;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class AfkCommand extends SurvislandCommand {

    public AfkCommand(Survisland plugin, AfkManager afkManager) {
        super("afk", "Permet de se mettre afk", List.of(), true, PermissionDefault.TRUE);

        executes(ctx -> {
            Player player = (Player) ctx.getSource().getSender();
            afkManager.startAfk(player);
            return Command.SINGLE_SUCCESS;
        });

        restrictedArgument("joueur", ArgumentTypes.player(), "un joueur en ligne à basculer en afk",
                PermissionDefault.OP, ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Player target = resolvePlayer(ctx, "joueur");

                    boolean nowAfk = afkManager.toggleAfk(target);
                    MessageUtils.sendSuccessMessage(sender, MessageUtils.highlight(target.getName()),
                            nowAfk ? " est maintenant afk." : " n'est plus afk.");
                    return Command.SINGLE_SUCCESS;
                });
    }
}
