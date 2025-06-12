package com.example.lineta.Entity.Conversation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Message {
    @SerializedName("id")
    private String id;

    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("sender")
    private String senderId;

    @SerializedName("context")
    private String context;

    @SerializedName("media")
    private List<String> media; // Thay đổi thành List<String> để hỗ trợ mảng

    @SerializedName("timestamp")
    private Timestamp timestamp;

    @SerializedName("user")
    private UserInfo user; // Thông tin người gửi

    public Message() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public List<String> getMedia() { return media; }
    public void setMedia(List<String> media) { this.media = media; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    public String getRelativeTimestamp() {
        return timestamp != null ? timestamp.toRelativeTime() : "Unknown";
    }

    public boolean hasMedia() {
        return media != null && !media.isEmpty();
    }

    public boolean hasText() {
        return context != null && !context.isEmpty();
    }

    // Lấy URL ảnh đầu tiên nếu có (cho trường hợp chỉ hiển thị 1 ảnh)
    public String getFirstMedia() {
        return hasMedia() ? media.get(0) : null;
    }
}