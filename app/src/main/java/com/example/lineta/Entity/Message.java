package com.example.lineta.Entity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Message {
    @SerializedName("id")
    private String id; // Document ID của tin nhắn (thêm thủ công từ API)

    @SerializedName("context")
    private String context;

    @SerializedName("conversationId")
    private String conversationId;

    @SerializedName("media")
    private List<String> media; // Danh sách URL ảnh

    @SerializedName("readBy")
    private List<String> readBy; // Danh sách UID người đã đọc

    @SerializedName("sender")
    private String sender; // UID người gửi

    @SerializedName("timestamp")
    private String timestamp; // Timestamp dạng String

    public Message() {
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getMedia() { return media; }
    public void setMedia(List<String> media) { this.media = media; }

    public List<String> getReadBy() { return readBy; }
    public void setReadBy(List<String> readBy) { this.readBy = readBy; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}