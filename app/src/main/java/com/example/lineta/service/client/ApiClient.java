package com.example.lineta.service.client;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:9000/"; // 👉 Thay server bạn vào
    private static Retrofit retrofit;
    private static String token = "";

    public static void setToken(String tokenValue) {
        token = tokenValue;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            clientBuilder.addInterceptor(chain -> {
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder();

                if (!TextUtils.isEmpty(token)) {
                    builder.header("Authorization", "Bearer " + token);
                }

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(clientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
