package com.example.lineta.service.client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.lineta.dto.response.ConversationUpdateDTO;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

public class WebSocketService extends Service {

    private static final String TAG = "WebSocketMessageService";
    private static final String WS_URL = "ws://10.0.2.2:4000/api/message/ws"; // Sử dụng 10.0.2.2 cho emulator

    private final IBinder binder = new WebSocketBinder();
    private StompClient stompClient;
    private final Gson gson = new Gson();

    private final Map<String, ConversationUpdateDTO> conversationUpdates = new ConcurrentHashMap<>();
    private OnConversationUpdateListener updateListener;
    private String userId; // Lưu userId
    private long totalUnreadCount = 0; // Theo dõi tổng số tin nhắn chưa đọc

    @Override
    public void onCreate() {
        super.onCreate();
        connectWebSocket();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Lấy userId từ Intent khi bind
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            if (userId == null) {
                Log.w(TAG, "userId not found in Intent");
                userId = "unknown_user"; // Fallback
            } else {
                Log.d(TAG, "userId set to: " + userId);
            }
        }
        return binder;
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient.Builder().build();
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);

        stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "STOMP connection opened");
                            // Subscribe to /topic/conversations
                            stompClient.topic("/topic/conversations")
                                    .subscribe(topicMessage -> {
                                        String message = topicMessage.getPayload();
                                        Log.d(TAG, "Received message from /topic/conversations: " + message);
                                        handleMessage(message);
                                    }, throwable -> Log.e(TAG, "Error subscribing to topic: " + throwable.getMessage()));
                            break;
                        case ERROR:
                            Log.e(TAG, "STOMP error: " + lifecycleEvent.getException().getMessage());
                            break;
                        case CLOSED:
                            Log.d(TAG, "STOMP connection closed");
                            break;
                    }
                });

        stompClient.connect();
    }

    private void handleMessage(String message) {
        try {
            Log.d(TAG, "Parsing message: " + message);
            ConversationUpdateDTO updateDTO = gson.fromJson(message, ConversationUpdateDTO.class);
            if (updateDTO != null && updateDTO.getConversationId() != null) {
                Log.d(TAG, "Parsed updateDTO: " + gson.toJson(updateDTO));
                conversationUpdates.put(updateDTO.getConversationId(), updateDTO);

                // Tính unreadCount cho userId hiện tại
                long unreadCount = (updateDTO.getUnreadCount() != null && userId != null)
                        ? updateDTO.getUnreadCount().getOrDefault(userId, 0L)
                        : 0L;

                if (updateListener != null) {
                    Log.d(TAG, "Notifying listener with convId: " + updateDTO.getConversationId() + ", unreadCount: " + unreadCount);
                    updateListener.onConversationUpdated(updateDTO.getConversationId(), unreadCount, updateDTO.getLastUpdate());
                }

                // Cập nhật tổng số tin nhắn chưa đọc
                updateTotalUnreadCount();
            } else {
                Log.w(TAG, "Invalid updateDTO or conversationId: " + message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message: " + e.getMessage() + ", Message: " + message, e);
        }
    }

    private void updateTotalUnreadCount() {
        totalUnreadCount = conversationUpdates.values().stream()
                .filter(dto -> dto.getUnreadCount() != null && userId != null)
                .flatMap(dto -> dto.getUnreadCount().entrySet().stream())
                .filter(entry -> userId.equals(entry.getKey())) // Chỉ lấy unreadCount của userId hiện tại
                .mapToLong(Map.Entry::getValue)
                .sum();
        broadcastUnreadCount(totalUnreadCount);
    }

    private void broadcastUnreadCount(long totalUnread) {
        Intent intent = new Intent("com.example.lineta.UNREAD_COUNT_UPDATE")
                .setPackage(getPackageName()); // Đảm bảo chỉ ứng dụng nhận
        intent.putExtra("totalUnread", totalUnread);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcasted totalUnread: " + totalUnread);
    }

    public void setOnConversationUpdateListener(OnConversationUpdateListener listener) {
        this.updateListener = listener;
    }

    public long getUnreadCount(String conversationId) {
        ConversationUpdateDTO dto = conversationUpdates.get(conversationId);
        if (dto != null && dto.getUnreadCount() != null && userId != null) {
            return dto.getUnreadCount().getOrDefault(userId, 0L);
        }
        return 0L;
    }

    public long getTotalUnreadCount() {
        return totalUnreadCount;
    }

    // Thêm phương thức để gửi thông báo WebSocket
    public void sendConversationUpdate(Map<String, Object> updateData) {
        if (stompClient != null && stompClient.isConnected()) {
            String conversationId = (String) updateData.get("conversationId");
            @SuppressWarnings("unchecked")
            Map<String, Long> unreadCountMap = (Map<String, Long>) updateData.get("unreadCount");
            String lastUpdate = String.valueOf(updateData.get("lastUpdate"));

            ConversationUpdateDTO updateDTO = new ConversationUpdateDTO();
            updateDTO.setConversationId(conversationId);
            updateDTO.setUnreadCount(unreadCountMap != null ? unreadCountMap : new HashMap<>());
            updateDTO.setLastUpdate(lastUpdate);

            String jsonMessage = gson.toJson(updateDTO);
            stompClient.send("/app/conversations", jsonMessage)
                    .subscribe(
                            () -> Log.d(TAG, "WebSocket message sent successfully for conversationId: " + conversationId),
                            throwable -> Log.e(TAG, "Failed to send WebSocket message: " + throwable.getMessage())
                    );
        } else {
            Log.w(TAG, "StompClient is null or not connected, cannot send WebSocket update");
        }
    }

    private String getUserIdFromContext() {
        // Trả về userId đã lưu
        if (userId != null) {
            return userId;
        }
        Log.w(TAG, "userId is null, using fallback");
        return "unknown_user"; // Fallback
    }

    // Thêm broadcast sau khi gửi update
    public void sendConversationUpdateWithBroadcast(Map<String, Object> updateData) {
        sendConversationUpdate(updateData); // Gửi WebSocket update
        // Broadcast ngay lập tức để đồng bộ
        if (updateData.get("unreadCount") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Long> unreadCountMap = (Map<String, Long>) updateData.get("unreadCount");
            long newTotalUnread = unreadCountMap.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
                    .mapToLong(Map.Entry::getValue)
                    .sum();
            broadcastUnreadCount(newTotalUnread); // Cập nhật ngay lập tức
            Log.d(TAG, "Broadcasted new totalUnread after update: " + newTotalUnread);
        }
    }

    @Override
    public void onDestroy() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
        super.onDestroy();
    }

    public class WebSocketBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    public interface OnConversationUpdateListener {
        void onConversationUpdated(String conversationId, long unreadCount, String lastUpdate);
    }
}