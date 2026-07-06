package com.lwidev.survisland.skins;

public class SkinData {
    
    private final String texture;
    private final String signature;
    private final String playerName;
    private final long timestamp;
    
    public SkinData(String texture, String signature, String playerName) {
        this.texture = texture;
        this.signature = signature;
        this.playerName = playerName;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getTexture() {
        return texture;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isExpired(long cacheDuration) {
        return System.currentTimeMillis() - timestamp > cacheDuration;
    }
    
    @Override
    public String toString() {
        return "SkinData{playerName='" + playerName + "', timestamp=" + timestamp + "}";
    }
}