package com.lwidev.survisland.skins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lwidev.survisland.Survisland;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MinecraftHeadsAPI {
    
    private static final String API_BASE_URL = "https://minecraft-heads.com/scripts/api.php";
    
    private final Survisland plugin;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public MinecraftHeadsAPI(Survisland plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public CompletableFuture<List<HeadData>> searchHeads(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = API_BASE_URL + "?cat=all&tags=" + java.net.URLEncoder.encode(query, "UTF-8");
                plugin.getLogger().info("Searching heads with URL: " + url);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                plugin.getLogger().info("API response status: " + response.statusCode());
                plugin.getLogger().info("API response body: " + response.body().substring(0, Math.min(200, response.body().length())));
                
                if (response.statusCode() != 200) {
                    throw new IOException("API responded with status: " + response.statusCode());
                }
                
                return parseHeadsResponse(response.body());
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to search heads: " + e.getMessage());
                throw new RuntimeException("Erreur lors de la recherche: " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<List<HeadData>> getHeadsByCategory(String category) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = API_BASE_URL + "?cat=" + category;
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() != 200) {
                    throw new IOException("API responded with status: " + response.statusCode());
                }
                
                List<HeadData> heads = parseHeadsResponse(response.body());
                
                return heads;
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get heads by category: " + e.getMessage());
                throw new RuntimeException("Erreur lors de la récupération: " + e.getMessage());
            }
        });
    }
    
    private List<HeadData> parseHeadsResponse(String responseBody) throws IOException {
        List<HeadData> heads = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        
        if (rootNode.isArray()) {
            for (JsonNode headNode : rootNode) {
                String name = headNode.path("name").asText("");
                String uuid = headNode.path("uuid").asText("");
                String value = headNode.path("value").asText("");
                String signature = headNode.path("signature").asText(""); // Check for signature
                
                // API doesn't seem to provide category/tags in response, use empty strings
                String category = "";
                String tags = "";
                
                if (!value.isEmpty()) {
                    heads.add(new HeadData(name, uuid, value, signature, category, tags));
                }
            }
        }
        
        return heads;
    }
    
    public static class HeadData {
        private final String name;
        private final String uuid;
        private final String texture;
        private final String signature;
        private final String category;
        private final String tags;
        
        public HeadData(String name, String uuid, String texture, String signature, String category, String tags) {
            this.name = name;
            this.uuid = uuid;
            this.texture = texture;
            this.signature = signature;
            this.category = category;
            this.tags = tags;
        }
        
        public String getName() {
            return name;
        }
        
        public String getUuid() {
            return uuid;
        }
        
        public String getTexture() {
            return texture;
        }
        
        public String getSignature() {
            return signature;
        }
        
        public String getCategory() {
            return category;
        }
        
        public String getTags() {
            return tags;
        }
        
        public String getDisplayName() {
            return name.isEmpty() ? "Tête personnalisée" : name;
        }
    }
    
    public static final String[] CATEGORIES = {
        "alphabet", "animals", "blocks", "decoration", "food-drinks", 
        "humans", "humanoid", "miscellaneous", "monsters", "plants"
    };
    
    public static boolean isValidCategory(String category) {
        for (String validCategory : CATEGORIES) {
            if (validCategory.equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }
}