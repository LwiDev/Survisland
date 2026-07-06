package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.confess.LinkCodeManager;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class LinkCommand extends SurvislandCommand {

    private final LinkCodeManager linkCodeManager;

    public LinkCommand(LinkCodeManager linkCodeManager) {
        super("link", "Générer un code pour lier votre compte au confess Discord", List.of(), true, PermissionDefault.TRUE);
        this.linkCodeManager = linkCodeManager;

        executes(ctx -> {
            Player player = (Player) ctx.getSource().getSender();
            generateLink(player);
            return Command.SINGLE_SUCCESS;
        });
    }

    private void generateLink(Player player) {
        CommandSender sender = player;
        String playerName = player.getName();

        if (linkCodeManager.hasValidCode(playerName)) {
            String existingCode = linkCodeManager.getPlayerCode(playerName);
            MessageUtils.sendSuccessMessage(sender, "§7Vous avez déjà un code en attente : §f§l" + existingCode);
            MessageUtils.sendSecondaryMessage(sender, "Allez sur Discord dans votre salon confess-xxx et tapez : §f/verify " + existingCode);
            MessageUtils.sendSecondaryMessage(sender, "§7Ce code expire dans " + linkCodeManager.getCodeExpiryMinutes() + " minutes.");
            return;
        }

        String code = linkCodeManager.generateLinkCode(playerName);

        if (code == null) {
            MessageUtils.sendErrorMessage(sender, "Erreur lors de la génération du code de liaison !");
            return;
        }

        MessageUtils.sendSuccessMessage(sender, "§7Code de liaison généré : §f§l" + code);
        MessageUtils.sendMessage(sender, "§8§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        MessageUtils.sendMessage(sender, "§6§l1.§r §7Allez sur Discord dans votre salon §fconfess-xxx");
        MessageUtils.sendMessage(sender, "§6§l2.§r §7Tapez la commande : §f/verify " + code);
        MessageUtils.sendMessage(sender, "§6§l3.§r §7Une fois validé, vous pourrez utiliser §f/confess§7 !");
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendMessage(sender, "§c⚠ §7Ce code expire dans §c" + linkCodeManager.getCodeExpiryMinutes() + " minutes§7 !");
        MessageUtils.sendMessage(sender, "§8§m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}
