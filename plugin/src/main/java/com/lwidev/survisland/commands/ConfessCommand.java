package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.confess.ConfessLinkManager;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class ConfessCommand extends SurvislandCommand {

    private final EmbeddedDiscordBot discordBot;
    private final ConfessLinkManager confessLinkManager;

    public ConfessCommand(EmbeddedDiscordBot discordBot, ConfessLinkManager confessLinkManager) {
        super("confess", "Envoyer un message dans votre confess Discord", List.of("conf"), true, PermissionDefault.TRUE);
        this.discordBot = discordBot;
        this.confessLinkManager = confessLinkManager;

        argument("message", StringArgumentType.greedyString(), ctx -> {
            Player player = (Player) ctx.getSource().getSender();
            sendConfess(player, StringArgumentType.getString(ctx, "message"));
            return Command.SINGLE_SUCCESS;
        });
    }

    private void sendConfess(Player player, String message) {
        CommandSender sender = player;
        String playerName = player.getName();

        String channelName = confessLinkManager.getPlayerChannel(playerName);
        if (channelName == null) {
            MessageUtils.sendErrorMessage(sender, "Vous n'aviez lié aucun confess à votre compte !");
            MessageUtils.sendSecondaryMessage(sender, "Utilisez /link pour générer un code sur Minecraft, puis /verify <code> sur Discord.");
            return;
        }

        if (!discordBot.isConnected()) {
            MessageUtils.sendErrorMessage(sender, "Le bot Discord n'est pas connecté !");
            return;
        }

        TextChannel targetChannel = null;
        if (discordBot.getJDA() != null) {
            for (TextChannel channel : discordBot.getJDA().getTextChannels()) {
                if (channel.getName().equals(channelName)) {
                    targetChannel = channel;
                    break;
                }
            }
        }

        if (targetChannel == null) {
            MessageUtils.sendErrorMessage(sender, "Salon Discord introuvable : " + channelName);
            MessageUtils.sendSecondaryMessage(sender, "Contactez un administrateur ou reliez votre compte");
            return;
        }

        String channelId = targetChannel.getId();
        discordBot.sendConfessMessage(channelId, message, playerName, (success, error) -> {
            if (success) {
                MessageUtils.sendSuccessMessage(sender, "§7Message envoyé : §f" + message);
            } else {
                MessageUtils.sendErrorMessage(sender, "Erreur lors de l'envoi du message : " + (error != null ? error : "Erreur inconnue"));
            }
        });
    }
}
