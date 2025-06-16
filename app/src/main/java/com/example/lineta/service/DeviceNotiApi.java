package com.example.lineta.service;

import com.example.lineta.Entity.DeviceNoti.TokenRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeviceNotiApi {
    @POST("/api/notifications/fcm/token")
    Call<Void> sendTokenToServer(@Body TokenRequest request);
}
