package com.example.lineta.service;

import com.example.lineta.dto.request.UserFollowRequest;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserFollowResponse;
import com.example.lineta.dto.response.UserSearchResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
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

    @POST("api/friend/users/follow")
    Call<ApiResponse<Void>> follow(@Body UserFollowRequest request);

//    @DELETE("api/friend/users/unfollow")
    @HTTP(method = "DELETE", path = "api/friend/users/unfollow", hasBody = true) // vì DELETE của retrofit không cho dùng body nên phải dùng cái này
    Call<ApiResponse<Void>> unfollow(@Body Map<String, String> body);

    @GET("api/friend/users/check-following")
    Call<ApiResponse<Boolean>> isFollowing(
            @Query("followerId") String followerId,
            @Query("followedId") String followedId
    );
}
