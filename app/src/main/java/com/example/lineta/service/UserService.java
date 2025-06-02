package com.example.lineta.service;

import com.example.lineta.Entity.User;
import com.example.lineta.dto.response.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserService {
    @GET("api/auth/users/req")
    Call<ApiResponse<User>> getUserInfo();

    @GET("api/auth/users/{userId}")
    Call<ApiResponse<User>> getUserById(@Path("userId") String userId);

}
