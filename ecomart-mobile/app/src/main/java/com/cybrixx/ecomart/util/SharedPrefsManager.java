package com.cybrixx.ecomart.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static final String PREF_NAME = "ecomart_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ROLE = "user_role";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAuthData(String token, String role, String firstName, String lastName, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("JWT_TOKEN", token);
        editor.putString("USER_ROLE", role);
        editor.putString("FIRST_NAME", firstName);
        editor.putString("LAST_NAME", lastName);
        editor.putString("EMAIL", email);
        editor.apply();
    }

    public String getFirstName() { return sharedPreferences.getString("FIRST_NAME", "Eco"); }
    public String getLastName() { return sharedPreferences.getString("LAST_NAME", "Customer"); }
    public String getEmail() { return sharedPreferences.getString("EMAIL", "customer@ecomart.lk"); }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
