package com.example.lineta.AuthActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lineta.R;
import com.example.lineta.dto.request.UserSignupRequest;
import com.example.lineta.dto.response.ApiResponse;
import com.example.lineta.dto.response.UserSignupResponse;
import com.example.lineta.service.AuthService;
import com.example.lineta.service.client.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterViewActivity extends AppCompatActivity {

    Button btnLoginRegister, btnRegisterEnter;
    EditText etxtEmail, etxtUsernameRegister, etxtFullnameRegister, etxtPasswordRegister, etxtRePasswordRegister;
    ProgressBar progressBarRegister;
    Intent intentRegisterView;
    AuthService authService;
    ImageView ivTogglePassword, ivToggleRePassword;
    boolean isPasswordVisible = false;
    boolean isRePasswordVisible = false;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_view);

        etxtEmail = findViewById(R.id.etxtEmail);
        etxtUsernameRegister = findViewById(R.id.etxtUsernameRegister);
        etxtFullnameRegister = findViewById(R.id.etxtFullnameRegister);
        etxtPasswordRegister = findViewById(R.id.etxtPasswordRegister);
        etxtRePasswordRegister = findViewById(R.id.etxtRePasswordRegister);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleRePassword = findViewById(R.id.ivToggleRePassword);
        progressBarRegister = findViewById(R.id.progressBarRegister);

        // Register click
        btnRegisterEnter = findViewById(R.id.btnRegisterEnter);
        btnRegisterEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    progressBarRegister.setVisibility(View.VISIBLE);
                    btnRegisterEnter.setEnabled(false);
//                    authService = ApiClient.getRetrofit().create(AuthService.class);
                    Call<ApiResponse<UserSignupResponse>> call = authService.signUp(UserSignupRequest.builder()
                            .email(etxtEmail.getText().toString().trim())
                            .fullName(etxtFullnameRegister.getText().toString().trim())
                            .username(etxtUsernameRegister.getText().toString().trim())
                            .password(etxtPasswordRegister.getText().toString().trim())
                            .build());
                    call.enqueue(new Callback<ApiResponse<UserSignupResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UserSignupResponse>> call, Response<ApiResponse<UserSignupResponse>> response) {
                            progressBarRegister.setVisibility(View.GONE); // Ẩn ProgressBar
                            btnRegisterEnter.setEnabled(true); // Bật lại nút
                            if (response.isSuccessful() && response.body() != null && response.body().getCode() == 1000) {
                                Toast.makeText(RegisterViewActivity.this, "Signup successfully", Toast.LENGTH_SHORT).show();
                                intentRegisterView = new Intent(RegisterViewActivity.this, LoginViewActivity.class);
                                startActivity(intentRegisterView);
                                finish();
                            } else {
                                ApiResponse<Void> errorResponse = parseErrorWithGson(response);
                                String errorMessage = (errorResponse != null && errorResponse.getMessage() != null)
                                        ? errorResponse.getMessage()
                                        : "Signup failed: Unknown error";
                                Toast.makeText(RegisterViewActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<UserSignupResponse>> call, Throwable t) {
                            progressBarRegister.setVisibility(View.GONE); // Ẩn ProgressBar
                            btnRegisterEnter.setEnabled(true); // Bật lại nút
                            String message = t.getMessage();
                            Toast.makeText(RegisterViewActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        // Login click
        btnLoginRegister = findViewById(R.id.btnToRegister);
        btnLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentRegisterView = new Intent(RegisterViewActivity.this, LoginViewActivity.class);
                startActivity(intentRegisterView);
                finish();
            }
        });

        // Show/hide password
        ivTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(etxtPasswordRegister, ivTogglePassword, isPasswordVisible);
                isPasswordVisible = !isPasswordVisible;
            }
        });

        ivToggleRePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(etxtRePasswordRegister, ivToggleRePassword, isRePasswordVisible);
                isRePasswordVisible = !isRePasswordVisible;
            }
        });
    }

    private ApiResponse<Void> parseErrorWithGson(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                Gson gson = new Gson();
                return gson.fromJson(response.errorBody().charStream(), new TypeToken<ApiResponse<Void>>() {}.getType());
            }
        } catch (Exception e) {
            Log.e("ParseError", "Exception when parsing error", e);
        }
        return null;
    }

    // Hide/show password
    private void togglePasswordVisibility(EditText editText, ImageView imageView, boolean isVisible) {
        if (isVisible) {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.ic_visibility_off);
        } else {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.ic_visibility);
        }

        // Move cursor to end
        editText.setSelection(editText.getText().length());
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        String email = etxtEmail.getText().toString().trim();
        String username = etxtUsernameRegister.getText().toString().trim();
        String fullName = etxtFullnameRegister.getText().toString().trim();
        String password = etxtPasswordRegister.getText().toString().trim();
        String rePassword = etxtRePasswordRegister.getText().toString().trim();

        /*Email*/
        if (email.isEmpty()) {
            errors.append("Email is required\n");
            etxtEmail.setError("Email is required");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.append("Invalid email format\n");
            etxtEmail.setError("Invalid email format");
        }

        /*Username*/
        if (username.isEmpty()) {
            errors.append("Username is required\n");
            etxtEmail.setError("Username is required");
        } else {
            authService = ApiClient.getRetrofit().create(AuthService.class);
            Call<ApiResponse<Boolean>> call = authService.checkUsernameExist(username);
            call.enqueue(new Callback<ApiResponse<Boolean>>() {
                @Override
                public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                    if (response.isSuccessful() && response.body().getCode() == 1000) {
                        if (response.body().getResult()) {
                            etxtUsernameRegister.setError(response.body().getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                    Toast.makeText(RegisterViewActivity.this, "Error checking username", Toast.LENGTH_LONG).show();
                }
            });
        }

        /*Fullname*/
        if (fullName.isEmpty()) {
            errors.append("Fullname is required\n");
            etxtFullnameRegister.setError("Fullname is required");
        }

        /*Password*/
        if (password.isEmpty()) {
            errors.append("Password is required\n");
            etxtPasswordRegister.setError("Password is required");
        } else if (password.length() < 6) {
            errors.append("Password must be at least 6 characters\n");
            etxtPasswordRegister.setError("Password must be at least 6 characters");
        }
        if (rePassword.isEmpty()) {
            errors.append("Re-enter password is required\n");
            etxtRePasswordRegister.setError("Re-enter password is required");
        }
        if (!password.equals(rePassword)) {
            errors.append("Passwords do not match\n");
            etxtRePasswordRegister.setError("Passwords do not match");
        }

        if (errors.length() > 0) {
            Toast.makeText(this, errors.toString(), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}