package com.lwidev.survisland.skins;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.lwidev.survisland.Survisland;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkinApplier {
    
    private final Survisland plugin;
    
    public SkinApplier(Survisland plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("SkinApplier initialized with Paper API");
    }
    
    public boolean applySkin(Player player, SkinData skinData) {
        try {
            // Create PlayerProfile using Paper API
            PlayerProfile profile = player.getPlayerProfile();
            
            // Clear existing texture properties
            profile.removeProperty("textures");
            
            // Add new texture property
            String signature = skinData.getSignature();
            ProfileProperty textureProperty = new ProfileProperty(
                "textures", 
                skinData.getTexture(), 
                signature.isEmpty() ? null : signature
            );
            profile.setProperty(textureProperty);
            
            // Apply the profile back to player
            player.setPlayerProfile(profile);
            
            // Refresh the player's appearance
            refreshPlayerSkin(player);
            
            plugin.getLogger().info("Applied skin from " + skinData.getPlayerName() + " to " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply skin to " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean removeSkin(Player player) {
        try {
            // Get player profile using Paper API
            PlayerProfile profile = player.getPlayerProfile();
            
            // Remove texture properties
            profile.removeProperty("textures");
            
            // Apply the profile back to player
            player.setPlayerProfile(profile);
            
            // Refresh the player's appearance
            refreshPlayerSkin(player);
            
            plugin.getLogger().info("Removed forced skin from " + player.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove skin from " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void refreshPlayerSkin(Player player) {
        // Force player respawn to synchronize skin with all players
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                // Method: Force player "respawn" by making them leave and rejoin visually
                // This is what forces the skin to sync properly
                
                // Force disconnect/reconnect packets for all viewers
                player.getWorld().getPlayers().forEach(viewer -> {
                    if (!viewer.equals(player)) {
                        // This should force a complete entity refresh
                        viewer.hidePlayer(plugin, player);
                        
                        // Re-show after longer delay to allow profile sync
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            viewer.showPlayer(plugin, player);
                        }, 20L); // 1 second delay
                    }
                });
                
                plugin.getLogger().info("Forced skin respawn for " + player.getName());
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to refresh skin: " + e.getMessage());
            }
        }, 5L);
    }
}