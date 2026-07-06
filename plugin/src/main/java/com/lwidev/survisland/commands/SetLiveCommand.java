package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class SetLiveCommand implements CommandExecutor, TabCompleter {
    
    private final Survisland plugin;
    private final EmbeddedDiscordBot discordBot;
    
    public SetLiveCommand(Survisland plugin, EmbeddedDiscordBot discordBot) {
        this.plugin = plugin;
        this.discordBot = discordBot;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!sender.hasPermission("survisland.live.config")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }
        
        if (args.length == 0) {
            // Show current channel
            String currentChannel = plugin.getDiscordConfig().getLiveChannelId();
            
            if (currentChannel == null || currentChannel.isEmpty()) {
                MessageUtils.sendInfoMessage(sender, "Aucun channel Discord configuré.");
                MessageUtils.sendSecondaryMessage(sender, "Utilisez §e/setlive <channel_id>§7 pour configurer un channel.");
            } else {
                MessageUtils.sendSuccessMessage(sender, "Channel Discord actuel : §e" + currentChannel);
            }
            return true;
        }
        
        if (args.length != 1) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /setlive <channel_id>");
            return true;
        }
        
        String channelId = args[0];
        
        // Check if it's already the same channel
        String currentChannel = plugin.getDiscordConfig().getLiveChannelId();
        if (channelId.equals(currentChannel)) {
            MessageUtils.sendInfoMessage(sender, "C'est déjà le channel configuré !");
            return true;
        }
        
        // Validate channel ID format (Discord snowflake)
        if (!isValidChannelId(channelId)) {
            MessageUtils.sendErrorMessage(sender, "ID de channel Discord invalide ! Format attendu : 123456789012345678");
            return true;
        }
        
        // Update configuration
        plugin.getConfig().set("discord.live-channel-id", channelId);
        plugin.saveConfig();
        
        // Update runtime config
        plugin.getDiscordConfig().setLiveChannelId(channelId);
        
        MessageUtils.sendSuccessMessage(sender, "Channel Discord configuré : " + channelId);
        MessageUtils.sendSecondaryMessage(sender, "Vous pouvez maintenant utiliser /live <message> pour envoyer des messages !");
        
        // Test the channel
        discordBot.sendLiveMessage("Configuration du channel réussie !", "System", (success, error) -> {
            if (success) {
                MessageUtils.sendSuccessMessage(sender, "Test d'envoi réussi !");
            } else {
                MessageUtils.sendErrorMessage(sender, "Attention : Le test d'envoi a échoué. Vérifiez que le bot Discord a accès à ce channel.");
            }
        });
        
        return true;
    }
    
    private boolean isValidChannelId(String channelId) {
        // Discord snowflakes are 64-bit integers, typically 17-19 characters when as strings
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("123456789012345678");
            completions.add("<channel_id>");
        }
        
        return completions;
    }
}