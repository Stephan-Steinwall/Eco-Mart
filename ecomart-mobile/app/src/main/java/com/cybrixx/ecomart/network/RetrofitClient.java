package com.cybrixx.ecomart.network;

import android.content.Context;
import com.cybrixx.ecomart.util.SharedPrefsManager;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class RetrofitClient {
    // Change this to your actual computer's local IP address if testing on a physical phone,
    // or keep 10.0.2.2 if using the Android Studio Emulator!
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {

            // 1. Create the Interceptor to attach the JWT Token
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    SharedPrefsManager prefs = new SharedPrefsManager(context);
                    String token = prefs.sharedPreferences.getString("JWT_TOKEN", "");

                    Request originalRequest = chain.request();

                    // If we have a token, add it to the header
                    if (!token.isEmpty()) {
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    }

                    return chain.proceed(originalRequest);
                }
            };

            // 2. Build the OkHttpClient with the Interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            // 3. Build Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Tell Retrofit to use our custom client!
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}