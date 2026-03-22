package com.cybrixx.ecomart.activity;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.RegisterRequestDTO;
import com.cybrixx.ecomart.model.TokenDTO;
import com.cybrixx.ecomart.network.AuthApi;
import com.cybrixx.ecomart.network.RetrofitClient;
import com.cybrixx.ecomart.util.SharedPrefsManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPrefsManager = new SharedPrefsManager(this);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        // Navigate back to Login screen
        tvGoToLogin.setOnClickListener(v -> {
            finish(); // Closes RegisterActivity and returns to LoginActivity
        });
    }

    private void registerUser() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Disable the button so the user can't double-click
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        AuthApi authApi = RetrofitClient.getInstance(this).create(AuthApi.class);
        RegisterRequestDTO request = new RegisterRequestDTO(firstName, lastName, email, password);

        authApi.userRegister(request).enqueue(new Callback<TokenDTO>() {
            @Override
            public void onResponse(Call<TokenDTO> call, Response<TokenDTO> response) {
                // 2. Re-enable the button
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");

                if (response.isSuccessful() && response.body() != null) {
                    sharedPrefsManager.saveAuthData(response.body().getToken(), response.body().getRole(),response.body().getFirstName(),
                            response.body().getLastName(),
                            response.body().getEmail());
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    goToHome(); // We are about to create this!
                } else {
                    // 3. Catch the EXACT error from the backend instead of guessing
                    try {
                        String serverError = response.errorBody() != null ? response.errorBody().string() : "Unknown Error";
                        Toast.makeText(RegisterActivity.this, "Error " + response.code() + ": " + serverError, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<TokenDTO> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToHome() {
         Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
         startActivity(intent);
         finishAffinity(); // Clears all previous activities so the user can't press 'Back' to return to auth screens
    }
}
