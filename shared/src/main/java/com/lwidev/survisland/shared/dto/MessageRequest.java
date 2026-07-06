package com.lwidev.survisland.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageRequest {
    
    @JsonProperty("channel_id")
    private String channelId;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("sender")
    private String sender;
    
    public MessageRequest() {}
    
    public MessageRequest(String channelId, String message, String sender) {
        this.channelId = channelId;
        this.message = message;
        this.sender = sender;
    }
    
    public String getChannelId() {
        return channelId;
    }
    
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
}