package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.LoginRequestDTO;
import com.cybrixx.ecomart.model.TokenDTO;
import com.cybrixx.ecomart.network.AuthApi;
import com.cybrixx.ecomart.network.RetrofitClient;
import com.cybrixx.ecomart.util.SharedPrefsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPrefsManager = new SharedPrefsManager(this);

        // Auto-login check: If token exists, skip login
        if (sharedPrefsManager.getToken() != null) {
            goToHome();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)); // [cite: 873]
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthApi authApi = RetrofitClient.getInstance(this).create(AuthApi.class);
        LoginRequestDTO request = new LoginRequestDTO(email, password);

        // Asynchronous network call [cite: 1833, 1840]
        authApi.userLogin(request).enqueue(new Callback<TokenDTO>() {
            @Override
            public void onResponse(Call<TokenDTO> call, Response<TokenDTO> response) {
                if (response.isSuccessful() && response.body() != null) { // [cite: 1841, 1852]
                    // Save JWT Token
                    sharedPrefsManager.saveAuthData(response.body().getToken(), response.body().getRole(),response.body().getFirstName(),
                            response.body().getLastName(),
                            response.body().getEmail()); //
                    Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    goToHome();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenDTO> call, Throwable t) { // [cite: 1845]
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToHome() {
         Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
         startActivity(intent);
         finish(); // Prevents user from going back to login screen
    }
}
