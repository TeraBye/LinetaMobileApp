package com.example.lineta.AuthActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lineta.Home.HomeViewActivity;
import com.example.lineta.R;

public class LoginViewActivity extends AppCompatActivity {
    Button btnRegisterLogin, btnLoginEnter;
    Intent intentLoginView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_view);

        btnRegisterLogin = findViewById(R.id.btnRegisterLogin);
        btnLoginEnter = findViewById(R.id.btnLoginEnter);

        btnRegisterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentLoginView = new Intent(LoginViewActivity.this, RegisterViewActivity.class);
                startActivity(intentLoginView);
                finish();
            }
        });

        btnLoginEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentLoginView = new Intent(LoginViewActivity.this, HomeViewActivity.class);
                startActivity(intentLoginView);
                finish();
            }
        });    }
}