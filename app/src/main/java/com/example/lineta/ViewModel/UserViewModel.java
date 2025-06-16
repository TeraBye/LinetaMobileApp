package com.example.lineta.ViewModel;

import android.content.SharedPreferences;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends ViewModel {
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> postCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> uidLiveData = new MutableLiveData<>();

    public LiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<Integer> getPostCountLiveData() {
        return postCountLiveData;
    }

    private boolean isFetching = false;

    public void fetchUserInfo(String userId) {
//        User cachedUser = loadUserFromCache(userId);
//        if (cachedUser != null) {
//            userLiveData.setValue(cachedUser);
//        }
        if (isFetching) return; // Tránh gọi API nhiều lần
        isFetching = true;

        UserService userService = ApiClient.getRetrofit().create(UserService.class);
        Call<ApiResponse<User>> call = userService.getUserById(userId);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                isFetching = false;
                if (response.isSuccessful() && response.body() != null) {
                    userLiveData.setValue(response.body().getResult());
                    Log.i("User Response (View Model)", response.body().getResult().getFullName());
//                    saveUserToCache(user, userId);
                } else {
                    errorLiveData.setValue("Failed to load user info: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                isFetching = false;
                Log.e("UserViewModel", "API error: ", t);
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchPostsByUser(String username, int page, int size) {
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

    public void fetchUidByUsername(String username) {
        if (isFetching) return; // Tránh gọi API nhiều lần
        isFetching = true;

        UserService userService = ApiClient.getRetrofit().create(UserService.class);
        Call<ApiResponse<String>> call = userService.getUidByUsername(username);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String uid = response.body().getResult();
                    uidLiveData.setValue(uid);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {

            }
        });
    }

//    private void saveUserToCache(User user, String userId) {
//        String cacheKey = userId != null ? "user_" + userId : "current_user";
//        SharedPreferences prefs = getSharedPreferences();
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(cacheKey + "_username", user.getUsername());
//        editor.putString(cacheKey + "_fullName", user.getFullName());
//        editor.putInt(cacheKey + "_postNum", user.getPostNum());
//        editor.putInt(cacheKey + "_followerNum", user.getFollowerNum());
//        editor.putInt(cacheKey + "_followingNum", user.getFollowingNum());
//        editor.putString(cacheKey + "_profilePicUrl", user.getProfilePicURL());
//        editor.apply();
//    }

//    private User loadUserFromCache(String userId) {
//        String cacheKey = userId != null ? "user_" + userId : "current_user";
//        SharedPreferences prefs = getSharedPreferences();
//        String username = prefs.getString(cacheKey + "_username", null);
//        if (username == null) return null;
//
//        User user = new User();
//        user.setUsername(username);
//        user.setFullName(prefs.getString(cacheKey + "_fullName", ""));
//        user.setPostNum(prefs.getInt(cacheKey + "_postNum", 0));
//        user.setFollowerNum(prefs.getInt(cacheKey + "_followerNum", 0));
//        user.setFollowingNum(prefs.getInt(cacheKey + "_followingNum", 0));
//        user.setProfilePicURL(prefs.getString(cacheKey + "_profilePicUrl", null));
//        return user;
//    }

//    private SharedPreferences getSharedPreferences() {
//        // Replace with your Application context or pass context if needed
//        return androidx.preference.PreferenceManager.getDefaultSharedPreferences(ApiClient.getContext());
//    }


    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}
