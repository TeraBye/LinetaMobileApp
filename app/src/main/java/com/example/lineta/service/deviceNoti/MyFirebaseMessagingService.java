package com.example.lineta.service.deviceNoti;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lineta.Entity.DeviceNoti.TokenRequest;
import com.example.lineta.R;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.DeviceNotiApi;
import com.example.lineta.service.client.ApiClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        DeviceNotiApi apiService = ApiClient.getRetrofit().create(DeviceNotiApi.class);

        TokenRequest tokenRequest = new TokenRequest(token, "terabye");

        Call<Void> call = apiService.sendTokenToServer(tokenRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Token sent successfully");
                } else {
                    Log.e("FCM", "Failed to send token. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Error sending token: " + t.getMessage());
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Nếu là message kiểu notification
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            showNotification(title, body); // Hiển thị thông báo local
        }

        // Nếu là message kiểu data
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String body = data.get("body");

            showNotification(title, body);
        }
    }

    // Hàm hiển thị thông báo local
    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.main_logo) // icon nhỏ
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1001, builder.build());
    }

}
