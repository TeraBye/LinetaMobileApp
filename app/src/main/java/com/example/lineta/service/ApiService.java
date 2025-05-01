package com.example.lineta.service;

import com.example.lineta.Entity.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("posts")
    Call<List<Post>> getPosts(
            @Query("page") int page,
            @Query("size") int size
    );
}
