package com.example.lineta.ViewModel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lineta.Entity.User;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.service.UserService;
import com.example.lineta.service.client.ApiClient;

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
    }
}
