package com.example.lineta.service;

import com.example.lineta.Entity.Comment;
import com.example.lineta.Entity.ReplyComment;
import com.example.lineta.dto.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CommentApi {
    @GET("api/comments/getComments")
    Call<ApiResponse<List<Comment>>> getComments(
            @Query("postID") String postId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/comments/getReply")
    Call<ApiResponse<List<ReplyComment>>> getReplyComments(
            @Query("commentID") String commentId,
            @Query("page") int page,
            @Query("size") int size
    );
}

