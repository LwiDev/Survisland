package com.lwidev.survisland.confess;

import com.lwidev.survisland.Survisland;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkCodeManager {
    
    private final Survisland plugin;
    private final ConcurrentHashMap<String, PendingLink> pendingLinks; // code -> PendingLink
    private final ConcurrentHashMap<String, String> playerCodes; // playerName -> code
    private final ScheduledExecutorService scheduler;
    private final SecureRandom random;
    
    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRY_MINUTES = 10;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    public LinkCodeManager(Survisland plugin) {
        this.plugin = plugin;
        this.pendingLinks = new ConcurrentHashMap<>();
        this.playerCodes = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.random = new SecureRandom();
        
        // Nettoyage automatique des codes expirés toutes les minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredCodes, 1, 1, TimeUnit.MINUTES);
        
        plugin.getLogger().info("LinkCodeManager initialized");
    }
    
    public String generateLinkCode(String playerName) {
        // Supprimer l'ancien code si il existe
        String oldCode = playerCodes.remove(playerName.toLowerCase());
        if (oldCode != null) {
            pendingLinks.remove(oldCode);
            plugin.getLogger().info("Removed old pending code for player " + playerName);
        }
        
        // Générer un nouveau code unique
        String code;
        do {
            code = generateRandomCode();
        } while (pendingLinks.containsKey(code));
        
        // Créer le lien en attente
        long expiryTime = System.currentTimeMillis() + (CODE_EXPIRY_MINUTES * 60 * 1000);
        PendingLink pendingLink = new PendingLink(playerName, expiryTime);
        
        pendingLinks.put(code, pendingLink);
        playerCodes.put(playerName.toLowerCase(), code);
        
        plugin.getLogger().info("Generated link code " + code + " for player " + playerName + " (expires in " + CODE_EXPIRY_MINUTES + " minutes)");
        
        return code;
    }
    
    public boolean validateAndConsumeLinkCode(String code, String channelName) {
        PendingLink pendingLink = pendingLinks.remove(code);
        
        if (pendingLink == null) {
            plugin.getLogger().warning("Invalid or expired link code: " + code);
            return false;
        }
        
        if (System.currentTimeMillis() > pendingLink.getExpiryTime()) {
            plugin.getLogger().warning("Expired link code: " + code + " for player " + pendingLink.getPlayerName());
            playerCodes.remove(pendingLink.getPlayerName().toLowerCase());
            return false;
        }
        
        // Code valide, créer le lien
        String playerName = pendingLink.getPlayerName();
        playerCodes.remove(playerName.toLowerCase());
        
        // Utiliser le ConfessLinkManager pour créer le lien définitif
        plugin.getConfessLinkManager().linkPlayer(playerName, channelName);
        
        plugin.getLogger().info("Successfully linked player " + playerName + " to channel " + channelName + " via code " + code);
        
        return true;
    }
    
    public boolean hasValidCode(String playerName) {
        String code = playerCodes.get(playerName.toLowerCase());
        if (code == null) {
            return false;
        }
        
        PendingLink pendingLink = pendingLinks.get(code);
        if (pendingLink == null) {
            playerCodes.remove(playerName.toLowerCase());
            return false;
        }
        
        if (System.currentTimeMillis() > pendingLink.getExpiryTime()) {
            pendingLinks.remove(code);
            playerCodes.remove(playerName.toLowerCase());
            return false;
        }
        
        return true;
    }
    
    public String getPlayerCode(String playerName) {
        return playerCodes.get(playerName.toLowerCase());
    }
    
    public long getCodeExpiryMinutes() {
        return CODE_EXPIRY_MINUTES;
    }
    
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
    
    private void cleanupExpiredCodes() {
        long currentTime = System.currentTimeMillis();
        AtomicInteger removed = new AtomicInteger();
        
        pendingLinks.entrySet().removeIf(entry -> {
            if (currentTime > entry.getValue().getExpiryTime()) {
                String playerName = entry.getValue().getPlayerName();
                playerCodes.remove(playerName.toLowerCase());
                removed.getAndIncrement();
                return true;
            }
            return false;
        });
        
        if (removed.get() > 0) {
            plugin.getLogger().info("Cleaned up " + removed + " expired link codes");
        }
    }
    
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        plugin.getLogger().info("LinkCodeManager shutdown");
    }
    
    public int getPendingLinksCount() {
        return pendingLinks.size();
    }
    
    private static class PendingLink {
        private final String playerName;
        private final long expiryTime;
        
        public PendingLink(String playerName, long expiryTime) {
            this.playerName = playerName;
            this.expiryTime = expiryTime;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public long getExpiryTime() {
            return expiryTime;
        }
    }
}