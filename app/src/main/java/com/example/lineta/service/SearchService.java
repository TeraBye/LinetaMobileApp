package com.example.lineta.service;

import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserSearchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SearchService {
    @GET("api/auth/search/")
    Call<ApiResponse<List<UserSearchResponse>>> getSearch(
            @Query("q") String query
    );

    @GET("api/auth/search/{searcherId}")
    Call<ApiResponse<List<UserSearchResponse>>> getSearchHistory(@Path("searcherId") String searcherId);

    // API thêm lịch sử tìm kiếm
    @POST("api/auth/search/add-search-history")
    Call<ApiResponse<Void>> addSearchHistory(
            @Query("searcherId") String searcherId,
            @Query("targetUserId") String targetUserId
    );

    // API xóa một mục lịch sử tìm kiếm
    @DELETE("api/auth/search/delete-search-history")
    Call<ApiResponse<Void>> deleteSearchHistory(
            @Query("searcherId") String searcherId,
            @Query("targetUserId") String targetUserId
    );

    // API xóa toàn bộ lịch sử tìm kiếm
    @DELETE("api/auth/search/delete-all-search-history")
    Call<ApiResponse<Void>> deleteAllSearchHistory(@Query("searcherId") String searcherId);
}
