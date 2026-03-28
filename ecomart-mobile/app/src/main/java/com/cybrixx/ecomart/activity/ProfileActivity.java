package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.model.UserProfileDTO;
import com.cybrixx.ecomart.network.RetrofitClient;
import com.cybrixx.ecomart.network.UserApi;
import com.cybrixx.ecomart.util.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPhone, etAddress;
    private Button btnSaveProfile, btnLogout,btnCallSupport;
    private UserApi userApi;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefsManager = new SharedPrefsManager(this);
        userApi = RetrofitClient.getInstance(this).create(UserApi.class);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnCallSupport = findViewById(R.id.btnCallSupport);

        setupBottomNavigation();
        fetchUserData();

        btnSaveProfile.setOnClickListener(v -> saveUserData());

        btnLogout.setOnClickListener(v -> {
            prefsManager.clearSession(); // Erase JWT Token
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity(); // Clear all activities
        });

        btnCallSupport.setOnClickListener(v -> {
            // The phone number for your "Store"
            String supportNumber = "0112345678";

            // ACTION_DIAL opens the phone app with the number typed in.
            // It does NOT require any special AndroidManifest permissions!
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + supportNumber));
            startActivity(dialIntent);
        });
    }

    private void fetchUserData() {
        userApi.getProfile().enqueue(new Callback<UserProfileDTO>() {
            @Override
            public void onResponse(Call<UserProfileDTO> call, Response<UserProfileDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileDTO user = response.body();
                    etFirstName.setText(user.getFirstName());
                    etLastName.setText(user.getLastName());
                    etEmail.setText(user.getEmail());
                    etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                    etAddress.setText(user.getAddress() != null ? user.getAddress() : "");
                }
            }

            @Override
            public void onFailure(Call<UserProfileDTO> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData() {
        UserProfileDTO updatedUser = new UserProfileDTO(
                etFirstName.getText().toString(),
                etLastName.getText().toString(),
                etEmail.getText().toString(),
                etPhone.getText().toString(),
                etAddress.getText().toString()
        );

        userApi.updateProfile(updatedUser).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();

                    // NEW: Save it locally so the Checkout screen can use it!
                    prefsManager.updateProfileData(
                            updatedUser.getFirstName(),
                            updatedUser.getLastName(),
                            updatedUser.getPhone(),
                            updatedUser.getAddress()
                    );
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(getApplicationContext(), OrderHistoryActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}