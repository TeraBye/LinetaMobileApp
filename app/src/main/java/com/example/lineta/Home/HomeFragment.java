package com.example.lineta.Home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.PostAdapter;
import com.example.lineta.Entity.Post;
import com.example.lineta.R;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.ApiService;
import com.example.lineta.service.client.ApiClient;
import com.example.lineta.config.WebSocketManager;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Post> list = new ArrayList<>();
    private PostAdapter adapter;
    private View loadingOverlay; // Lớp phủ loading
    private ProgressBar progressBar;
    private int currentPage = 1;
    private int pageSize = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPost);
        progressBar = view.findViewById(R.id.progressBar); // ProgressBar trong overlay
        loadingOverlay = view.findViewById(R.id.loadingOverlay); // Lấy overlay từ layout

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) {
                    loadMorePosts();
                }
            }
        });

        fetchPosts(currentPage);
        setupWebSocket();

        return view;
    }

    private void loadMorePosts() {
        showLoading(true);
        new Handler().postDelayed(() -> {
            currentPage++;
            fetchPosts(currentPage);
        }, 1500);
    }

    private void fetchPosts(int page) {
        showLoading(true);
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        Call<ApiResponse<List<Post>>> call = apiService.getPosts(page, pageSize);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body().getResult();
                    if (page == 1) {
                        list.clear();
                    }
                    list.addAll(posts);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load posts!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Post>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupWebSocket() {
        WebSocketManager.getInstance().setListener(jsonObject -> {
            try {
                Post newPost = new Post();
                newPost.setPostId(jsonObject.getString("postId"));
                newPost.setContent(jsonObject.getString("content"));
                newPost.setVideo(jsonObject.optString("video", null));
                newPost.setFullName(jsonObject.optString("fullName", null));
                newPost.setProfilePicURL(jsonObject.optString("profilePicURL", null));
                newPost.setPicture(jsonObject.optString("picture", null));

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    list.add(0, newPost);
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        WebSocketManager.getInstance().connect("ws://localhost:9000/ws/websocket", "/topic/posts");
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WebSocketManager.getInstance().disconnect();
    }
}
