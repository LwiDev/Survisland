package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.confess.LinkCodeManager;
import com.lwidev.survisland.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LinkCommand implements CommandExecutor, TabCompleter {
    
    private final Survisland plugin;
    private final LinkCodeManager linkCodeManager;
    
    public LinkCommand(Survisland plugin, LinkCodeManager linkCodeManager) {
        this.plugin = plugin;
        this.linkCodeManager = linkCodeManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!sender.hasPermission("survisland.confess.link")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "Cette commande ne peut être utilisée que par un joueur !");
            return true;
        }
        
        Player player = (Player) sender;
        String playerName = player.getName();

        // Vérifier si le joueur a déjà un code en attente
        if (linkCodeManager.hasValidCode(playerName)) {
            String existingCode = linkCodeManager.getPlayerCode(playerName);
            MessageUtils.sendSuccessMessage(sender, "§7Vous avez déjà un code en attente : §f§l" + existingCode);
            MessageUtils.sendSecondaryMessage(sender, "Allez sur Discord dans votre salon confess-xxx et tapez : §f/verify " + existingCode);
            MessageUtils.sendSecondaryMessage(sender, "§7Ce code expire dans " + linkCodeManager.getCodeExpiryMinutes() + " minutes.");
            return true;
        }
        
        // Générer un nouveau code
        String code = linkCodeManager.generateLinkCode(playerName);
        
        if (code == null) {
            MessageUtils.sendErrorMessage(sender, "Erreur lors de la génération du code de liaison !");
            return true;
        }
        
        // Afficher les instructions au joueur
        MessageUtils.sendSuccessMessage(sender, "§7Code de liaison généré : §f§l" + code);
        MessageUtils.sendMessage(sender, "§8§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        MessageUtils.sendMessage(sender, "§6§l1.§r §7Allez sur Discord dans votre salon §fconfess-xxx");
        MessageUtils.sendMessage(sender, "§6§l2.§r §7Tapez la commande : §f/verify " + code);
        MessageUtils.sendMessage(sender, "§6§l3.§r §7Une fois validé, vous pourrez utiliser §f/confess§7 !");
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "§c⚠ §7Ce code expire dans §c" + linkCodeManager.getCodeExpiryMinutes() + " minutes§7 !");
        MessageUtils.sendMessage(sender, "§8§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Pas d'arguments pour cette commande
        return new ArrayList<>();
    }
}