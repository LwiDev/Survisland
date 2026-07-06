package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class LiveCommand extends SurvislandCommand {

    public LiveCommand(EmbeddedDiscordBot discordBot) {
        super("live", "Envoyer un message sur Discord", PermissionDefault.OP);

        argument("message", StringArgumentType.greedyString(),
                "le texte à envoyer sur le salon Discord", ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            String message = StringArgumentType.getString(ctx, "message");
            String senderName = sender instanceof Player ? sender.getName() : "Console";

            discordBot.sendLiveMessage(message, senderName, (success, error) -> {
                if (success) {
                    MessageUtils.sendSuccessMessage(sender, "§7" + senderName + " §8» §7Discord : §f" + message);
                } else {
                    MessageUtils.sendErrorMessage(sender, "Erreur lors de l'envoi du message : " + (error != null ? error : "Erreur inconnue"));
                    if (error != null && error.contains("channel")) {
                        MessageUtils.sendSecondaryMessage(sender, "Utilisez /setlive <channel_id> pour configurer le channel Discord");
                    }
                }
            });

            return Command.SINGLE_SUCCESS;
        });
    }
}
