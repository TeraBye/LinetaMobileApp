package com.example.lineta.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lineta.Entity.Post;
import com.example.lineta.Entity.User;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.PostService;
import com.example.lineta.service.UserService;
import com.example.lineta.service.client.ApiClient;

import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CurrentUserViewModel extends ViewModel {
    MutableLiveData<User> currentUserLiveData = new MutableLiveData<>();
    MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public MutableLiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    private final MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> postCountLiveData = new MutableLiveData<>();

    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<Integer> getPostCountLiveData() {
        return postCountLiveData;
    }

    public void fetchCurrentUserInfo() {
        UserService userService = ApiClient.getRetrofit().create(UserService.class);
        Call<ApiResponse<User>> call = userService.getUserInfo();
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserLiveData.setValue(response.body().getResult());
                } else {
                    errorLiveData.setValue("Failed to load user info: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("CurrentUserViewModel", "Error fetching user info", t);
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
    }public void fetchPostsByUser(String username, int page, int size) {
        PostService postService = ApiClient.getRetrofit().create(PostService.class);
        Log.d("DEBUG", "Fetching posts for username: " + username + " | page=" + page + ", size=" + size);

        Call<ApiResponse<List<Post>>> call = postService.getPostsByUserId(username, page, size);
        Log.d("DEBUG", "Call object: " + call.toString());

        call.enqueue(new Callback<ApiResponse<List<Post>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Post>>> call, Response<ApiResponse<List<Post>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body().getResult();
                    Log.d("fetchPostsByUser", "Số lượng bài viết nhận được: " + (posts != null ? posts.size() : "null"));
                    postsLiveData.setValue(posts);
                    postCountLiveData.setValue(posts != null ? posts.size() : 0); // <- Đây là phần "đếm"
                } else {
                    Log.e("fetchPostsByUser", "Lỗi response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Post>>> call, Throwable t) {
                Log.e("fetchPostsByUser", "Lỗi kết nối", t);
            }
        });
    }


}
