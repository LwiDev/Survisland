package com.lwidev.survisland.chatspec;

import com.lwidev.survisland.Survisland;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatSpecManager implements Listener {
    
    private final Survisland plugin;
    private final Set<Player> spectatorChatEnabled;
    private final String prefix;
    private final NamedTextColor color;
    private boolean enabled;
    
    public ChatSpecManager(Survisland plugin) {
        this.plugin = plugin;
        this.spectatorChatEnabled = ConcurrentHashMap.newKeySet();
        
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
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        
        // Check if player is in spectator mode
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }
        
        // Cancel the original event
        event.setCancelled(true);
        
        // Format spectator message
        Component message = formatSpectatorMessage(player, event.getMessage());
        
        // Send to all spectators and players with permission
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (canReceiveSpectatorChat(onlinePlayer)) {
                onlinePlayer.sendMessage(message);
            }
        }
        
        // Send to console
        plugin.getLogger().info("[SPECTATOR CHAT] " + player.getName() + ": " + event.getMessage());
    }
    
    private Component formatSpectatorMessage(Player player, String message) {
        return Component.text(prefix + " ", color)
            .append(Component.text(player.getName(), NamedTextColor.WHITE))
            .append(Component.text(": ", NamedTextColor.WHITE))
            .append(Component.text(message, color));
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
    
    public void toggleSpectatorChat(Player player) {
        if (spectatorChatEnabled.contains(player)) {
            spectatorChatEnabled.remove(player);
            player.sendMessage(Component.text("Chat spectateur désactivé", NamedTextColor.RED));
        } else {
            spectatorChatEnabled.add(player);
            player.sendMessage(Component.text("Chat spectateur activé", NamedTextColor.GREEN));
        }
    }
    
    public boolean isSpectatorChatEnabled(Player player) {
        return spectatorChatEnabled.contains(player);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("chatspec.enabled", enabled);
        plugin.saveConfig();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void reload() {
        // Reload configuration
        plugin.reloadConfig();
        
        this.enabled = plugin.getConfig().getBoolean("chatspec.enabled", true);
    }
}