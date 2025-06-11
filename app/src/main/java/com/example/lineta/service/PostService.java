package com.example.lineta.service;

import com.example.lineta.Entity.Post;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserFollowResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostService {
    @GET("api/posts/getPosts-user")
    Call<ApiResponse<List<Post>>> getPostsByUserId(
            @Query("username") String username,
            @Query("page") int page,
            @Query("size") int size
    );

}
