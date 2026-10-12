package com.cybrixx.ecomart.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.activity.HomeActivity;
import com.cybrixx.ecomart.activity.OrderHistoryActivity;
import com.cybrixx.ecomart.activity.TrackingActivity;

import java.util.Map;

/** Builds and shows the app's push notifications, and decides where tapping them leads. */
public final class NotificationHelper {

    public static final String CHANNEL_ORDERS = "ecomart_order_updates";
    public static final String CHANNEL_OFFERS = "ecomart_offers";

    private NotificationHelper() {}

    /** Safe to call repeatedly; Android ignores a channel that already exists. */
    public static void createChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        NotificationChannel orders = new NotificationChannel(
                CHANNEL_ORDERS, "Order updates", NotificationManager.IMPORTANCE_HIGH);
        orders.setDescription("Know when your order is being prepared, on the way, or delivered");

        NotificationChannel offers = new NotificationChannel(
                CHANNEL_OFFERS, "Offers & deals", NotificationManager.IMPORTANCE_DEFAULT);
        offers.setDescription("Limited-time offers from EcoMart");

        manager.createNotificationChannel(orders);
        manager.createNotificationChannel(offers);
    }

    /** Tapping opens that order's tracking screen; Back walks up through Orders to Home. */
    public static void showOrderUpdate(Context context, String title, String body, Map<String, String> data) {
        long orderId = parseLong(data.get("orderId"), -1);

        Intent tracking = new Intent(context, TrackingActivity.class)
                .putExtra("ORDER_ID", orderId)
                .putExtra("STATUS", data.get("status"))
                .putExtra("LATITUDE", parseDouble(data.get("latitude")))
                .putExtra("LONGITUDE", parseDouble(data.get("longitude")));

        // One PendingIntent per order, updated in place, so a newer status never opens with stale extras.
        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntent(new Intent(context, HomeActivity.class))
                .addNextIntent(new Intent(context, OrderHistoryActivity.class))
                .addNextIntent(tracking)
                .getPendingIntent((int) orderId, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Same id per order: "preparing" is replaced by "on the way" instead of stacking up.
        notify(context, CHANNEL_ORDERS, notificationId(orderId), title, body, pendingIntent);
    }

    /** Tapping opens Home, where the offer banner (with its live countdown) is shown. */
    public static void showOffer(Context context, String title, String body) {
        Intent intent = new Intent(context, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        notify(context, CHANNEL_OFFERS, (int) (System.currentTimeMillis() & 0x0FFFFFFF), title, body, pendingIntent);
    }

    private static void notify(Context context, String channelId, int notificationId,
                               String title, String body, PendingIntent pendingIntent) {
        createChannels(context);

        // Android 13+ drops notifications silently without this runtime permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_stat_ecomart)
                .setColor(ContextCompat.getColor(context, R.color.brand_primary))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(CHANNEL_ORDERS.equals(channelId)
                        ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private static int notificationId(long orderId) {
        return 10_000 + (int) (orderId & 0x0FFFFFFF);
    }

    private static long parseLong(String value, long fallback) {
        try {
            return value == null ? fallback : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(String value) {
        try {
            return value == null ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
