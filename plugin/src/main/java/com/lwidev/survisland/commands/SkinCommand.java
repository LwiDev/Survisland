package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.skins.SkinManager;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;

public class SkinCommand extends SurvislandCommand {

    public SkinCommand(SkinManager skinManager) {
        super("skin", "Gérer les skins forcés", PermissionDefault.TRUE);

        subcommand("force")
                .argument("joueur", ArgumentTypes.player())
                .argument("skin", StringArgumentType.greedyString(), ctx -> {
                    Player target = resolvePlayer(ctx, "joueur");
                    String skin = StringArgumentType.getString(ctx, "skin");
                    skinManager.forceSkin(target.getName(), skin);
                    MessageUtils.sendSuccessMessage(ctx.getSource().getSender(),
                            "Skin '" + skin + "' forcé pour " + target.getName() + " !");
                    return Command.SINGLE_SUCCESS;
                });

        subcommand("restore")
                .argument("joueur", ArgumentTypes.player(), ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Player target = resolvePlayer(ctx, "joueur");
                    if (!skinManager.getAllForcedSkins().containsKey(target.getName())) {
                        MessageUtils.sendErrorMessage(sender, "Aucun skin forcé trouvé pour " + target.getName() + " !");
                        return Command.SINGLE_SUCCESS;
                    }
                    skinManager.removeForcedSkin(target.getName());
                    MessageUtils.sendSuccessMessage(sender, "Skin original restauré pour " + target.getName() + " !");
                    return Command.SINGLE_SUCCESS;
                });

        subcommand("all")
                .onLiteral("restore", ctx -> {
                    skinManager.restoreAllSkins();
                    MessageUtils.sendSuccessMessage(ctx.getSource().getSender(), "Skins originaux restaurés pour tous les joueurs !");
                    return Command.SINGLE_SUCCESS;
                })
                .onArgument("skin", StringArgumentType.greedyString(), ctx -> {
                    String skin = StringArgumentType.getString(ctx, "skin");
                    skinManager.applySkinToAll(skin);
                    MessageUtils.sendSuccessMessage(ctx.getSource().getSender(),
                            "Skin '" + skin + "' appliqué à tous les joueurs connectés !");
                    return Command.SINGLE_SUCCESS;
                });

        subcommand("list")
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Map<String, String> forcedSkins = skinManager.getAllForcedSkins();

                    if (forcedSkins.isEmpty()) {
                        MessageUtils.sendInfoMessage(sender, "Aucun skin forcé actuellement");
                        return Command.SINGLE_SUCCESS;
                    }

                    MessageUtils.sendMessage(sender, "§e=== Skins Forcés ===");
                    forcedSkins.forEach((playerName, skin) ->
                            MessageUtils.sendMessage(sender, "§e  " + playerName + " §7» §f" + skin));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
