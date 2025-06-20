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
    private StompClient stompClient;
    private MessageListener listener;
    private boolean isConnected = false;
    private final Set<String> subscribedTopics = new HashSet<>();
    private final String url;

    public interface MessageListener {
        void onMessage(JSONObject jsonObject);
    }

    public WebSocketManager(String url) {
        this.url = url;
    }

    public void connect(Runnable onConnected) {
        if (isConnected && stompClient != null && stompClient.isConnected()) {
            onConnected.run();
            return;
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url);
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    isConnected = true;
                    Log.d("WebSocket", "Connected: " + url);
                    for (String topic : subscribedTopics) {
                        stompClient.topic(topic).subscribe(this::handleMessage);
                    }
                    onConnected.run();
                    break;
                case ERROR:
                    Log.e("WebSocket", "Error: " + url, lifecycleEvent.getException());
                    break;
                case CLOSED:
                    isConnected = false;
                    Log.d("WebSocket", "Disconnected: " + url);
                    break;
            }
        });

        stompClient.connect();
    }

    public void subscribe(String topic) {
        if (subscribedTopics.contains(topic)) return;
        subscribedTopics.add(topic);

        if (stompClient != null && stompClient.isConnected()) {
            Log.d("WebSocket", "Subscribing to topic: " + topic);
            stompClient.topic(topic).subscribe(this::handleMessage);
        } else {
            // Đợi kết nối rồi mới đăng ký
            Log.w("WebSocket", "Tried to subscribe before connection: " + topic);
            new Thread(() -> {
                while (!isConnected) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }
                Log.d("WebSocket", "Subscribing late to topic: " + topic);
                stompClient.topic(topic).subscribe(this::handleMessage);
            }).start();
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

