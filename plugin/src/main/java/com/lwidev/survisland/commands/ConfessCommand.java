package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.confess.ConfessLinkManager;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.utils.MessageUtils;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ConfessCommand implements CommandExecutor, TabCompleter {
    
    private final Survisland plugin;
    private final EmbeddedDiscordBot discordBot;
    private final ConfessLinkManager confessLinkManager;
    
    public ConfessCommand(Survisland plugin, EmbeddedDiscordBot discordBot, ConfessLinkManager confessLinkManager) {
        this.plugin = plugin;
        this.discordBot = discordBot;
        this.confessLinkManager = confessLinkManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!sender.hasPermission("survisland.confess.send")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "Cette commande ne peut être utilisée que par un joueur !");
            return true;
        }
        
        Player player = (Player) sender;
        String playerName = player.getName();
        
        // Vérifier si le joueur a un confess lié AVANT de vérifier les arguments
        String channelName = confessLinkManager.getPlayerChannel(playerName);
        if (channelName == null) {
            MessageUtils.sendErrorMessage(sender, "Vous n'aviez lié aucun confess à votre compte !");
            MessageUtils.sendSecondaryMessage(sender, "Utilisez /link pour générer un code sur Minecraft, puis /verify <code> sur Discord.");
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /confess <message> ou /conf <message>");
            return true;
        }
        
        // Vérifier que le bot Discord est connecté
        if (!discordBot.isConnected()) {
            MessageUtils.sendErrorMessage(sender, "Le bot Discord n'est pas connecté !");
            return true;
        }
        
        // Trouver le channel Discord par son nom
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
            return true;
        }
        
        // Reconstruct message from args
        String message = String.join(" ", args);
        String channelId = targetChannel.getId();
        
        discordBot.sendConfessMessage(channelId, message, playerName, (success, error) -> {
            if (success) {
                MessageUtils.sendSuccessMessage(sender, "§7Message envoyé : §f" + message);
            } else {
                MessageUtils.sendErrorMessage(sender, "Erreur lors de l'envoi du message : " + (error != null ? error : "Erreur inconnue"));
            }
        });
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("Votre message confidentiel...");
        }
        
        return completions;
    }
}