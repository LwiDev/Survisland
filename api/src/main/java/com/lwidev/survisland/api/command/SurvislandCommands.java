package com.lwidev.survisland.api.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers {@link SurvislandCommand}s via Paper's Brigadier lifecycle API and
 * auto-registers their (and their subcommands') permission nodes with Bukkit.
 * No plugin.yml command/permission declaration needed.
 */
public final class SurvislandCommands {

    private SurvislandCommands() {
    }

    public static void register(JavaPlugin plugin, SurvislandCommand... commands) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (SurvislandCommand command : commands) {
            registerPermission(pluginManager, command.permission(), command.permissionDefault());
            for (String sub : command.subcommandNames()) {
                registerPermission(pluginManager, command.permission() + "." + sub, command.permissionDefault());
            }
        }

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            for (SurvislandCommand command : commands) {
                LiteralCommandNode<CommandSourceStack> node = command.buildNode();
                event.registrar().register(node, command.description(), command.aliases());
            }
        });
    }

    private static void registerPermission(PluginManager pluginManager, String node, org.bukkit.permissions.PermissionDefault permissionDefault) {
        if (pluginManager.getPermission(node) == null) {
            pluginManager.addPermission(new Permission(node, permissionDefault));
        }
    }
}
