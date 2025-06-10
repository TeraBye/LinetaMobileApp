package com.example.lineta.service;

import com.example.lineta.Entity.Conversation.Conversation;
import com.example.lineta.Entity.Conversation.Message;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface ApiConversation {
    @GET("api/message/get-all-contact")
    Call<List<Conversation>> getConversations();

    @POST("api/message/get-details-messages")
    Call<List<Message>> getMessages(
            @Body Map<String, String> requestData);

    @Multipart
    @POST("api/message/upload")
    Call<Map<String, Object>> sendMessage(
            @PartMap Map<String, RequestBody> params,
            @Part MultipartBody.Part[] listMediaFile
    );

    @POST("api/message/update-unread")
    Call<Void> updateUnread(@Body Map<String, String> requestData);
}