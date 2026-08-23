package com.lwidev.survisland.commands;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.command.SurvislandCommand;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.SoundUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class SayCommand extends SurvislandCommand {

    private static final String DEFAULT_FORMAT = "&f%player% &7: &c%message%";

    public SayCommand(Survisland plugin) {
        super("say", "Envoyer un message à tous les joueurs dans une forme particulière", PermissionDefault.OP);

        argument("message", StringArgumentType.greedyString(), "le message à envoyer", ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            String message = StringArgumentType.getString(ctx, "message");
            String format = plugin.getConfig().getString("say.format", DEFAULT_FORMAT);
            String formatted = format.replace("%player%", sender.getName()).replace("%message%", message);

            Component component = MessageUtils.colorize(formatted);
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), component);
            Bukkit.getOnlinePlayers().forEach(recipient -> MessageUtils.sendMessage(recipient, component));

            String soundName = plugin.getConfig().getString("say.sound", "");
            if (!soundName.isBlank()) {
                Sound sound = SoundUtils.fromConfigName(soundName);
                if (sound != null) {
                    Bukkit.getOnlinePlayers().forEach(recipient -> SoundUtils.play(recipient, sound));
                }
            }
            return Command.SINGLE_SUCCESS;
        });
    }
}
