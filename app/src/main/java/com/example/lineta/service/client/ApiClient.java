package com.example.lineta.service.client;

import com.example.lineta.intercepter.AuthInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor()) // Thêm Interceptor
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://localhost:9191/") // thay bằng base URL thật
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

