package com.cybrixx.ecomart.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.util.SharedPrefsManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPrefsManager prefsManager = new SharedPrefsManager(this);

        // Add a tiny 1-second delay so the user actually sees your logo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            Intent intent;
            if (prefsManager.isLoggedIn()) {
                // Token exists and is NOT expired!
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                // No token, or it expired. Clear old data and go to Login.
                prefsManager.clearSession();
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish(); // Destroy the Splash screen so the user can't click 'Back' to it

        }, 1000); // 1000 milliseconds = 1 second
    }
}