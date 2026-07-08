package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.menu.MainMenu;
import com.lwidev.survisland.menu.MenuContext;
import com.mojang.brigadier.Command;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class MenuCommand extends SurvislandCommand {

    public MenuCommand(MenuContext context) {
        super("menu", "Ouvrir le centre de contrôle (équipes, joueurs, partie)", List.of(), true, PermissionDefault.OP);
        executes(ctx -> {
            Player player = (Player) ctx.getSource().getSender();
            new MainMenu(player, context).open();
            return Command.SINGLE_SUCCESS;
        });
    }
}
