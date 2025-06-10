package com.example.lineta.Entity.Conversation;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class Conversation {
    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("listUser")
    private List<String> listUser;

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("lastSender")
    private String lastSender;

    @SerializedName("lastUpdate")
    private Long lastUpdate;

    @SerializedName("unreadCount")
    private Map<String, Long> unreadCount;

    @SerializedName("users")
    private List<UserInfo> users;

    // Getters và Setters
    public String getId() { return conversationId; }
    public void setId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getListUser() { return listUser; }
    public void setListUser(List<String> listUser) { this.listUser = listUser; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastSender() { return lastSender; }
    public void setLastSender(String lastSender) { this.lastSender = lastSender; }

    public Long getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Long lastUpdate) { this.lastUpdate = lastUpdate; }

    public Map<String, Long> getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Map<String, Long> unreadCount) { this.unreadCount = unreadCount; }

    public List<UserInfo> getUsers() { return users; }
    public void setUsers(List<UserInfo> users) { this.users = users; }

    // Phương thức tiện ích
    public String getName(String currentUserId) {
        if (users != null) {
            for (UserInfo user : users) {
                if (!user.getId().equals(currentUserId)) {
                    return user.getFullName() != null ? user.getFullName() : user.getUsername();
                }
            }
        }
        return "Unknown";
    }

    public String getAvatarUrl(String currentUserId) {
        if (users != null) {
            for (UserInfo user : users) {
                if (!user.getId().equals(currentUserId)) {
                    return user.getAvatarUrl();
                }
            }
        }
        return null;
    }

    public long getUnreadCountForUser(String userId) {
        if (unreadCount != null && unreadCount.containsKey(userId)) {
            return unreadCount.get(userId);
        }
        return 0;
    }
}