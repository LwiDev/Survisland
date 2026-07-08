package com.lwidev.survisland.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

/**
 * Sends prefixed chat messages built from a mix of plain strings (rendered in the message's neutral
 * base color) and {@link Component}s (rendered as given) — use {@link #highlight(String)} for the one
 * or two words that actually matter in a sentence instead of coloring the whole line, e.g.:
 * {@code sendSuccessMessage(player, "Il fait maintenant ", highlight("nuit", BrandUtils.SECONDARY), ".")}.
 * Errors are the one exception: they stay fully red, since the whole line being an error is the point.
 */
public class MessageUtils {

    private static final Component PREFIX = Component.text("[Survisland] ", BrandUtils.PRIMARY);

    public static void sendMessage(CommandSender sender, Object... parts) {
        sender.sendMessage(PREFIX.append(build(null, parts)));
    }

    public static void sendSuccessMessage(CommandSender sender, Object... parts) {
        sender.sendMessage(PREFIX.append(build(NamedTextColor.GRAY, parts)));
    }

    public static void sendErrorMessage(CommandSender sender, Object... parts) {
        sender.sendMessage(PREFIX.append(build(NamedTextColor.RED, parts)));
    }

    public static void sendInfoMessage(CommandSender sender, Object... parts) {
        sender.sendMessage(PREFIX.append(build(NamedTextColor.GRAY, parts)));
    }

    public static void sendSecondaryMessage(CommandSender sender, Object... parts) {
        sender.sendMessage(PREFIX.append(build(NamedTextColor.GRAY, parts)));
    }

    /** A word or phrase to make stand out in an otherwise neutral sentence, in the brand's primary accent. */
    public static Component highlight(String text) {
        return highlight(text, BrandUtils.PRIMARY);
    }

    /** A word or phrase to make stand out in an otherwise neutral sentence, in the given color. */
    public static Component highlight(String text, TextColor color) {
        return Component.text(text, color);
    }

    private static Component build(TextColor base, Object[] parts) {
        Component line = base == null ? Component.empty() : Component.text("", base);
        for (Object part : parts) {
            line = line.append(part instanceof Component component ? component : Component.text(String.valueOf(part)));
        }
        return line;
    }
}
