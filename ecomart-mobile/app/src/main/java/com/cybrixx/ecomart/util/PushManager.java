package com.cybrixx.ecomart.util;

import android.content.Context;
import android.util.Log;

import com.cybrixx.ecomart.model.FcmTokenDTO;
import com.cybrixx.ecomart.network.RetrofitClient;
import com.cybrixx.ecomart.network.UserApi;
import com.google.firebase.messaging.FirebaseMessaging;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Connects this device to push notifications on login and disconnects it on logout. */
public final class PushManager {

    private static final String TAG = "PushManager";
    private static final String OFFERS_TOPIC = "offers";

    private PushManager() {}

    /**
     * Call once the user is logged in. Subscribes to broadcast offers and tells the backend which
     * device belongs to this account, so order updates can be pushed to it.
     */
    public static void register(Context context) {
        Context app = context.getApplicationContext();
        try {
            FirebaseMessaging messaging = FirebaseMessaging.getInstance();
            messaging.subscribeToTopic(OFFERS_TOPIC).addOnCompleteListener(task ->
                    Log.d(TAG, "Subscribe to '" + OFFERS_TOPIC + "' topic: "
                            + (task.isSuccessful() ? "ok" : "FAILED " + task.getException())));
            messaging.getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    Log.d(TAG, "FCM token obtained (ends ..." + token.substring(Math.max(0, token.length() - 6)) + ")");
                    sendTokenToServer(app, token);
                } else {
                    // Typically an emulator image without Google Play services, or no network yet.
                    Log.w(TAG, "Could not get an FCM token", task.getException());
                }
            });
        } catch (IllegalStateException e) {
            Log.w(TAG, "Firebase is not available; push notifications are off", e);
        }
    }

    /** Uploads the device token for the logged-in user. Does nothing when logged out. */
    public static void sendTokenToServer(Context context, String fcmToken) {
        Context app = context.getApplicationContext();
        if (new SharedPrefsManager(app).getToken() == null) return;

        RetrofitClient.getInstance(app).create(UserApi.class)
                .registerFcmToken(new FcmTokenDTO(fcmToken))
                .enqueue(new LoggingCallback("register device"));
    }

    /**
     * Call BEFORE clearing the saved session. Stops this phone receiving the signed-out user's
     * order updates and offers.
     */
    public static void unregister(Context context) {
        Context app = context.getApplicationContext();
        String jwt = new SharedPrefsManager(app).getToken();
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(OFFERS_TOPIC);
        } catch (IllegalStateException e) {
            Log.w(TAG, "Firebase is not available", e);
        }
        if (jwt != null) {
            RetrofitClient.getInstance(app).create(UserApi.class)
                    .unregisterFcmToken("Bearer " + jwt)
                    .enqueue(new LoggingCallback("unregister device"));
        }
    }

    private static final class LoggingCallback implements Callback<ResponseBody> {
        private final String what;

        LoggingCallback(String what) {
            this.what = what;
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (!response.isSuccessful()) {
                Log.w(TAG, "Failed to " + what + ": HTTP " + response.code());
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            // Best-effort: the token is re-sent on the next app start.
            Log.w(TAG, "Failed to " + what, t);
        }
    }
}
