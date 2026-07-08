package com.lwidev.survisland.skins;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.skin.MojangAPI;
import com.lwidev.survisland.api.skin.SkinApplier;
import com.lwidev.survisland.api.skin.SkinData;
import com.lwidev.survisland.api.utils.BrandUtils;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.lwidev.survisland.api.utils.Shutdownable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class SkinManager implements Listener, Shutdownable {
    
    private final Survisland plugin;
    private final Map<String, String> forcedSkins; // Player name -> Skin input (name or texture)
    private final Map<String, SkinData> skinCache; // Skin input -> SkinData cache
    private final Map<String, SkinData> originalSkins; // Player name -> Original SkinData
    private final MojangAPI mojangAPI;
    private final SkinApplier skinApplier;
    
    private boolean enabled;
    private long cacheDuration;
    private File skinDataFile;
    private FileConfiguration skinDataConfig;
    
    public SkinManager(Survisland plugin) {
        this.plugin = plugin;
        this.forcedSkins = new ConcurrentHashMap<>();
        this.skinCache = new ConcurrentHashMap<>();
        this.originalSkins = new ConcurrentHashMap<>();
        this.mojangAPI = new MojangAPI(plugin);
        this.skinApplier = new SkinApplier(plugin);
        
        // Load configuration
        loadConfiguration();
        
        // Setup data file
        setupDataFile();
        
        // Load saved data
        loadSkinData();
        
        // Register events if enabled
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }
    
    private void loadConfiguration() {
        this.enabled = plugin.getConfig().getBoolean("skins.enabled", true);
        this.cacheDuration = plugin.getConfig().getLong("skins.cache-duration", 3600) * 1000; // Convert to ms
    }
    
    private void setupDataFile() {
        skinDataFile = new File(plugin.getDataFolder(), "skin-data.yml");
        if (!skinDataFile.exists()) {
            try {
                skinDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create skin data file: " + e.getMessage());
            }
        }
        skinDataConfig = YamlConfiguration.loadConfiguration(skinDataFile);
    }
    
    private void loadSkinData() {
        if (skinDataConfig.contains("forced-skins")) {
            for (String player : skinDataConfig.getConfigurationSection("forced-skins").getKeys(false)) {
                String skinInput = skinDataConfig.getString("forced-skins." + player);
                forcedSkins.put(player, skinInput);
            }
        }
        
        plugin.getLogger().info("Loaded " + forcedSkins.size() + " forced skins from data file");
    }
    
    private void saveSkinData() {
        try {
            skinDataConfig.set("forced-skins", null); // Clear existing
            
            for (Map.Entry<String, String> entry : forcedSkins.entrySet()) {
                skinDataConfig.set("forced-skins." + entry.getKey(), entry.getValue());
            }
            
            skinDataConfig.save(skinDataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save skin data: " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // Store original skin data (for restoration later)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            storeOriginalSkin(player);
        }, 20L); // Wait 1 second after join
        
        // Check if player has a forced skin
        if (forcedSkins.containsKey(playerName)) {
            String skinInput = forcedSkins.get(playerName);
            
            // Apply forced skin after a short delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                applySkin(player, skinInput);
            }, 40L); // Wait 2 seconds after join
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        // Clean up original skin data to prevent memory leaks
        originalSkins.remove(playerName);
    }
    
    public void forceSkin(String playerName, String skinInput) {
        forcedSkins.put(playerName, skinInput);

        // Save to disk
        saveSkinData();

        // Apply immediately if player is online
        Player player = plugin.getServer().getPlayer(playerName);
        if (player != null && player.isOnline()) {
            // Store original skin if not already stored
            if (!originalSkins.containsKey(playerName)) {
                storeOriginalSkin(player);
            }

            applySkin(player, skinInput);
        }

        String displayName = MojangAPI.isValidBase64Texture(skinInput) ? "texture base64" : skinInput;
        plugin.getLogger().info("Forced skin '" + displayName + "' for player " + playerName);
    }

    public void removeForcedSkin(String playerName) {
        forcedSkins.remove(playerName);

        // Save to disk
        saveSkinData();

        // Restore original skin if player is online
        Player player = plugin.getServer().getPlayer(playerName);
        if (player != null && player.isOnline()) {
            restoreOriginalSkin(player);
        }

        plugin.getLogger().info("Removed forced skin for player " + playerName);
    }

    public Map<String, String> getAllForcedSkins() {
        return Map.copyOf(forcedSkins);
    }

    private void restoreOriginalSkin(Player player) {
        String playerName = player.getName();
        SkinData originalSkin = originalSkins.get(playerName);

        if (originalSkin != null) {
            if (skinApplier.applySkin(player, originalSkin)) {
                MessageUtils.sendSuccessMessage(player, "Skin original restauré");
            } else {
                MessageUtils.sendErrorMessage(player, "Erreur lors de la restauration du skin original");
            }
        } else {
            // Fallback: remove any applied skin (will show default/random skin)
            if (skinApplier.removeSkin(player)) {
                MessageUtils.sendSuccessMessage(player, "Skin réinitialisé");
            } else {
                MessageUtils.sendErrorMessage(player, "Erreur lors de la réinitialisation du skin");
            }
        }
    }

    public void applySkin(Player player, String skinInput) {
        if (skinInput == null || skinInput.isEmpty()) {
            MessageUtils.sendErrorMessage(player, "Erreur : Skin invalide");
            return;
        }
        
        CompletableFuture<Void> skinTask;
        
        plugin.getLogger().info("Applying skin: " + skinInput.substring(0, Math.min(50, skinInput.length())) + "...");
        plugin.getLogger().info("Is valid base64 texture: " + MojangAPI.isValidBase64Texture(skinInput));
        plugin.getLogger().info("Is valid player name: " + MojangAPI.isValidPlayerName(skinInput));
        
        // Determine if it's a base64 texture or player name
        if (MojangAPI.isValidBase64Texture(skinInput)) {
            // Direct texture application
            plugin.getLogger().info("Applying as base64 texture");
            skinTask = applyTextureDirectly(player, skinInput);
        } else if (MojangAPI.isValidPlayerName(skinInput)) {
            // Fetch from Mojang API
            plugin.getLogger().info("Fetching from Mojang API");
            skinTask = applySkinFromPlayer(player, skinInput);
        } else {
            plugin.getLogger().warning("Invalid skin format: " + skinInput.substring(0, Math.min(50, skinInput.length())));
            MessageUtils.sendErrorMessage(player, "Erreur : Format de skin invalide");
            MessageUtils.sendSecondaryMessage(player, "Utilisez un nom de joueur ou une texture base64");
            return;
        }

        // Handle result
        skinTask.whenComplete((result, throwable) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (throwable != null) {
                    MessageUtils.sendErrorMessage(player, "Erreur lors de l'application du skin : " + throwable.getMessage());
                    plugin.getLogger().warning("Failed to apply skin for " + player.getName() + ": " + throwable.getMessage());
                } else {
                    String displayName = MojangAPI.isValidBase64Texture(skinInput) ? "texture personnalisée" : skinInput;
                    MessageUtils.sendSuccessMessage(player, "Skin appliqué : ", MessageUtils.highlight(displayName, BrandUtils.PRIMARY));
                }
            });
        });
    }
    
    private CompletableFuture<Void> applyTextureDirectly(Player player, String textureValue) {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Applying texture directly for player: " + player.getName());
                plugin.getLogger().info("Texture value: " + textureValue);
                
                // Decode and log the texture to see its structure
                try {
                    String decoded = new String(java.util.Base64.getDecoder().decode(textureValue));
                    plugin.getLogger().info("Decoded texture: " + decoded);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to decode texture for logging: " + e.getMessage());
                }
                
                // Create SkinData from base64 texture (no signature for custom textures)
                SkinData skinData = new SkinData(textureValue, "", "custom_texture");
                
                // Cache the texture
                skinCache.put(textureValue, skinData);
                
                // Apply the skin on main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    boolean success = skinApplier.applySkin(player, skinData);
                    plugin.getLogger().info("Skin application result: " + success);
                    
                    if (success) {
                        MessageUtils.sendSuccessMessage(player, "Skin appliqué : ", MessageUtils.highlight("texture personnalisée", BrandUtils.PRIMARY));
                    } else {
                        MessageUtils.sendErrorMessage(player, "Erreur lors de l'application du skin");
                    }
                });
                
            } catch (Exception e) {
                plugin.getLogger().severe("Error applying texture: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Erreur lors de l'application de la texture : " + e.getMessage());
            }
        });
    }
    
    private CompletableFuture<Void> applySkinFromPlayer(Player player, String targetPlayerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                SkinData skinData = skinCache.get(targetPlayerName);

                // Drop expired cache entries so they get refetched below
                if (skinData != null && skinData.isExpired(cacheDuration)) {
                    plugin.getLogger().info("Skin cache expired for " + targetPlayerName + ", fetching new data");
                    skinCache.remove(targetPlayerName);
                    skinData = null;
                }

                if (skinData == null) {
                    skinData = mojangAPI.fetchSkinData(targetPlayerName).get();
                    skinCache.put(targetPlayerName, skinData);
                    plugin.getLogger().info("Fetched and cached skin data for " + targetPlayerName);
                }

                return skinData;
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la récupération du skin : " + e.getMessage());
            }
        }).thenComposeAsync(skinData -> {
            // NMS entity mutations (setPlayerProfile) must happen on the main thread,
            // not on the ForkJoinPool thread this future was running on.
            CompletableFuture<Void> applied = new CompletableFuture<>();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (skinApplier.applySkin(player, skinData)) {
                    applied.complete(null);
                } else {
                    applied.completeExceptionally(new RuntimeException("Échec de l'application du skin via NMS"));
                }
            });
            return applied;
        });
    }
    
    private void storeOriginalSkin(Player player) {
        String playerName = player.getName();
        
        if (originalSkins.containsKey(playerName)) {
            return; // Already stored
        }
        
        // Fetch player's current skin from Mojang
        mojangAPI.fetchSkinData(playerName).whenComplete((skinData, throwable) -> {
            if (throwable == null && skinData != null) {
                originalSkins.put(playerName, skinData);
                plugin.getLogger().info("Stored original skin for " + playerName);
            } else {
                plugin.getLogger().warning("Could not store original skin for " + playerName + ": " + 
                    (throwable != null ? throwable.getMessage() : "null skin data"));
            }
        });
    }
    
    @Override
    public void shutdown() {
        // Save all data before shutdown
        saveSkinData();

        // Clear caches
        skinCache.clear();
        originalSkins.clear();

        plugin.getLogger().info("SkinManager shutdown complete");
    }
}