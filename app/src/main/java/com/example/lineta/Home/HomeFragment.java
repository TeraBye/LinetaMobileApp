package com.example.lineta.Home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lineta.Adapter.PostAdapter;
import com.example.lineta.Entity.Post;
import com.example.lineta.R;
import com.example.lineta.ViewModel.CurrentUserViewModel;
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

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Post> list = new ArrayList<>();
    private PostAdapter adapter;
    private CurrentUserViewModel currentUserViewModel;
    private View loadingOverlay;
    private ProgressBar progressBar;
    private int currentPage = 1;
    private int pageSize = 5;
    private boolean isDataLoaded = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    private WebSocketManager postSocketManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPost);
        progressBar = view.findViewById(R.id.progressBar);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUserViewModel = new ViewModelProvider(requireActivity()).get(CurrentUserViewModel.class);
        if (currentUserViewModel.getCurrentUserLiveData().getValue() == null) {
            currentUserViewModel.fetchCurrentUserInfo();}

        currentUserViewModel.getCurrentUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            if (adapter == null) {
                adapter = new PostAdapter(requireContext(), list, user);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.setCurrentUser(user); // ← bạn cần viết thêm setter nếu chưa có
            }


        });

        // Cuộn đến cuối để load thêm posts (chỉ khi adapter không null)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && adapter != null) {
                    loadMorePosts();
                }
            }
        });
        fetchPosts(currentPage);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            fetchPosts(currentPage);
        });

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
        if (page == 1) {
            swipeRefreshLayout.setRefreshing(true);
            showLoading(false);
        } else {
            showLoading(true);
        }

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        Call<ApiResponse<List<Post>>> call = apiService.getPosts(page, pageSize);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
                if (page == 1) swipeRefreshLayout.setRefreshing(false);
                else showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body().getResult();
                    if (page == 1) {
                        list.clear();
                    }
                    list.addAll(posts);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load posts!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Post>>> call, Throwable t) {
                if (page == 1) swipeRefreshLayout.setRefreshing(false);
                else showLoading(false);

                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupWebSocket() {
        if (postSocketManager != null) {
            return; // Đã kết nối rồi, không kết nối lại
        }
        String wsUrl = "ws://localhost:9000/ws/websocket"; // hoặc lấy từ config

        postSocketManager = new WebSocketManager(wsUrl);
        postSocketManager.setListener(jsonObject -> {
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
                    if (adapter != null) {
                        adapter.notifyItemInserted(0);
                        Log.d("WebSocket: post",newPost.getContent());
                        if (isAdded() && !isDetached()) {
                            recyclerView.scrollToPosition(0);
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        postSocketManager.connect(() -> {
            postSocketManager.subscribe("/topic/posts");
        });
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        WebSocketManager.getInstance().disconnect();
//    }
}
