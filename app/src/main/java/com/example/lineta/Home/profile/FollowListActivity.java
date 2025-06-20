package com.example.lineta.Home.profile;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.FollowAdapter;
import com.example.lineta.Adapter.UserSearchAdapter;
import com.example.lineta.Entity.User;
import com.example.lineta.Home.HomeViewActivity;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserFollowResponse;
import com.example.lineta.service.FriendService;
import com.example.lineta.service.client.ApiClient;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class FollowListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    EditText searchEditText;
    FollowAdapter followAdapter;
    List<UserFollowResponse> userList;
    //    List<UserFollowResponse> filteredList;
    String listType;
    String userId;
    int currentPage = 1;
    final int PAGE_SIZE = 5;
    boolean isLoading = false;
    boolean isLastPage = false;
    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_follow_list);

        userId = getIntent().getStringExtra("user_id");

        initViews();
        setupToolbar();
        setupSearch();
        setupRecyclerViewScrollListener();
        loadData();
    }

    // Thêm vào phần initViews() trong FollowListActivity.java
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        typeface = ResourcesCompat.getFont(this, R.font.agbalumo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
//        filteredList = new ArrayList<>();
        followAdapter = new FollowAdapter(userList, this);
        recyclerView.setAdapter(followAdapter);

        listType = getIntent().getStringExtra("type");
        String title = getIntent().getStringExtra("title");
        setTitle(title);

        // Setup tabs
        tabLayout.addTab(tabLayout.newTab().setText("Followers"));
        tabLayout.addTab(tabLayout.newTab().setText("Following"));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                TextView tabTextView = new TextView(this);
                tabTextView.setText(tab.getText());
                tabTextView.setTypeface(typeface);
                tabTextView.setTextColor(getResources().getColor(R.color.black, getTheme()));
                // Căn giữa TextView trong tab
                tabTextView.setGravity(Gravity.CENTER); // Sử dụng Gravity.CENTER thay vì View.TEXT_ALIGNMENT_CENTER
                tabTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                tab.setCustomView(tabTextView);

                // Kiểm tra và set màu cho tab được chọn mặc định
                if (i == 0 && "followers".equals(listType)) {
                    tabTextView.setTextColor(getResources().getColor(R.color.royal_ant, getTheme()));
                } else if (i == 1 && "following".equals(listType)) {
                    tabTextView.setTextColor(getResources().getColor(R.color.royal_ant, getTheme()));
                }
            }
        }

        // Select the appropriate tab based on the type
        if ("following".equals(listType)) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        } else {
            tabLayout.selectTab(tabLayout.getTabAt(0));
        }

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Lấy TextView của tab được chọn
                TextView tabTextView = (TextView) tab.getCustomView();
                if (tabTextView != null) {
                    tabTextView.setTextColor(getResources().getColor(R.color.royal_ant, getTheme()));
                }
                int position = tab.getPosition();
                if (position == 0) {
                    listType = "followers";
                    setTitle("Followers");
                } else {
                    listType = "following";
                    setTitle("Following");
                }
                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabTextView = (TextView) tab.getCustomView();
                if (tabTextView != null) {
                    tabTextView.setTextColor(getResources().getColor(R.color.black, getTheme()));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        // Setup click listeners
        followAdapter.setOnItemClickListener(userId -> {
            Intent intent = new Intent(FollowListActivity.this, HomeViewActivity.class);
            intent.putExtra("navigate_to_profile", true);
            intent.putExtra("selected_user_id", userId);
            startActivity(intent);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitleTextAppearance(this, R.style.Toolbart);
        setSupportActionBar(toolbar);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.agbalumo);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleText);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterUsers(String query) {
        List<UserFollowResponse> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(userList);
        } else {
            for (UserFollowResponse user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        user.getFullName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(user);
                }
            }
        }
        Log.d("FollowListActivity", "Filtered list size: " + filtered.size());
        followAdapter.setUsers(filtered); // Sử dụng setUsers
    }

    private void setupRecyclerViewScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    int lastVisiblePosition = firstVisibleItemPosition + visibleItemCount - 1;

                    Log.d("FollowListActivity", "Scrolled - visible: " + visibleItemCount + ", total: " + totalItemCount + ", last: " + lastVisiblePosition);
                    if (lastVisiblePosition == totalItemCount - 1 && totalItemCount > 0) {
                        loadMoreData();
                    }
                }
            }
        });
    }

    private void loadData() {
        currentPage = 1;
        isLastPage = false;
        userList.clear();
//        filteredList.clear();
        searchEditText.setText("");
        followAdapter.clearUsers();
        loadMoreData();
    }

    private void loadMoreData() {
        if (isLoading || isLastPage) return;

        isLoading = true;
        recyclerView.post(() -> followAdapter.setLoading(true));

        FriendService friendService = ApiClient.getRetrofit().create(FriendService.class);
        Call<ApiResponse<List<UserFollowResponse>>> call = "followers".equals(listType) ?
                friendService.getFollowers(userId, currentPage, PAGE_SIZE) :
                friendService.getFollowing(userId, currentPage, PAGE_SIZE);

        call.enqueue(new Callback<ApiResponse<List<UserFollowResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserFollowResponse>>> call, Response<ApiResponse<List<UserFollowResponse>>> response) {
                isLoading = false;
                followAdapter.setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<UserFollowResponse> newUsers = response.body().getResult();
                    if (newUsers.isEmpty() || newUsers.size() < PAGE_SIZE) {
                        isLastPage = true;
                    }
                    if (currentPage == 1) {
                        userList.clear();
                    }
                    userList.addAll(newUsers);
                    followAdapter.addUsers(newUsers);
                    currentPage++;
                } else {
                    Toast.makeText(FollowListActivity.this, "Failed to load data " + response.body(), Toast.LENGTH_SHORT).show();
                    Log.e("Failed to load data", response.body().getResult().toString());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserFollowResponse>>> call, Throwable t) {
                isLoading = false;
                followAdapter.setLoading(false);
                Toast.makeText(FollowListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}