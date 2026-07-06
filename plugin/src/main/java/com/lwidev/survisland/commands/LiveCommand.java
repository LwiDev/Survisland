package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LiveCommand implements CommandExecutor, TabCompleter {
    
    private final Survisland plugin;
    private final EmbeddedDiscordBot discordBot;
    
    public LiveCommand(Survisland plugin, EmbeddedDiscordBot discordBot) {
        this.plugin = plugin;
        this.discordBot = discordBot;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!sender.hasPermission("survisland.live.send")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /live <message>");
            return true;
        }
        
        // Reconstruct message from args
        String message = String.join(" ", args);
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
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("Votre message ici...");
        }
        
        return completions;
    }
}