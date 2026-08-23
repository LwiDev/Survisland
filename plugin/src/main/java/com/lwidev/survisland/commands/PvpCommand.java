package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class PvpCommand extends SurvislandCommand {

    public PvpCommand() {
        super("pvp", "Activer ou désactiver le PvP", PermissionDefault.OP);

        executes(ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            boolean enabled = Bukkit.getWorlds().stream().anyMatch(world -> world.getGameRuleValue(GameRules.PVP));
            MessageUtils.sendInfoMessage(sender, "Le PvP est actuellement ", status(enabled), ".");
            return Command.SINGLE_SUCCESS;
        });

        onLiteral("on", ctx -> setPvp(ctx.getSource().getSender(), true));
        onLiteral("off", ctx -> setPvp(ctx.getSource().getSender(), false));
    }

    private int setPvp(CommandSender sender, boolean enabled) {
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRules.PVP, enabled));
        MessageUtils.sendSuccessMessage(sender, "PvP ", status(enabled), ".");
        return Command.SINGLE_SUCCESS;
    }

    private static Component status(boolean enabled) {
        return MessageUtils.highlight(enabled ? "activé" : "désactivé", enabled ? NamedTextColor.GREEN : NamedTextColor.RED);
    }
}
