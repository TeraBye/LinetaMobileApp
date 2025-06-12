package com.example.lineta.AuthActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.lineta.Entity.Account;
import com.example.lineta.Home.HomeViewActivity;
import com.example.lineta.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginViewActivity extends AppCompatActivity {
    AutoCompleteTextView edtEmail;
    EditText edtPassword;
    Button btnRegisterLogin, btnLoginEnter;
    CheckBox chkSaveCredentials;
    ProgressBar progressBar;
    View dimBackground;
    TextView tvForgotPassword;
    ImageView togglePasswordVisibility;
    FirebaseAuth mAuth;
    Intent intentLoginView;
    SharedPreferences sharedPreferences;
    boolean isPasswordVisible = false;
    List<Account> savedAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_view);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegisterLogin = findViewById(R.id.btnRegisterLogin);
        btnLoginEnter = findViewById(R.id.btnLoginEnter);
        chkSaveCredentials = findViewById(R.id.chkSaveCredentials);
        progressBar = findViewById(R.id.progressBar);
        dimBackground = findViewById(R.id.dimBackground);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo SharedPreferences để lưu email và mật khẩu
        String masterKeyAlias;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "LoginPrefs",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to regular SharedPreferences if encryption fails
            sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        }

        // Tải danh sách tài khoản đã lưu
        loadSavedAccounts();

        // Thiết lập AutoCompleteTextView cho email
        List<String> emailList = new ArrayList<>();
        for (Account account : savedAccounts) {
            emailList.add(account.getEmail());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emailList);
        edtEmail.setAdapter(adapter);
        edtEmail.setThreshold(1);

        // Điền mật khẩu khi chọn email
        edtEmail.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEmail = (String) parent.getItemAtPosition(position);
            for (Account account : savedAccounts) {
                if (account.getEmail().equals(selectedEmail)) {
                    edtPassword.setText(account.getPassword());
                    break;
                }
            }
        });

        // Nếu có tài khoản, điền tài khoản gần nhất
        if (!savedAccounts.isEmpty()) {
            Account lastAccount = savedAccounts.get(savedAccounts.size() - 1);
            edtEmail.setText(lastAccount.getEmail());
            edtPassword.setText(lastAccount.getPassword());
        }

        // Xử lý nút hiện/ẩn mật khẩu
        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Ẩn mật khẩu
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = false;
            } else {
                // Hiện mật khẩu
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.ic_eye_on);
                isPasswordVisible = true;
            }
            // Di chuyển con trỏ đến cuối văn bản
            edtPassword.setSelection(edtPassword.getText().length());
        });

        btnRegisterLogin.setOnClickListener(v -> {
            intentLoginView = new Intent(LoginViewActivity.this, RegisterViewActivity.class);
            startActivity(intentLoginView);
            finish();
        });

        btnLoginEnter.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Xóa lỗi trước khi kiểm tra
            edtEmail.setError(null);
            edtPassword.setError(null);

            // Kiểm tra từng field
            boolean hasError = false;
            if (email.isEmpty()) {
                edtEmail.setError("Email is required");
                hasError = true;
            }
            if (password.isEmpty()) {
                edtPassword.setError("Password is required");
                hasError = true;
            }

            // Nếu có lỗi, dừng lại
            if (hasError) {
                return;
            }
            // Hiển thị ProgressBar và vô hiệu hóa nút đăng nhập
            Log.d("LoginViewActivity", "Showing ProgressBar");
            dimBackground.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            btnLoginEnter.setEnabled(false);

            // Lưu tài khoản nếu CheckBox được chọn
            if (chkSaveCredentials.isChecked()) {
                saveAccount(email, password);
            }

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
                            dimBackground.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            btnLoginEnter.setEnabled(true);
                            Toast.makeText(LoginViewActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Forgot Password click
        tvForgotPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginViewActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            dimBackground.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            btnLoginEnter.setEnabled(false);
            btnRegisterLogin.setEnabled(false);
            tvForgotPassword.setEnabled(false);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        dimBackground.setVisibility(View.GONE);
                        btnLoginEnter.setEnabled(true);
                        btnRegisterLogin.setEnabled(true);
                        tvForgotPassword.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginViewActivity.this, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginViewActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void saveAccount(String email, String password) {
        for (Account account : savedAccounts) {
            if (account.getEmail().equals(email)) {
                account.setPassword(password);
                saveAccountsToPrefs();
                return;
            }
        }
        savedAccounts.add(new Account(email, password));
        saveAccountsToPrefs();
    }

    private void saveAccountsToPrefs() {
        Gson gson = new Gson();
        String accountsJson = gson.toJson(savedAccounts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accounts", accountsJson);
        editor.apply();
    }

    private void loadSavedAccounts() {
        String accountsJson = sharedPreferences.getString("accounts", "[]");
        Gson gson = new Gson();
        Type accountListType = new TypeToken<List<Account>>() {
        }.getType();
        savedAccounts = gson.fromJson(accountsJson, accountListType);
        if (savedAccounts == null) {
            savedAccounts = new ArrayList<>();
        }
    }

    private void sendTokenToBackend(String idToken) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:9191/api/auth/login")
                .addHeader("Authorization", "Bearer " + idToken)
                .post(okhttp3.RequestBody.create(null, new byte[0])) // POST rỗng
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                            dimBackground.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            btnLoginEnter.setEnabled(true);
                            Toast.makeText(LoginViewActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    dimBackground.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    btnLoginEnter.setEnabled(true);
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(LoginViewActivity.this, HomeViewActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorBody = null;
                        try {
                            errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(LoginViewActivity.this, "Login failed: " + errorBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
