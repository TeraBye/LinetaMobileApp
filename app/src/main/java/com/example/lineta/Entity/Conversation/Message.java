package com.example.lineta.Entity.Conversation;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("id")
    private String id;

    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("sender")
    private String senderId;

    @SerializedName("context")
    private String context;

    @SerializedName("timestamp")
    private Timestamp timestamp;

    @SerializedName("user")
    private UserInfo user; // Thông tin người gửi

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    public String getRelativeTimestamp() {
        return timestamp != null ? timestamp.toRelativeTime() : "Unknown";
    }
}