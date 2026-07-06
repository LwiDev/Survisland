package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.utils.CompassTask;
import com.lwidev.survisland.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CampCommand implements CommandExecutor, TabCompleter {

    private final Survisland plugin;

    public CampCommand(Survisland plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("survisland.camp.use")) {
            MessageUtils.sendErrorMessage(sender, "Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtils.sendErrorMessage(sender, "Cette commande ne peut être utilisée que par un joueur !");
            return true;
        }
        Location bedSpawnLocation = player.getBedSpawnLocation();
        if (bedSpawnLocation == null) {
            MessageUtils.sendErrorMessage(player, "Vous n'avez pas de campement défini !");
            MessageUtils.sendSecondaryMessage(player, "Dormez dans un lit pour définir votre point de spawn.");
            return true;
        }
        CompassTask.start(plugin, player, bedSpawnLocation);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>(); // Aucune auto-complétion nécessaire
    }
}