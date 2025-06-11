package com.example.lineta.service;

import com.example.lineta.Entity.Post;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserFollowResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostService {
    @GET("api/posts/getPosts-user")
    Call<ApiResponse<List<Post>>> getPostsByUserId(
            @Query("username") String username,
            @Query("page") int page,
            @Query("size") int size
    );

    @DELETE("/api/posts/deletePost/{postId}")
    Call<ApiResponse<Void>> deletePost(@Path("postId") String postId);

    @PUT("/api/posts/updatePost/{postId}")
    Call<ApiResponse<Void>> updatePostContent(
            @Path("postId") String postId,
            @Body Map<String, String> body
    );



}
