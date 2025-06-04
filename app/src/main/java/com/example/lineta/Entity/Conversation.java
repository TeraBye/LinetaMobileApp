package com.example.lineta.Entity;

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
    private Long lastUpdate; // Thay đổi thành Long để khớp với timestamp milliseconds

    @SerializedName("unreadCount")
    private Map<String, Long> unreadCount; // Thay đổi thành Long để khớp với backend

    @SerializedName("users")
    private List<UserInfo> users; // Danh sách thông tin người dùng

    // Class lồng để ánh xạ thông tin người dùng

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

    // Lấy tên người dùng khác (không phải người dùng hiện tại)
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

    // Lấy avatar của người dùng khác
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

    // Lấy số tin chưa đọc của người dùng hiện tại
    public long getUnreadCountForUser(String userId) {
        if (unreadCount != null && unreadCount.containsKey(userId)) {
            return unreadCount.get(userId);
        }
        return 0;
    }
}