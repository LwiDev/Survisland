package com.lwidev.survisland.commands;

import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.confess.LinkCodeManager;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.PluralUtils;
import com.mojang.brigadier.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
            MessageUtils.sendSuccessMessage(sender, "Vous avez déjà un code en attente : ", bold(existingCode, BrandUtils.SECONDARY));
            MessageUtils.sendSecondaryMessage(sender, "Allez sur Discord dans votre salon confess-xxx et tapez : ", MessageUtils.highlight("/verify " + existingCode, BrandUtils.TERTIARY));
            MessageUtils.sendSecondaryMessage(sender, "Ce code expire dans ", MessageUtils.highlight(PluralUtils.withCount(linkCodeManager.getCodeExpiryMinutes(), "minute"), BrandUtils.SECONDARY), ".");
            return;
        }

        String code = linkCodeManager.generateLinkCode(playerName);

        if (code == null) {
            MessageUtils.sendErrorMessage(sender, "Erreur lors de la génération du code de liaison !");
            return;
        }

        MessageUtils.sendSuccessMessage(sender, "Code de liaison généré : ", bold(code, BrandUtils.SECONDARY));
        MessageUtils.sendMessage(sender, divider());
        MessageUtils.sendSecondaryMessage(sender, bold("1.", BrandUtils.PRIMARY), " Allez sur Discord dans votre salon ", MessageUtils.highlight("confess-xxx", BrandUtils.TERTIARY));
        MessageUtils.sendSecondaryMessage(sender, bold("2.", BrandUtils.PRIMARY), " Tapez la commande : ", MessageUtils.highlight("/verify " + code, BrandUtils.TERTIARY));
        MessageUtils.sendSecondaryMessage(sender, bold("3.", BrandUtils.PRIMARY), " Une fois validé, vous pourrez utiliser ", MessageUtils.highlight("/confess", BrandUtils.TERTIARY), " !");
        MessageUtils.sendMessage(sender, "");
        MessageUtils.sendErrorMessage(sender, "⚠ Ce code expire dans ", bold(PluralUtils.withCount(linkCodeManager.getCodeExpiryMinutes(), "minute"), NamedTextColor.RED), " !");
        MessageUtils.sendMessage(sender, divider());
    }

    private static Component bold(String text, TextColor color) {
        return Component.text(text, color).decorate(TextDecoration.BOLD);
    }

    private static Component divider() {
        return Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH);
    }
}
