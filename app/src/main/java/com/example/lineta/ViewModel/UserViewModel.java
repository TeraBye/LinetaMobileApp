package com.example.lineta.ViewModel;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lineta.Entity.User;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.UserService;
import com.example.lineta.service.client.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends ViewModel {
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private boolean isFetching = false;

    public void fetchUserInfo() {
        fetchUserInfo(null);
    }
    public void fetchUserInfo(String userId) {
//        User cachedUser = loadUserFromCache(userId);
//        if (cachedUser != null) {
//            userLiveData.setValue(cachedUser);
//        }
        if (isFetching) return; // Tránh gọi API nhiều lần
        isFetching = true;

        UserService userService = ApiClient.getRetrofit().create(UserService.class);
        Call<ApiResponse<User>> call = userId == null ? userService.getUserInfo() : userService.getUserById(userId);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                isFetching = false;
                if (response.isSuccessful() && response.body() != null) {
                    userLiveData.setValue(response.body().getResult());
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
