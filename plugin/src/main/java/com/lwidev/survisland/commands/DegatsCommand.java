package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.services.DamageManager;
import com.mojang.brigadier.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class DegatsCommand extends SurvislandCommand {

    public DegatsCommand(DamageManager damageManager) {
        super("degats", "Activer ou désactiver les dégâts (invulnérabilité des joueurs)", PermissionDefault.OP);

        executes(ctx -> {
            MessageUtils.sendInfoMessage(ctx.getSource().getSender(), "Les dégâts sont actuellement ", status(damageManager.isEnabled()), ".");
            return Command.SINGLE_SUCCESS;
        });

        onLiteral("on", ctx -> setEnabled(ctx.getSource().getSender(), damageManager, true));
        onLiteral("off", ctx -> setEnabled(ctx.getSource().getSender(), damageManager, false));
    }

    private int setEnabled(CommandSender sender, DamageManager damageManager, boolean enabled) {
        damageManager.setEnabled(enabled);
        MessageUtils.sendSuccessMessage(sender, "Dégâts ", status(enabled), ".");
        return Command.SINGLE_SUCCESS;
    }

    private static Component status(boolean enabled) {
        return MessageUtils.highlight(enabled ? "activés" : "désactivés", enabled ? NamedTextColor.GREEN : NamedTextColor.RED);
    }
}
