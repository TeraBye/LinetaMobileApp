package com.example.lineta.AuthActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lineta.R;

public class RegisterViewActivity extends AppCompatActivity {

    Button btnLoginRegister;
    Intent intentRegisterView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_view);

        btnLoginRegister = findViewById(R.id.btnToRegister);

        btnLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentRegisterView = new Intent(RegisterViewActivity.this, LoginViewActivity.class);
                startActivity(intentRegisterView);
                finish();
            }
        });


    }
}