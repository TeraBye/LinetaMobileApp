package com.example.lineta.service;

import com.example.lineta.Entity.User;
import com.example.lineta.dto.response.ApiResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {
    @GET("api/auth/users/req")
    Call<ApiResponse<User>> getUserInfo();

    @GET("api/auth/users/{userId}")
    Call<ApiResponse<User>> getUserById(@Path("userId") String userId);

    @Multipart
    @PUT("api/auth/users/update-user/{userId}")
    Call<ResponseBody> updateUser(
            @Path("userId") String userId,
            @Part("fullName") RequestBody fullName,
            @Part("bio") RequestBody bio,
            @Part MultipartBody.Part file
    );

    @GET("api/auth/users/uid")
    Call<ApiResponse<String>> getUidByUsername(@Query("username") String username);

}
