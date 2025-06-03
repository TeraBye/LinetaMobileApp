package com.example.lineta.service;

import com.example.lineta.Entity.Conversation;
import com.example.lineta.Entity.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiConversation {
    @GET("api/message/get-all-contact")
    Call<List<Conversation>> getConversations(
            @Query("userId") String userId);

    @GET("api/message/get-messages/{conversationId}")
    Call<List<Message>> getMessages(
            @Path("conversationId") String conversationId);
}
