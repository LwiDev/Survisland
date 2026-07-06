package com.lwidev.survisland.chatspec;

import com.lwidev.survisland.Survisland;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatSpecManager implements Listener {

    private final Survisland plugin;
    private final String prefix;
    private final NamedTextColor color;
    private final boolean enabled;

    public ChatSpecManager(Survisland plugin) {
        this.plugin = plugin;

        // Load configuration
        this.enabled = plugin.getConfig().getBoolean("chatspec.enabled", true);
        this.prefix = plugin.getConfig().getString("chatspec.prefix", "[SPEC]");

        String colorName = plugin.getConfig().getString("chatspec.color", "GRAY");
        this.color = NamedTextColor.NAMES.value(colorName.toLowerCase());

        // Register events if enabled
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();

        // Check if player is in spectator mode
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        // Cancel the original event
        event.setCancelled(true);

        // Format spectator message
        Component message = formatSpectatorMessage(player, event.message());

        // Send to all spectators and players with permission
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (canReceiveSpectatorChat(onlinePlayer)) {
                onlinePlayer.sendMessage(message);
            }
        }

        // Send to console
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        plugin.getLogger().info("[SPECTATOR CHAT] " + player.getName() + ": " + plainMessage);
    }

    private Component formatSpectatorMessage(Player player, Component message) {
        return Component.text(prefix + " ", color)
            .append(Component.text(player.getName(), NamedTextColor.WHITE))
            .append(Component.text(": ", NamedTextColor.WHITE))
            .append(message.colorIfAbsent(color));
    }

    private boolean canReceiveSpectatorChat(Player player) {
        // Spectators can always see spectator chat
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        // Players with permission can see spectator chat
        if (player.hasPermission("survisland.chatspec.receive")) {
            return true;
        }

        // OPs can see spectator chat
        return player.isOp();
    }
}
