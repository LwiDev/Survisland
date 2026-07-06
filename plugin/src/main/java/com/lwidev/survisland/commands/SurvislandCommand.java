package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.skins.MinecraftHeadsAPI;
import com.lwidev.survisland.skins.SkinManager;
import com.lwidev.survisland.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SurvislandCommand implements CommandExecutor, TabCompleter {
    
    private final Survisland plugin;
    private final SkinManager skinManager;
    private final MinecraftHeadsAPI headsAPI;
    
    public SurvislandCommand(Survisland plugin) {
        this.plugin = plugin;
        this.skinManager = plugin.getSkinManager();
        this.headsAPI = new MinecraftHeadsAPI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!sender.hasPermission("survisland.use")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "skin" -> handleSkinCommands(sender, args);
            default -> showHelp(sender);
        }
        
        return true;
    }
    
    private void handleSkinCommands(CommandSender sender, String[] args) {
        if (args.length == 1) {
            showSkinHelp(sender);
            return;
        }
        
        String skinSubCommand = args[1].toLowerCase();
        
        switch (skinSubCommand) {
            case "force" -> handleSkinForce(sender, args);
            case "restore" -> handleSkinRestore(sender, args);
            case "all" -> handleSkinAll(sender, args);
            case "list" -> handleSkinList(sender);
            // case "head" -> handleSkinHead(sender, args);
            // case "search" -> handleSkinSearch(sender, args);
            default -> showSkinHelp(sender);
        }
    }
    
    private void showHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "§e=== Survisland Plugin ===");
        MessageUtils.sendMessage(sender, "§eCommandes disponibles :");
        MessageUtils.sendMessage(sender, "§e  /surv action menu §7- Interface graphique pour gérer les actions");
        MessageUtils.sendMessage(sender, "§e  /surv skin force <joueur> <skin> §7- Forcer un skin à un joueur");
        MessageUtils.sendMessage(sender, "§e  /live <message> §7- Envoyer un message sur Discord");
        MessageUtils.sendMessage(sender, "§e  /setlive <channel_id> §7- Configurer le salon Discord cible");
    }
    
    private void showActionHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "§e=== Actions Survisland ===");
        MessageUtils.sendMessage(sender, "§e  /surv action menu §7- Interface graphique pour gérer");
        MessageUtils.sendMessage(sender, "§e  /surv action add <id> <commandes> §7- Créer une action");
        MessageUtils.sendSecondaryMessage(sender, "     Exemple : /surv action add heal \"/heal @s,/tell @s Soigné !\"");
        MessageUtils.sendMessage(sender, "§e  /surv action remove <id> §7- Supprimer une action");
        MessageUtils.sendMessage(sender, "§e  /surv action modify <id> §7- Modifier via interface");
        MessageUtils.sendMessage(sender, "§e  /surv action grant <id> <joueur> §7- Donner permission");
        MessageUtils.sendMessage(sender, "§e  /surv action revoke <id> <joueur> §7- Retirer permission");
    }
    
    private void showSkinHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "§e=== Skins Survisland ===");
        MessageUtils.sendMessage(sender, "§e  /surv skin force <joueur> <skin> §7- Forcer un skin à un joueur");
        MessageUtils.sendMessage(sender, "§e  /surv skin restore <joueur> §7- Restaurer le skin original");
        MessageUtils.sendMessage(sender, "§e  /surv skin all <skin> §7- Appliquer un skin à tous");
        MessageUtils.sendMessage(sender, "§e  /surv skin list §7- Lister les skins forcés");
        MessageUtils.sendMessage(sender, "§e  /surv skin head <catégorie> [numéro] §7- Appliquer une tête décorative");
        MessageUtils.sendMessage(sender, "§e  /surv skin search <termes> §7- Rechercher des têtes");
        MessageUtils.sendSecondaryMessage(sender, "Skins forcés actifs : §e" + skinManager.getAllForcedSkins().size());
    }
    
    private List<String> parseCommands(String input) {
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
    
    private void handleSkinForce(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survisland.skins.force")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission de forcer des skins !");
            return;
        }
        
        if (args.length < 4) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /surv skin force <joueur> <skin>");
            MessageUtils.sendSecondaryMessage(sender, "Exemple : /surv skin force Notch Steve");
            return;
        }
        
        String playerName = args[2];
        String skinName = args[3];
        
        skinManager.forceSkin(playerName, skinName);
        MessageUtils.sendSuccessMessage(sender, "Skin '" + skinName + "' forcé pour " + playerName + " !");
    }
    
    private void handleSkinRestore(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survisland.skins.force")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission de restaurer des skins !");
            return;
        }
        
        if (args.length < 3) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /surv skin restore <joueur>");
            return;
        }
        
        String playerName = args[2];
        
        if (!skinManager.hasForcedSkin(playerName)) {
            MessageUtils.sendErrorMessage(sender, "Aucun skin forcé trouvé pour " + playerName + " !");
            return;
        }
        
        skinManager.removeForcedSkin(playerName);
        MessageUtils.sendSuccessMessage(sender, "Skin original restauré pour " + playerName + " !");
    }
    
    private void handleSkinAll(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survisland.skins.admin")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'appliquer des skins à tous !");
            return;
        }
        
        if (args.length < 3) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /surv skin all <skin>");
            MessageUtils.sendSecondaryMessage(sender, "Utilise 'restore' pour restaurer tous les skins");
            return;
        }
        
        String skinName = args[2];
        
        if (skinName.equalsIgnoreCase("restore")) {
            skinManager.restoreAllSkins();
            MessageUtils.sendSuccessMessage(sender, "Skins originaux restaurés pour tous les joueurs !");
        } else {
            skinManager.applySkinToAll(skinName);
            MessageUtils.sendSuccessMessage(sender, "Skin '" + skinName + "' appliqué à tous les joueurs connectés !");
        }
    }
    
    private void handleSkinList(CommandSender sender) {
        if (!sender.hasPermission("survisland.skins.admin")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission de voir la liste des skins !");
            return;
        }
        
        var forcedSkins = skinManager.getAllForcedSkins();
        
        if (forcedSkins.isEmpty()) {
            MessageUtils.sendInfoMessage(sender, "Aucun skin forcé actuellement");
            return;
        }
        
        MessageUtils.sendMessage(sender, "§e=== Skins Forcés ===");
        forcedSkins.forEach((player, skin) -> {
            MessageUtils.sendMessage(sender, "§e  " + player + " §7» §f" + skin);
        });
    }
    
    private void handleSkinHead(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survisland.skins.force")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'appliquer des têtes !");
            return;
        }
        
        if (!(sender instanceof Player player)) {
            MessageUtils.sendErrorMessage(sender, "Cette commande est réservée aux joueurs !");
            return;
        }
        
        if (args.length < 3) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /surv skin head <catégorie> [numéro]");
            MessageUtils.sendSecondaryMessage(sender, "Catégories : " + String.join(", ", MinecraftHeadsAPI.CATEGORIES));
            return;
        }
        
        String category = args[2].toLowerCase();
        
        if (!MinecraftHeadsAPI.isValidCategory(category)) {
            MessageUtils.sendErrorMessage(sender, "Catégorie invalide !");
            MessageUtils.sendSecondaryMessage(sender, "Catégories : " + String.join(", ", MinecraftHeadsAPI.CATEGORIES));
            return;
        }
        
        int index = 0;
        if (args.length > 3) {
            try {
                index = Integer.parseInt(args[3]) - 1; // Convert to 0-based index
                if (index < 0) index = 0;
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(sender, "Numéro invalide !");
                return;
            }
        }
        
        MessageUtils.sendInfoMessage(sender, "Recherche des têtes dans la catégorie " + category + "...");

        int finalIndex = index;
        headsAPI.getHeadsByCategory(category).whenComplete((heads, throwable) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (throwable != null) {
                    MessageUtils.sendErrorMessage(sender, "Erreur lors de la récupération : " + throwable.getMessage());
                    return;
                }
                
                if (heads.isEmpty()) {
                    MessageUtils.sendErrorMessage(sender, "Aucune tête trouvée dans cette catégorie !");
                    return;
                }
                
                if (finalIndex >= heads.size()) {
                    MessageUtils.sendErrorMessage(sender, "Numéro trop élevé ! Têtes disponibles : " + heads.size());
                    return;
                }
                
                MinecraftHeadsAPI.HeadData head = heads.get(finalIndex);
                String playerName = player.getName();
                
                // Apply the head skin with proper signature using SkinManager directly
                skinManager.forceHeadSkin(playerName, head.getTexture(), head.getSignature());
                MessageUtils.sendSuccessMessage(sender, "Tête '" + head.getDisplayName() + "' appliquée !");
                
                if (heads.size() > 1) {
                    MessageUtils.sendSecondaryMessage(sender, "Têtes dans cette catégorie : " + heads.size() + " (utilisez /surv skin head " + category + " <1-" + heads.size() + ">)");
                }
            });
        });
    }
    
    private void handleSkinSearch(CommandSender sender, String[] args) {
        if (!sender.hasPermission("survisland.skins.force")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission de rechercher des têtes !");
            return;
        }
        
        if (args.length < 3) {
            MessageUtils.sendErrorMessage(sender, "Utilisation : /surv skin search <termes de recherche>");
            MessageUtils.sendSecondaryMessage(sender, "Exemple : /surv skin search dragon");
            return;
        }
        
        String searchTerm = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        
        MessageUtils.sendInfoMessage(sender, "Recherche de têtes avec le terme « " + searchTerm + " »...");
        
        headsAPI.searchHeads(searchTerm).whenComplete((heads, throwable) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (throwable != null) {
                    MessageUtils.sendErrorMessage(sender, "Erreur lors de la recherche : " + throwable.getMessage());
                    return;
                }
                
                if (heads.isEmpty()) {
                    MessageUtils.sendErrorMessage(sender, "Aucune tête trouvée pour « " + searchTerm + " » !");
                    MessageUtils.sendSecondaryMessage(sender, "Essayez des termes différents ou utilisez /surv skin head <catégorie>");
                    return;
                }
                
                MessageUtils.sendMessage(sender, "§e=== Résultats de recherche ===");
                MessageUtils.sendSecondaryMessage(sender, "Trouvé " + heads.size() + " tête(s) pour « " + searchTerm + " » :");
                
                for (int i = 0; i < Math.min(10, heads.size()); i++) {
                    MinecraftHeadsAPI.HeadData head = heads.get(i);
                    String category = head.getCategory().isEmpty() ? "inconnu" : head.getCategory();
                    MessageUtils.sendMessage(sender, "§e  " + (i + 1) + ". §f" + head.getDisplayName() + " §7(" + category + ")");
                }
                
                if (heads.size() > 10) {
                    MessageUtils.sendSecondaryMessage(sender, "... et " + (heads.size() - 10) + " autre(s)");
                }
                
                if (sender instanceof Player) {
                    MessageUtils.sendSecondaryMessage(sender, "Cliquez sur un résultat ou utilisez une catégorie pour appliquer");
                }
            });
        });
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("skin");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("skin")) {
            completions.addAll(List.of("force", "restore", "all", "list"));
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("skin")) {
                String subCmd = args[1].toLowerCase();
                if (List.of("force", "restore").contains(subCmd)) {
                    // Add online player names
                    plugin.getServer().getOnlinePlayers().forEach(player -> 
                        completions.add(player.getName()));
                } else if (subCmd.equals("all")) {
                    completions.addAll(List.of("Steve", "Alex", "restore"));
                } else if (subCmd.equals("head")) {
                    // Add head categories
                    completions.addAll(List.of(MinecraftHeadsAPI.CATEGORIES));
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("skin") && args[1].equalsIgnoreCase("force")) {
            // Suggest common skin names
            completions.addAll(List.of("Steve", "Alex", "Notch", "Herobrine"));
        }
        
        return completions;
    }
}