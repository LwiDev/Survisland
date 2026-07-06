package com.lwidev.survisland.api.utils;

import org.bukkit.command.CommandSender;

public class MessageUtils {
    
    private static final String PREFIX = "§6[Survisland] ";
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }
    
    public static void sendSuccessMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§a" + message);
    }
    
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§c" + message);
    }
    
    public static void sendInfoMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§e" + message);
    }
    
    public static void sendSecondaryMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§7" + message);
    }
}