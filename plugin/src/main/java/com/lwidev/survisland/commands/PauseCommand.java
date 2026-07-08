package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.services.PauseManager;
import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class PauseCommand extends SurvislandCommand {

    public PauseCommand(PauseManager pauseManager) {
        super("pause", "Activer ou désactiver la pause du jeu", PermissionDefault.OP);

        executes(ctx -> {
            boolean paused = pauseManager.toggle();
            CommandSender sender = ctx.getSource().getSender();
            MessageUtils.sendSuccessMessage(sender, "Partie ", MessageUtils.highlight(paused ? "mise en pause" : "reprise", BrandUtils.SECONDARY), ".");
            return Command.SINGLE_SUCCESS;
        });
    }
}
