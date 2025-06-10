package com.example.lineta.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class ConversationUpdateDTO {
    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("listUser")
    private List<String> listUser;

    @SerializedName("name")
    private String name;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("lastUpdate")
    private String lastUpdate; // Dạng ISO_LOCAL_DATE_TIME

    @SerializedName("unreadCount")
    private Map<String, Long> unreadCount;

    // Getters và Setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getListUser() { return listUser; }
    public void setListUser(List<String> listUser) { this.listUser = listUser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }

    public Map<String, Long> getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Map<String, Long> unreadCount) { this.unreadCount = unreadCount; }
}