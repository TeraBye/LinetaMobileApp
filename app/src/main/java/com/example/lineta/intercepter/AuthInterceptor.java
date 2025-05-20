package com.example.lineta.intercepter;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Lấy token từ Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            try {
                Task<GetTokenResult> task = user.getIdToken(false);
                Tasks.await(task); // blocking call để chờ token

                String token = task.getResult().getToken();
                if (token != null) {
                    // Gắn token vào header Authorization
                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }
            } catch (Exception e) {
                e.printStackTrace(); // hoặc log lỗi
            }
        }

        return chain.proceed(originalRequest); // Không có token vẫn tiếp tục
    }
}

