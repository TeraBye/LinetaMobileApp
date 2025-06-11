package com.example.lineta.service;


import com.example.lineta.Entity.Comment;
import com.example.lineta.Entity.CommentLike;
import com.example.lineta.Entity.Like;
import com.example.lineta.Entity.Post;
import com.example.lineta.dto.response.ApiResponse;


import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

    @POST("api/posts/createPost")
    Call<ApiResponse<Void>> createPost(@Body Post postRequest);

    @GET("/api/likes/check")
    Call<ApiResponse<Map<String, Boolean>>> checkIfLiked(
            @Query("username") String username,
            @Query("postID") String postID
    );

    @POST("/api/likes/likes")
    Call<ApiResponse<Void>> toggleLike(@Body Like like);

    @GET("/api/likes/cmtLike/check")
    Call<ApiResponse<Map<String, Boolean>>> checkIfLikedCmt(
            @Query("username") String username,
            @Query("commentId") String commentId
    );

    @POST("/api/likes//cmtLike/likes")
    Call<ApiResponse<Void>> toggleLikeCmt(@Body CommentLike like);

    @POST("/api/comments/createComment")
    Call<ApiResponse<Void>> createComment(@Body Comment comment);

}
