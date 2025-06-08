package com.example.lineta.service;

import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserFollowResponse;
import com.example.lineta.dto.response.UserSearchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FriendService {
    @GET("api/friend/users/followers/{followedId}")
    Call<ApiResponse<List<UserFollowResponse>>> getFollowers(
            @Path("followedId") String followedId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/friend/users/following/{followerId}")
    Call<ApiResponse<List<UserFollowResponse>>> getFollowing(
            @Path("followerId") String followerId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/friend/users/friends/{uid}")
    Call<ApiResponse<List<UserFollowResponse>>> getFriends(
            @Path("uid") String uid,
            @Query("page") int page,
            @Query("size") int size
    );
}
