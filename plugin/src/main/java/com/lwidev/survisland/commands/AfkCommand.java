package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.services.AfkManager;
import com.mojang.brigadier.Command;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class AfkCommand extends SurvislandCommand {

    public AfkCommand(Survisland plugin, AfkManager afkManager) {
        super("afk", "Permet de se mettre afk", List.of(), true, PermissionDefault.TRUE);

        executes(ctx -> {
            Player player = (Player) ctx.getSource().getSender();
            afkManager.startAfk(player);
            return Command.SINGLE_SUCCESS;
        });
    }
}
