package com.example.lineta.Entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class Conversation {
    @SerializedName("id")
    private String id; // Document ID của cuộc hội thoại (thêm thủ công từ API)

    @SerializedName("lastMessage")
    private String lastMessage;

    @SerializedName("lastSender")
    private String lastSender;

    @SerializedName("lastUpdate")
    private String lastUpdate; // Timestamp dạng String (API có thể đã chuyển đổi)

    @SerializedName("listUser")
    private List<String> listUser; // Danh sách UID người dùng

    @SerializedName("ureadCount")
    private Map<String, Integer> ureadCount; // Map UID và số tin chưa đọc

    public Conversation() {
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastSender() { return lastSender; }
    public void setLastSender(String lastSender) { this.lastSender = lastSender; }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }

    public List<String> getListUser() { return listUser; }
    public void setListUser(List<String> listUser) { this.listUser = listUser; }

    public Map<String, Integer> getUreadCount() { return ureadCount; }
    public void setUreadCount(Map<String, Integer> ureadCount) { this.ureadCount = ureadCount; }


    public String getName(String currentUserId) {
        if (listUser != null) {
            for (String userId : listUser) {
                if (!userId.equals(currentUserId)) {
                    return userId; // Trả về UID của người kia)
                }
            }
        }
        return "Unknown";
    }

    // Lấy số tin chưa đọc của người dùng hiện tại
    public int getUnreadCountForUser(String userId) {
        if (ureadCount != null && ureadCount.containsKey(userId)) {
            return ureadCount.get(userId);
        }
        return 0;
    }
}