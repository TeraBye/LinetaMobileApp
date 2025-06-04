package com.example.lineta.service;

import com.example.lineta.Entity.Conversation;
import com.example.lineta.Entity.Message;
import com.example.lineta.service.client.ApiClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiConversation {
    @GET("api/message/get-all-contact")
    Call<List<Conversation>> getConversations();

    @POST("api/message/get-details-messages")
    Call<List<Message>> getMessages(
            @Body Map<String, String> requestData);
}
//ApiConversation apiConversation = ApiClient.getRetrofit().create(ApiConversation.class);
//Call<List<Message>> call = apiConversation.getMessages(conversationId);