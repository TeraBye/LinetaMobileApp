package com.example.lineta;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lineta.AuthActivity.LoginActivity;
import com.example.lineta.Entity.DeviceNoti.TokenRequest;
import com.example.lineta.Home.HomeViewActivity;
import com.example.lineta.service.DeviceNotiApi;
import com.example.lineta.service.client.ApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Intent intent;
                if (currentUser != null) {
                    intent = new Intent(MainActivity.this, HomeViewActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 1500);

//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (!task.isSuccessful()) {
//                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
//                        return;
//                    }
//
//                    // Lấy token
//                    String token = task.getResult();
//                    Log.d("FCM", "Token (manual fetch): " + token);
//
//                    // Gửi lên server
//                    sendTokenToServer(token);
//                });
//
//
//    }
//    private void sendTokenToServer(String token) {
//        DeviceNotiApi apiService = ApiClient.getRetrofit().create(DeviceNotiApi.class);
//        TokenRequest tokenRequest = new TokenRequest(token, "terabye");
//
//        Call<Void> call = apiService.sendTokenToServer(tokenRequest);
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    Log.d("FCM", "Token sent successfully from MainActivity");
//                } else {
//                    Log.e("FCM", "Failed to send token. Code: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Log.e("FCM", "Error sending token: " + t.getMessage());
//            }
//        });
    }

}