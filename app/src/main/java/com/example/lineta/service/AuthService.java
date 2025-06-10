package com.example.lineta.service;

import com.example.lineta.dto.request.UserSignupRequest;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserSignupResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {
    @POST("api/auth/signup")
    Call<ApiResponse<UserSignupResponse>> signUp(@Body UserSignupRequest request);
    @GET("api/auth/check-username")
    Call<ApiResponse<Boolean>> checkUsernameExist(@Query("username") String username);
}
