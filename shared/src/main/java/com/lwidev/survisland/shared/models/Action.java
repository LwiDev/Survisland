package com.lwidev.survisland.shared.models;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Action {
    
    private String id;
    private String displayName;
    private List<String> commands;
    private long cooldown;
    private int maxUses;
    private int currentUses;
    private Set<String> allowedPlayers;
    private boolean requiresPermission;
    
    public Action() {
        this.allowedPlayers = new HashSet<>();
        this.currentUses = 0;
        this.requiresPermission = true;
    }
    
    public Action(String id, String displayName, List<String> commands) {
        this();
        this.id = id;
        this.displayName = displayName;
        this.commands = commands;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    
    public long getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }
    
    public int getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }
    
    public int getCurrentUses() {
        return currentUses;
    }
    
    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }
    
    public Set<String> getAllowedPlayers() {
        return allowedPlayers;
    }
    
    public void setAllowedPlayers(Set<String> allowedPlayers) {
        this.allowedPlayers = allowedPlayers;
    }
    
    public boolean isRequiresPermission() {
        return requiresPermission;
    }
    
    public void setRequiresPermission(boolean requiresPermission) {
        this.requiresPermission = requiresPermission;
    }
    
    public boolean canUse(String playerName) {
        if (maxUses > 0 && currentUses >= maxUses) {
            return false;
        }
        
        if (requiresPermission && !allowedPlayers.contains(playerName)) {
            return false;
        }
        
        return true;
    }
    
    public void incrementUse() {
        this.currentUses++;
    }
}