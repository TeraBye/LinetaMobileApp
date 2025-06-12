package com.example.lineta.config;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;
import ua.naiksoftware.stomp.dto.LifecycleEvent;

public class RawWebSocketManager {
    private StompClient stompClient;
    private RawMessageListener listener;
    private boolean isConnected = false;
    private final Set<String> subscribedTopics = new HashSet<>();
    private final String url;

    public interface RawMessageListener {
        void onMessage(String message);
    }

    public RawWebSocketManager(String url) {
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
                    Log.d("RawWebSocket", "Connected: " + url);
                    for (String topic : subscribedTopics) {
                        stompClient.topic(topic).subscribe(this::handleMessage);
                    }
                    onConnected.run();
                    break;
                case ERROR:
                    Log.e("RawWebSocket", "Error: " + url, lifecycleEvent.getException());
                    break;
                case CLOSED:
                    isConnected = false;
                    Log.d("RawWebSocket", "Disconnected: " + url);
                    break;
            }
        });

        stompClient.connect();
    }

    public void subscribe(String topic) {
        if (subscribedTopics.contains(topic)) return;
        subscribedTopics.add(topic);

        if (stompClient != null && stompClient.isConnected()) {
            Log.d("RawWebSocket", "Subscribing to topic: " + topic);
            stompClient.topic(topic).subscribe(this::handleMessage);
        } else {
            Log.w("RawWebSocket", "Tried to subscribe before connection: " + topic);
            new Thread(() -> {
                while (!isConnected) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }
                Log.d("RawWebSocket", "Subscribing late to topic: " + topic);
                stompClient.topic(topic).subscribe(this::handleMessage);
            }).start();
        }
    }

    private void handleMessage(StompMessage message) {
        String payload = message.getPayload();
        if (listener != null) {
            listener.onMessage(payload);  // Gửi nguyên chuỗi string (không parse JSON)
        }
    }

    public void setListener(RawMessageListener listener) {
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
