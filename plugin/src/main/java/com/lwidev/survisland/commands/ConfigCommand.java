package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import org.bukkit.permissions.PermissionDefault;

public class ConfigCommand extends SurvislandCommand {

    public ConfigCommand(Survisland plugin) {
        super("config", "Gérer la configuration du plugin", PermissionDefault.OP);

        subcommand("reload").executes(ctx -> {
            plugin.reloadPluginConfig();
            MessageUtils.sendSuccessMessage(ctx.getSource().getSender(), "La configuration a été ", MessageUtils.highlight("rechargée"), ".");
            return Command.SINGLE_SUCCESS;
        });
    }
}
