package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.utils.PauseManager;
import com.mojang.brigadier.Command;
import org.bukkit.permissions.PermissionDefault;

public class PauseCommand extends SurvislandCommand {

    public PauseCommand(Survisland plugin) {
        super("pause", "Activer ou désactiver la pause du jeu", PermissionDefault.OP);

        executes(ctx -> {
            PauseManager.toggle(plugin);
            return Command.SINGLE_SUCCESS;
        });
    }
}
