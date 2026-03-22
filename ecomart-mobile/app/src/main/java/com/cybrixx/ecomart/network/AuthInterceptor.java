package com.cybrixx.ecomart.network;

import android.content.Context;
import androidx.annotation.NonNull;
import java.io.IOException;
import com.cybrixx.ecomart.util.SharedPrefsManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();

        // Fetch the token from SharedPreferences
        SharedPrefsManager prefsManager = new SharedPrefsManager(context);
        String token = prefsManager.getToken();

        // If the user is logged in, attach the token to the header
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        return chain.proceed(requestBuilder.build());
    }
}
