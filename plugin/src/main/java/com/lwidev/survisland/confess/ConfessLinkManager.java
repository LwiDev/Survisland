package com.lwidev.survisland.confess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lwidev.survisland.Survisland;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ConfessLinkManager {
    
    private final Survisland plugin;
    private final File linksFile;
    private final ObjectMapper mapper;
    private final Map<String, String> links; // pseudoMC -> channel name
    
    public ConfessLinkManager(Survisland plugin) {
        this.plugin = plugin;
        this.linksFile = new File(plugin.getDataFolder(), "confess-links.json");
        this.mapper = new ObjectMapper();
        this.links = new ConcurrentHashMap<>();
        
        loadLinks();
    }
    
    private void loadLinks() {
        if (!linksFile.exists()) {
            plugin.getLogger().info("Confess links file not found, creating new one");
            saveLinks();
            return;
        }
        
        try {
            Map<String, String> loadedLinks = mapper.readValue(linksFile, new TypeReference<Map<String, String>>() {});
            links.clear();
            links.putAll(loadedLinks);
            plugin.getLogger().info("Loaded " + links.size() + " confess links");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load confess links", e);
        }
    }
    
    private void saveLinks() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            mapper.writerWithDefaultPrettyPrinter().writeValue(linksFile, new HashMap<>(links));
            plugin.getLogger().info("Saved " + links.size() + " confess links");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save confess links", e);
        }
    }
    
    public void linkPlayer(String minecraftPseudo, String channelName) {
        links.put(minecraftPseudo.toLowerCase(), channelName);
        saveLinks();
        plugin.getLogger().info("Linked player " + minecraftPseudo + " to channel " + channelName);
    }
    
    public void unlinkPlayer(String minecraftPseudo) {
        String removed = links.remove(minecraftPseudo.toLowerCase());
        if (removed != null) {
            saveLinks();
            plugin.getLogger().info("Unlinked player " + minecraftPseudo + " from channel " + removed);
        }
    }
    
    public String getPlayerChannel(String minecraftPseudo) {
        return links.get(minecraftPseudo.toLowerCase());
    }
    
    public boolean isPlayerLinked(String minecraftPseudo) {
        return links.containsKey(minecraftPseudo.toLowerCase());
    }
    
    public Map<String, String> getAllLinks() {
        return new HashMap<>(links);
    }
    
    public int getLinksCount() {
        return links.size();
    }
}