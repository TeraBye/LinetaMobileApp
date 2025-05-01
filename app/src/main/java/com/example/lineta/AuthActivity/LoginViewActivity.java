package com.example.lineta.AuthActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lineta.Home.HomeViewActivity;
import com.example.lineta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginViewActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnRegisterLogin, btnLoginEnter;
    FirebaseAuth mAuth;
    Intent intentLoginView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_view);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegisterLogin = findViewById(R.id.btnRegisterLogin);
        btnLoginEnter = findViewById(R.id.btnLoginEnter);

        mAuth = FirebaseAuth.getInstance();

        btnRegisterLogin.setOnClickListener(v -> {
            intentLoginView = new Intent(LoginViewActivity.this, RegisterViewActivity.class);
            startActivity(intentLoginView);
            finish();
        });

        btnLoginEnter.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.getIdToken(true).addOnSuccessListener(tokenResult -> {
                                    String idToken = tokenResult.getToken();
                                    sendTokenToBackend(idToken);
                                });
                            }
                        } else {
                            Toast.makeText(LoginViewActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void sendTokenToBackend(String idToken) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:8080/auth/login")
                .addHeader("Authorization", "Bearer " + idToken)
                .post(okhttp3.RequestBody.create(null, new byte[0])) // POST rỗng
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginViewActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(LoginViewActivity.this, HomeViewActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    runOnUiThread(() ->
                            Toast.makeText(LoginViewActivity.this, "Login failed: " + errorBody, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

}
