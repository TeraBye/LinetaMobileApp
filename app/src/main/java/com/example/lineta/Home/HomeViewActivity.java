package com.example.lineta.Home;

import static com.android.volley.VolleyLog.TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.bumptech.glide.Glide;
import com.example.lineta.AuthActivity.LoginActivity;
import com.example.lineta.Entity.User;
import com.example.lineta.Home.conversation.ConversationFragment;
import com.example.lineta.Home.modalPost.CreatePostBottomSheet;
import com.example.lineta.Home.profile.AccountFragment;
import com.example.lineta.R;
import com.example.lineta.Search.SearchActivity;
import com.example.lineta.ViewModel.CurrentUserViewModel;
import com.example.lineta.ViewModel.UserViewModel;
import com.example.lineta.config.RawWebSocketManager;
import com.example.lineta.databinding.ActivityHomeViewBinding;
import com.example.lineta.dto.response.UnreadCountResponse;
import com.example.lineta.service.ApiConversation;
import com.example.lineta.service.DeviceNotiApi;
import com.example.lineta.service.client.ApiClient;
import com.example.lineta.service.client.WebSocketService;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.naiksoftware.stomp.StompClient;

public class HomeViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth mAuth;
    ActivityHomeViewBinding binding;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    UserViewModel userViewModel;
    CurrentUserViewModel currentUserViewModel;
    private String uid;
    private String token;
    private boolean isShowingProfile = false;

    private WebSocketService webSocketService;
    private boolean isServiceBound;
    private BroadcastReceiver unreadCountReceiver;
    private BadgeDrawable badge; // Khai báo badge làm biến instance
    private StompClient stompClient;
    private BadgeDrawable notificationBadge;
    private int notificationUnreadCount = 0;
    private SharedPreferences sharedPreferences;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this); // Bắt buộc cho version Android mới

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

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            currentUser.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    token = task.getResult().getToken();
                    // Fetch initial unread count and start WebSocketService
                    fetchInitialUnreadCount();
                } else {
                    Toast.makeText(this, "Không lấy được token", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "current user is null", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        // Drawer setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Chỉ add HomeFragment lần đầu tiên khi Activity tạo
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        binding.bottomNavigationView.setBackground(null);

        FloatingActionButton fab = findViewById(R.id.fabu); // chắc chắn trong layout của bạn có fab này
        fab.setOnClickListener(v -> {
            CreatePostBottomSheet bottomSheet = CreatePostBottomSheet.newInstance();
            bottomSheet.show(getSupportFragmentManager(), "CreatePostBottomSheet");
        });

        // Đăng ký receiver để cập nhật tổng số tin nhắn chưa đọc
        unreadCountReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.lineta.UNREAD_COUNT_UPDATE".equals(intent.getAction())) {
                    long totalUnread = intent.getLongExtra("totalUnread", 0);
                    updateUnreadBadge(totalUnread);
                }
            }
        };
        IntentFilter filter = new IntentFilter("com.example.lineta.UNREAD_COUNT_UPDATE");
        registerReceiver(unreadCountReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (isShowingProfile) {
                isShowingProfile = false; // Cho phép thay đổi fragment sau lần đầu
                return false; // Ngăn bottom navigation ghi đè ngay lập tức
            }

            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.friends) {
                selectedFragment = new FriendsFragment();
            } else if (itemId == R.id.message) {
                selectedFragment = ConversationFragment.newInstance(uid, token); // Truyền userId và token
            } else if (itemId == R.id.notification) {
                selectedFragment = new NotificationFragment();

                // Reset count và ẩn badge
                notificationUnreadCount = 0;
                if (notificationBadge != null) {
                    notificationBadge.setVisible(false);
                }
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;
        });

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        currentUserViewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);

        // Update sidebar user info
        currentUserViewModel.fetchCurrentUserInfo();
        currentUserViewModel.getCurrentUserLiveData().observe(this, user -> {
            if (user != null) {
                updateHeader(user);
            }
        });

        // Handle intent from SearchActivity
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("navigate_to_profile", false)) {
            String userId = intent.getStringExtra("selected_user_id");
            if (userId != null) {
                Log.e("User Id", userId);
                replaceFragment(AccountFragment.newInstance(userId));
                userViewModel.fetchUserInfo(userId);
                isShowingProfile = true;
            } else {
                Log.e("HomeViewActivity", "userId bị null");
            }
        } else {
            if (savedInstanceState == null) {
                replaceFragment(new HomeFragment());
            }
        }

        // Cập nhật badge ban đầu
        MenuItem messageItem = binding.bottomNavigationView.getMenu().findItem(R.id.message);
        if (messageItem != null) {
            badge = binding.bottomNavigationView.getOrCreateBadge(R.id.message);
            badge.setVisible(false);
        }
    }

    // Thêm phương thức lấy tổng số tin nhắn chưa đọc từ API
    private void fetchInitialUnreadCount() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:9191/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiConversation apiService = retrofit.create(ApiConversation.class);
        Call<UnreadCountResponse> call = apiService.getUnreadCount(uid);
        call.enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(Call<UnreadCountResponse> call, Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long initialUnreadCount = response.body().getUnreadCount();
                    // Khởi động WebSocketService với initialUnreadCount
                    Intent intentService = new Intent(HomeViewActivity.this, WebSocketService.class);
                    intentService.putExtra("userId", uid);
                    intentService.putExtra("initialUnreadCount", initialUnreadCount);
                    startService(intentService);
                    bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);
                    // Cập nhật badge và broadcast ban đầu
                    updateUnreadBadge(initialUnreadCount);
                    sendInitialUnreadBroadcast(initialUnreadCount);
                    Log.d(TAG, "Fetched initial unread count: " + initialUnreadCount);
                } else {
                    Log.e(TAG, "Failed to fetch unread count, response code: " + response.code());
                    // Fallback: Khởi động service với unreadCount = 0 nếu API fail
                    Intent intentService = new Intent(HomeViewActivity.this, WebSocketService.class);
                    intentService.putExtra("userId", uid);
                    intentService.putExtra("initialUnreadCount", 0);
                    startService(intentService);
                    bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);
                    updateUnreadBadge(0);
                    sendInitialUnreadBroadcast(0);
                }
            }

            @Override
            public void onFailure(Call<UnreadCountResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch unread count: " + t.getMessage());
                // Fallback: Khởi động service với unreadCount = 0 nếu API fail
                Intent intentService = new Intent(HomeViewActivity.this, WebSocketService.class);
                intentService.putExtra("userId", uid);
                intentService.putExtra("initialUnreadCount", 0);
                startService(intentService);
                bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);
                updateUnreadBadge(0);
                sendInitialUnreadBroadcast(0);
            }
        });
    }

    // Thêm phương thức gửi broadcast khi WebSocket kết nối
    private void sendInitialUnreadBroadcast(long initialUnread) {
        Intent intent = new Intent("com.example.lineta.UNREAD_COUNT_UPDATE").setPackage(getPackageName());
        intent.putExtra("totalUnread", initialUnread);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcasted initial totalUnread: " + initialUnread);
    }

    // Thêm phương thức để đồng bộ lại unreadCount từ WebSocketService
    private void syncUnreadCount() {
        if (webSocketService != null && isServiceBound) {
            long syncedUnreadCount = webSocketService.getTotalUnreadCount();
            Intent intent = new Intent("com.example.lineta.UNREAD_COUNT_UPDATE").setPackage(getPackageName());
            intent.putExtra("totalUnread", syncedUnreadCount);
            sendBroadcast(intent);
            Log.d(TAG, "Synced unread count: " + syncedUnreadCount);
        }
    }

    // Thêm phương thức gọi sync khi resume activity
    @Override
    protected void onResume() {
        super.onResume();
        syncUnreadCount(); // Đồng bộ lại unreadCount khi activity resume
    }

    private void updateHeader(User user) {
        Log.i("updateHeader User", user.getUsername());
        View headerView = navigationView.getHeaderView(0);

        ImageView avatarImage = headerView.findViewById(R.id.avatar);
        TextView usernameText = headerView.findViewById(R.id.tvUsernameHeader);

        usernameText.setText(user.getFullName());

        Glide.with(this)
                .load(user.getProfilePicURL())
                .placeholder(R.drawable.default_avatar)
                .into(avatarImage);

        connectWebSocket(user);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.drawer_account) {
            replaceFragment(new AccountFragment());
        } else if (item.getItemId() == R.id.drawer_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.drawer_settings) {
            replaceFragment(new SettingsFragment());
        } else if (item.getItemId() == R.id.drawer_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        // Lấy username đang đăng nhập (giả sử lấy từ FirebaseAuth)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Lấy username từ SharedPreferences
            String username = sharedPreferences.getString("username", null);
            if (username != null) {
                deleteTokenFromServer(username, () -> {
                    performSignOut();
                });
            } else {
                // Fallback: Lấy từ ViewModel nếu cần
                currentUserViewModel.getCurrentUserLiveData().observe(this, user -> {
                    if (user != null) {
                        String fallbackUsername = user.getUsername();
                        deleteTokenFromServer(fallbackUsername, () -> {
                            performSignOut();
                        });
                    }
                });
            }
        }

    }

    private void deleteTokenFromServer(String username, Runnable onSuccess) {
        DeviceNotiApi apiService = ApiClient.getRetrofit().create(DeviceNotiApi.class);
        Call<Void> call = apiService.deleteToken(username); // Giả sử bạn đã thêm header

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Token deleted for username: " + username);
                    onSuccess.run();
                } else {
                    Log.e("FCM", "Failed to delete token. Code: " + response.code() + ", Message: " + response.message());
                    onSuccess.run(); // Tiếp tục đăng xuất ngay cả khi thất bại
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Failed to delete token: " + t.getMessage());
                onSuccess.run(); // Tiếp tục đăng xuất ngay cả khi lỗi
            }
        });
    }

    private void performSignOut() {
        mAuth.signOut();
        Intent intent = new Intent(HomeViewActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateUnreadBadge(long totalUnread) {
        if (badge != null) {
            if (totalUnread > 0) {
                badge.setVisible(true);
                badge.setNumber((int) totalUnread); // Chuyển long sang int
            } else {
                badge.setVisible(false);
            }
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;
            Log.d(TAG, "Service connected");
            // Không cần lấy initialUnreadCount từ service nữa, vì đã truyền từ Activity
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
        }
        if (unreadCountReceiver != null) {
            unregisterReceiver(unreadCountReceiver);
        }
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }

    private RawWebSocketManager rawWebSocketManager; // khai báo biến instance

    private void connectWebSocket(User user) {
        if (rawWebSocketManager != null) {
            return; // Đã kết nối rồi, không kết nối lại
        }

        String url = "ws://localhost:9001/ws/websocket";
        String topic = "/topic/notifications/" + user.getUsername();

        rawWebSocketManager = new RawWebSocketManager(url);
        rawWebSocketManager.setListener(message -> {
            Log.d("RawWebSocket", "Received raw message: " + message);
            runOnUiThread(() -> {
                notificationUnreadCount++;
                updateNotificationBadge(notificationUnreadCount);
            });
        });

        rawWebSocketManager.connect(() -> {
            rawWebSocketManager.subscribe(topic);
        });
    }

    private void updateNotificationBadge(int count) {
        if (notificationBadge == null) {
            notificationBadge = binding.bottomNavigationView.getOrCreateBadge(R.id.notification);
        }
        notificationBadge.setVisible(true);
        notificationBadge.setNumber(count);
    }
}