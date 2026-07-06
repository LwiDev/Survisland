package com.lwidev.survisland.api.skin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MojangAPI {
    
    private static final String UUID_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final JavaPlugin plugin;

    public MojangAPI(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public CompletableFuture<SkinData> fetchSkinData(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Step 1: Get UUID from username
                String uuid = fetchUUID(playerName);
                if (uuid == null) {
                    throw new RuntimeException("Joueur introuvable : " + playerName);
                }
                
                // Step 2: Get profile with textures
                return fetchProfile(uuid, playerName);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Erreur lors de la récupération du skin pour " + playerName + ": " + e.getMessage());
                throw new RuntimeException("Impossible de récupérer le skin : " + e.getMessage());
            }
        });
    }
    
    private String fetchUUID(String playerName) throws IOException {
        URL url = URI.create(UUID_API + playerName).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        
        int responseCode = connection.getResponseCode();
        if (responseCode == 204) {
            return null; // Player not found
        }
        
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + " from Mojang UUID API");
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            JsonNode json = MAPPER.readTree(response.toString());
            return json.get("id").asText();
        }
    }
    
    private SkinData fetchProfile(String uuid, String playerName) throws IOException {
        URL url = URI.create(PROFILE_API + uuid + "?unsigned=false").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        
        if (connection.getResponseCode() != 200) {
            throw new IOException("HTTP " + connection.getResponseCode() + " from Mojang Profile API");
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            JsonNode json = MAPPER.readTree(response.toString());
            JsonNode properties = json.get("properties");
            
            if (properties != null && properties.isArray()) {
                for (JsonNode property : properties) {
                    if ("textures".equals(property.get("name").asText())) {
                        String texture = property.get("value").asText();
                        String signature = property.has("signature") ? property.get("signature").asText() : "";
                        
                        return new SkinData(texture, signature, playerName);
                    }
                }
            }
            
            throw new RuntimeException("Aucune texture trouvée pour " + playerName);
        }
    }
    
    public static boolean isValidBase64Texture(String input) {
        if (input == null || input.length() < 100) {
            return false;
        }
        
        try {
            // Check if it's valid base64
            Base64.getDecoder().decode(input);
            
            // Decode and check if it contains texture data
            String decoded = new String(Base64.getDecoder().decode(input));
            // Check for both Mojang format and minecraft-heads.com format
            return (decoded.contains("\"textures\"") && decoded.contains("\"SKIN\"")) ||
                   (decoded.contains("\"SKIN\"") && decoded.contains("\"url\""));
            
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isValidPlayerName(String input) {
        if (input == null) return false;
        
        // Minecraft usernames: 3-16 characters, alphanumeric + underscore
        return input.matches("^[a-zA-Z0-9_]{3,16}$");
    }
}