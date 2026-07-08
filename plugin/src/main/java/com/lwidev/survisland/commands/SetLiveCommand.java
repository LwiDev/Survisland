package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class SetLiveCommand extends SurvislandCommand {

    private final Survisland plugin;
    private final EmbeddedDiscordBot discordBot;

    public SetLiveCommand(Survisland plugin, EmbeddedDiscordBot discordBot) {
        super("setlive", "Configurer le channel Discord cible", PermissionDefault.OP);
        this.plugin = plugin;
        this.discordBot = discordBot;

        executes(ctx -> {
            showCurrentChannel(ctx.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }).onArgument("channel_id", StringArgumentType.word(), ctx -> {
            setChannel(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "channel_id"));
            return Command.SINGLE_SUCCESS;
        });
    }

    private void showCurrentChannel(CommandSender sender) {
        String currentChannel = plugin.getDiscordConfig().getLiveChannelId();

        if (currentChannel == null || currentChannel.isEmpty()) {
            MessageUtils.sendInfoMessage(sender, "Aucun channel Discord configuré.");
            MessageUtils.sendSecondaryMessage(sender, "Utilisez ", MessageUtils.highlight("/setlive <channel_id>", BrandUtils.TERTIARY), " pour configurer un channel.");
        } else {
            MessageUtils.sendSuccessMessage(sender, "Channel Discord actuel : ", MessageUtils.highlight(currentChannel, BrandUtils.PRIMARY));
        }
    }

    private void setChannel(CommandSender sender, String channelId) {
        String currentChannel = plugin.getDiscordConfig().getLiveChannelId();
        if (channelId.equals(currentChannel)) {
            MessageUtils.sendInfoMessage(sender, "C'est déjà le channel configuré !");
            return;
        }

        if (!isValidChannelId(channelId)) {
            MessageUtils.sendErrorMessage(sender, "ID de channel Discord invalide ! Format attendu : 123456789012345678");
            return;
        }

        plugin.getConfig().set("discord.live-channel-id", channelId);
        plugin.saveConfig();
        plugin.getDiscordConfig().setLiveChannelId(channelId);

        MessageUtils.sendSuccessMessage(sender, "Channel Discord configuré : ", MessageUtils.highlight(channelId, BrandUtils.PRIMARY));
        MessageUtils.sendSecondaryMessage(sender, "Vous pouvez maintenant utiliser /live <message> pour envoyer des messages !");

        discordBot.sendLiveMessage("Configuration du channel réussie !", "System", (success, error) -> {
            if (success) {
                MessageUtils.sendSuccessMessage(sender, "Test d'envoi réussi !");
            } else {
                MessageUtils.sendErrorMessage(sender, "Attention : Le test d'envoi a échoué. Vérifiez que le bot Discord a accès à ce channel.");
            }
        });
    }

    private boolean isValidChannelId(String channelId) {
        if (channelId.length() < 17 || channelId.length() > 19) {
            return false;
        }
        try {
            Long.parseLong(channelId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
