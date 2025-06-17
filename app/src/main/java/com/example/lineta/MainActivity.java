package com.example.lineta;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.lineta.AuthActivity.LoginActivity;
import com.example.lineta.Entity.DeviceNoti.TokenRequest;
import com.example.lineta.Home.HomeViewActivity;
import com.example.lineta.ViewModel.CurrentUserViewModel;
import com.example.lineta.service.DeviceNotiApi;
import com.example.lineta.service.client.ApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo SharedPreferences
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "LoginPrefs",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to regular SharedPreferences if encryption fails
            sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        }

        // Kiểm tra username trong SharedPreferences
        String username = sharedPreferences.getString("username", null);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Intent intent;
                if (currentUser != null) {
                    intent = new Intent(MainActivity.this, HomeViewActivity.class);
                    // Nếu chưa có username, lấy thông tin user
                    if (username == null) {
                        CurrentUserViewModel viewModel = new ViewModelProvider(MainActivity.this).get(CurrentUserViewModel.class);
                        viewModel.fetchCurrentUserInfo();
                        viewModel.getCurrentUserLiveData().observe(MainActivity.this, user -> {
                            if (user != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", user.getUsername());
                                editor.apply();
                                sendTokenToServer(user.getUsername());
                            }
                        });
                    } else {
                        sendTokenToServer(username);
                    }
                } else {
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 1500);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Lấy token
                    String token = task.getResult();
                    Log.d("FCM", "Token (manual fetch): " + token);

                    // Gửi lên server
                    sendTokenToServer(token);
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            CharSequence name = "FCM Notifications";
            String description = "Firebase Cloud Messaging";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }


    }

    private void sendTokenToServer(String username) {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM", "Token: " + token);

                    TokenRequest tokenRequest = new TokenRequest(token, username);
                    DeviceNotiApi apiService = ApiClient.getRetrofit().create(DeviceNotiApi.class);
                    Call<Void> call = apiService.sendTokenToServer(tokenRequest);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d("FCM", "Token sent successfully with username: " + username);
                            } else {
                                Log.e("FCM", "Failed to send token. Code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("FCM", "Error sending token: " + t.getMessage());
                        }
                    });
                });
    }


}