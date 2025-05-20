package com.example.lineta.service;

import com.example.lineta.Entity.Comment;
import com.example.lineta.Entity.Post;
import com.example.lineta.dto.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/posts/getPosts")
    Call<ApiResponse<List<Post>>> getPosts(
            @Query("page") int page,
            @Query("size") int size
    );


    @GET("api/comments/getComments")  // Giả sử API endpoint là /comments
    Call<ApiResponse<List<Comment>>> getComments(
            @Query("postId") String postId
    );
}
