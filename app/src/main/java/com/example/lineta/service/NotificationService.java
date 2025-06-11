package com.example.lineta.service;

import com.example.lineta.Entity.Notification;
import com.example.lineta.dto.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NotificationService {
    @GET("/api/notifications/get/{username}")
    Call<ApiResponse<List<Notification>>> getNotifications(@Path("username") String username);
}
