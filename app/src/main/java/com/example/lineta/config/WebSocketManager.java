package com.example.lineta.config;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;
import ua.naiksoftware.stomp.dto.LifecycleEvent;

public class WebSocketManager {
    private static WebSocketManager instance;
    private StompClient stompClient;
    private MessageListener listener;

    public interface MessageListener {
        void onMessage(JSONObject jsonObject);
    }

    private WebSocketManager() {}

    public static WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void connect(String url, String topic) {
        if (stompClient != null && stompClient.isConnected()) return;

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url);
        stompClient.connect();

        // Theo dõi trạng thái kết nối
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WebSocket", "Connected");
                    break;
                case ERROR:
                    Log.e("WebSocket", "Error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("WebSocket", "Disconnected");
                    break;
            }
        });

        // Đăng ký lắng nghe topic
        stompClient.topic(topic).subscribe(this::handleMessage);
    }

    private void handleMessage(StompMessage message) {
        try {
            JSONObject jsonObject = new JSONObject(message.getPayload());
            if (listener != null) {
                listener.onMessage(jsonObject);
            }
        } catch (JSONException e) {
            Log.e("WebSocket", "Invalid JSON: " + e.getMessage());
        }
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
    }
}
