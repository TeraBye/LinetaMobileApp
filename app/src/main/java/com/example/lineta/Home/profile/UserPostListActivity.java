package com.example.lineta.Home.profile;


import static android.content.Intent.getIntent;
import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.PostAdapter;
import com.example.lineta.Entity.Post;
import com.example.lineta.Entity.User;
import com.example.lineta.R;
import com.example.lineta.ViewModel.CurrentUserViewModel;
import com.example.lineta.ViewModel.UserViewModel;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.PostService;
import com.example.lineta.service.client.ApiClient;

import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPostListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();

    private CurrentUserViewModel currentUserViewModel;
    private UserViewModel userViewModel;

    private ApiService apiService;
    private View loadingOverlay;
    private Toolbar toolbar;

    private String username; // bạn truyền từ intent sang
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_post_list);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lấy username từ Intent
        username = getIntent().getStringExtra("username");
        getSupportActionBar().setTitle("Posts ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

// Xử lý click back trên toolbar
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        currentUserViewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);

        currentUserViewModel.fetchCurrentUserInfo();
        currentUserViewModel.getCurrentUserLiveData().observe(this, user -> {
            Log.d("User1", "user1" +  user);
            if (user == null) return;
            postList = new ArrayList<>();
            postAdapter = new PostAdapter(this, postList, user);
            recyclerView.setAdapter(postAdapter);
            Log.d("UserPostListActivity", "Username received: " + username);
            if (username != null) {
                fetchPostsByUsername(username, 0);
            } else {
                Toast.makeText(this, "Không có username", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchPostsByUsername(String username, int page) {
//        showLoading(true);
        PostService postService = ApiClient.getRetrofit().create(PostService.class);

        Call<ApiResponse<List<Post>>> call = postService.getPostsByUserId(username, page, 30);

        call.enqueue(new Callback<ApiResponse<List<Post>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
//                showLoading(false);
                Log.d("API Response", "Response: " + response);
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body().getResult();
                    Log.d("Post Response", "Post: " + posts);
                    if (page == 0) {
                        postList.clear();
                    }
                    postList.addAll(posts);
                    postAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UserPostListActivity.this, "Failed to load posts!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Post>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(UserPostListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void fetchPostsByUsername(String username, int page, int size) {
//        apiService.getPostsByUserId(username, page, size).enqueue(new Callback<ApiResponse<List<Post>>>() {
//            @Override
//            public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    postList.clear();
//                    postList.addAll(response.body().getData());
//                    postAdapter.notifyDataSetChanged();
//                } else {
//                    Toast.makeText(UserPostListActivity.this, "Không load được dữ liệu", Toast.LENGTH_SHORT).show();
//                    Log.e("API_ERROR", "Response error: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse<List<Post>>> call, Throwable t) {
//                Toast.makeText(UserPostListActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                Log.e("API_ERROR", "Throwable: ", t);
//            }
//        });
//    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
