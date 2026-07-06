package com.lwidev.survisland.shared.config;

public class DiscordConfig {
    
    public static final String DEFAULT_BOT_PORT = "8080";
    public static final String DEFAULT_HOST = "localhost";
    public static final String SEND_MESSAGE_ENDPOINT = "/api/send-message";
    
    private String botToken;
    private String liveChannelId;
    private int botPort;
    private String botHost;
    
    public DiscordConfig() {
        this.botPort = Integer.parseInt(DEFAULT_BOT_PORT);
        this.botHost = DEFAULT_HOST;
    }
    
    public String getBotToken() {
        return botToken;
    }
    
    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }
    
    public String getLiveChannelId() {
        return liveChannelId;
    }
    
    public void setLiveChannelId(String liveChannelId) {
        this.liveChannelId = liveChannelId;
    }
    
    public int getBotPort() {
        return botPort;
    }
    
    public void setBotPort(int botPort) {
        this.botPort = botPort;
    }
    
    public String getBotHost() {
        return botHost;
    }
    
    public void setBotHost(String botHost) {
        this.botHost = botHost;
    }
    
    public String getBotUrl() {
        return "http://" + botHost + ":" + botPort;
    }
}