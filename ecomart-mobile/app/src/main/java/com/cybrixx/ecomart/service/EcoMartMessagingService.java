package com.cybrixx.ecomart.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cybrixx.ecomart.util.NotificationHelper;
import com.cybrixx.ecomart.util.PushManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Receives pushes from the EcoMart backend.
 *
 * The backend sends "data" messages (no notification block). Those always reach this method, in the
 * foreground and the background, so the app controls the channel, icon and where a tap leads:
 *   type=ORDER_STATUS -> order update, opens the order's tracking screen
 *   type=OFFER        -> timed offer, opens Home where the offer banner is shown
 */
public class EcoMartMessagingService extends FirebaseMessagingService {

    private static final String TAG = "EcoMartFCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "Push received: type=" + data.get("type")
                + " hasNotificationBlock=" + (remoteMessage.getNotification() != null));
        String title = data.get("title");
        String body = data.get("body");

        // A message composed in the Firebase console has a notification block instead of data.
        if (title == null && remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }
        if (title == null && body == null) return;

        if ("ORDER_STATUS".equals(data.get("type"))) {
            NotificationHelper.showOrderUpdate(this, title, body, data);
        } else {
            NotificationHelper.showOffer(this, title, body);
        }
    }

    // Firebase rotates a device's token occasionally (reinstall, restore, cleared data).
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        PushManager.sendTokenToServer(this, token);
    }
}
