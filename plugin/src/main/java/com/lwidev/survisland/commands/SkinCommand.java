package com.lwidev.survisland.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.skins.SkinManager;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SkinCommand extends SurvislandCommand {

    private static final String TARGET_HINT = "un pseudo de joueur (en ligne ou non) ou un sélecteur (@a, @p, @r, @s)";

    public SkinCommand(SkinManager skinManager) {
        super("skin", "Gérer les skins forcés", PermissionDefault.TRUE);

        subcommand("force")
                .argument("joueurs", ArgumentTypes.playerProfiles(), TARGET_HINT)
                .argument("skin", StringArgumentType.greedyString(),
                        "un pseudo de joueur existant (ex : GoldVision98) ou une texture base64 (copiée depuis minecraft-heads.com)",
                        ctx -> {
                            List<String> targets = names(resolvePlayerProfiles(ctx, "joueurs"));
                            String skin = StringArgumentType.getString(ctx, "skin");
                            targets.forEach(target -> skinManager.forceSkin(target, skin));
                            MessageUtils.sendSuccessMessage(ctx.getSource().getSender(), "Skin '" + skin + "' forcé pour " + describe(targets) + " !");
                            return Command.SINGLE_SUCCESS;
                        });

        subcommand("restore")
                .argument("joueurs", ArgumentTypes.playerProfiles(), TARGET_HINT, ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    List<String> targets = names(resolvePlayerProfiles(ctx, "joueurs"));
                    Map<String, String> forcedSkins = skinManager.getAllForcedSkins();
                    List<String> restored = targets.stream().filter(forcedSkins::containsKey).toList();

                    if (restored.isEmpty()) {
                        MessageUtils.sendErrorMessage(sender, "Aucun skin forcé trouvé pour " + describe(targets) + " !");
                        return Command.SINGLE_SUCCESS;
                    }

                    restored.forEach(skinManager::removeForcedSkin);
                    MessageUtils.sendSuccessMessage(sender, "Skin original restauré pour " + describe(restored) + " !");
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
                    forcedSkins.forEach((playerName, skin) -> MessageUtils.sendMessage(sender, "§e  " + playerName + " §7» §f" + skin));
                    return Command.SINGLE_SUCCESS;
                });
    }

    private static List<String> names(Collection<PlayerProfile> profiles) {
        return profiles.stream().map(PlayerProfile::getName).filter(Objects::nonNull).toList();
    }

    private static String describe(List<String> names) {
        return String.join(", ", names);
    }
}
