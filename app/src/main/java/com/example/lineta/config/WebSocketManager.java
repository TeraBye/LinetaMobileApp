package com.example.lineta.config;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;
import ua.naiksoftware.stomp.dto.LifecycleEvent;

public class WebSocketManager {
    private static WebSocketManager instance;
    private StompClient stompClient;
    private MessageListener listener;
    private boolean isConnected = false;
    private final Set<String> subscribedTopics = new HashSet<>();
    private String currentUrl = "";

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

    public void connect(String url, Runnable onConnected) {
        if (isConnected && stompClient != null && stompClient.isConnected()) {
            onConnected.run(); // Nếu đã kết nối → gọi luôn callback
            return;
        }

        currentUrl = url;
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url);
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    isConnected = true;
                    Log.d("WebSocket", "Connected");
                    onConnected.run();  // 🔥 Gọi subscribe hoặc lệnh khác sau khi connect
                    break;
                case ERROR:
                    Log.e("WebSocket", "Error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    isConnected = false;
                    Log.d("WebSocket", "Disconnected");
                    break;
            }
        });

        stompClient.connect();
    }


    public void subscribe(String topic) {
        if (subscribedTopics.contains(topic)) return;
        subscribedTopics.add(topic);

        if (stompClient != null && stompClient.isConnected()) {
            stompClient.topic(topic).subscribe(this::handleMessage);
        }
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
        isConnected = false;
        subscribedTopics.clear();
    }
}
