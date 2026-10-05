package com.cybrixx.ecomart.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

public class SharedPrefsManager {
    private static final String PREF_NAME = "ecomart_prefs";
    private static final String KEY_TOKEN = "JWT_TOKEN";
    private static final String KEY_ROLE = "USER_ROLE";
    private static final String KEY_FIRST_NAME = "FIRST_NAME";
    private static final String KEY_LAST_NAME = "LAST_NAME";
    private static final String KEY_EMAIL = "EMAIL";
    private static final String KEY_PHONE = "PHONE";
    private static final String KEY_ADDRESS = "ADDRESS";

    public SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAuthData(String token, String role, String firstName, String lastName, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getFirstName() { return sharedPreferences.getString(KEY_FIRST_NAME, "Eco"); }
    public String getLastName() { return sharedPreferences.getString(KEY_LAST_NAME, "Customer"); }
    public String getEmail() { return sharedPreferences.getString(KEY_EMAIL, "customer@ecomart.lk"); }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
    public void updateProfileData(String firstName, String lastName, String phone, String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ADDRESS, address);
        editor.apply();
    }

    // Add these new getters
    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, "");
    }

    public String getAddress() {
        return sharedPreferences.getString(KEY_ADDRESS, "");
    }

    public boolean isLoggedIn() {
        String token = sharedPreferences.getString(KEY_TOKEN, null);

        // 1. If there's no token at all, they aren't logged in
        if (token == null || token.isEmpty()) {
            return false;
        }

        // 2. Decode the token to check the expiration date
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false; // Invalid JWT format

            // Decode the payload (the second part of the JWT)
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject jsonObject = new JSONObject(payload);

            // Get the expiration time ('exp' claim is in seconds)
            long expTimeInSeconds = jsonObject.getLong("exp");
            long currentTimeInSeconds = System.currentTimeMillis() / 1000;

            // Return true ONLY if the expiration time is in the future
            return expTimeInSeconds > currentTimeInSeconds;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // If anything goes wrong, force them to log in again
        }
    }
}
