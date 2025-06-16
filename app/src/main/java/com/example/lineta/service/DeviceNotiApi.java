package com.example.lineta.service;

import com.example.lineta.Entity.DeviceNoti.TokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DeviceNotiApi {
    @POST("/api/auth/fcm/token")
    Call<Void> sendTokenToServer(@Body TokenRequest request);

    @DELETE("api/auth/deleteToken/{username}")
    Call<Void> deleteToken(@Path("username") String username);

}
