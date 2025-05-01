package com.example.lineta.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.PostAdapter;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.client.ApiClient;
import com.example.lineta.Entity.Post;
import com.example.lineta.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.PostAdapter;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.client.ApiClient;
import com.example.lineta.Entity.Post;
import com.example.lineta.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
    List<Post> list = new ArrayList<>();
    PostAdapter adapter;
    ProgressBar progressBar;
    int currentPage = 1; // Trang hiện tại
    int pageSize = 5;    // Kích thước mỗi trang

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPost);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(list);
        recyclerView.setAdapter(adapter);

        // Lắng nghe sự kiện cuộn của RecyclerView
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) {  // Kiểm tra nếu cuộn đến cuối
                    loadMorePosts();
                }
            }
        });

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult().getToken();
                        ApiClient.setToken(token);
                        fetchPosts(currentPage);
                    } else {
                        Toast.makeText(getContext(), "Lỗi lấy Token!", Toast.LENGTH_SHORT).show();
                    }
                });

        return view;
    }

    // Hàm để tải thêm bài viết khi cuộn đến cuối
    private void loadMorePosts() {
        progressBar.setVisibility(View.VISIBLE);  // Hiện ProgressBar
        new Handler().postDelayed(() -> {
            currentPage++;  // Tăng trang
            fetchPosts(currentPage);  // Tải thêm bài post
        }, 1500);  // Delay 1.5 giây để tạo hiệu ứng "đang tải"
    }

    private void fetchPosts(int page) {
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        Call<List<Post>> call = apiService.getPosts(page, pageSize); // Sử dụng trang và kích thước trang

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Nếu là trang đầu tiên, xóa danh sách cũ
                    if (page == 1) {
                        list.clear();
                    }
                    list.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar khi tải xong
                } else {
                    Toast.makeText(getContext(), "Failed to load posts!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar nếu có lỗi
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);  // Ẩn ProgressBar nếu có lỗi
            }
        });
    }
}


