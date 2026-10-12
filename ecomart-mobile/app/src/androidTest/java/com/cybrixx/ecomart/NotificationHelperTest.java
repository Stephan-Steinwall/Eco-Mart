package com.cybrixx.ecomart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.service.notification.StatusBarNotification;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.cybrixx.ecomart.util.NotificationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/** Runs the real notification code on a device/emulator and checks what lands in the shade. */
@RunWith(AndroidJUnit4.class)
public class NotificationHelperTest {

    private Context context;
    private NotificationManager manager;

    @Before
    public void setUp() throws InterruptedException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        manager = context.getSystemService(NotificationManager.class);
        // Android 13+ needs the runtime permission before a notification may be shown.
        InstrumentationRegistry.getInstrumentation().getUiAutomation()
                .grantRuntimePermission(context.getPackageName(), Manifest.permission.POST_NOTIFICATIONS);
        manager.cancelAll();
        // cancelAll() is asynchronous too; don't let a previous test's notification leak into this one.
        for (int i = 0; i < 30 && manager.getActiveNotifications().length > 0; i++) {
            Thread.sleep(100);
        }
    }

    private StatusBarNotification findByChannel(String channelId) {
        for (StatusBarNotification n : manager.getActiveNotifications()) {
            if (channelId.equals(n.getNotification().getChannelId())) return n;
        }
        return null;
    }

    // notify() hands the notification to the system asynchronously, so wait for it to show up.
    private StatusBarNotification awaitByChannel(String channelId) throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            StatusBarNotification found = findByChannel(channelId);
            if (found != null) return found;
            Thread.sleep(100);
        }
        return null;
    }

    @Test
    public void channelsAreCreatedWithTheRightImportance() {
        NotificationHelper.createChannels(context);

        assertNotNull(manager.getNotificationChannel(NotificationHelper.CHANNEL_ORDERS));
        assertNotNull(manager.getNotificationChannel(NotificationHelper.CHANNEL_OFFERS));
        assertEquals(NotificationManager.IMPORTANCE_HIGH,
                manager.getNotificationChannel(NotificationHelper.CHANNEL_ORDERS).getImportance());
    }

    @Test
    public void orderUpdateIsShownOnTheOrderChannel() throws InterruptedException {
        Map<String, String> data = new HashMap<>();
        data.put("orderId", "42");
        data.put("status", "ON_THE_WAY");
        data.put("latitude", "6.9271");
        data.put("longitude", "79.8612");

        NotificationHelper.showOrderUpdate(context, "Your order is on the way", "Order #42 is out for delivery.", data);

        StatusBarNotification posted = awaitByChannel(NotificationHelper.CHANNEL_ORDERS);
        assertNotNull("order notification should be in the shade", posted);
        assertEquals("Your order is on the way", posted.getNotification().extras.getString(Notification.EXTRA_TITLE));
        assertEquals("Order #42 is out for delivery.", posted.getNotification().extras.getString(Notification.EXTRA_TEXT));
        assertNotNull("tapping must lead somewhere", posted.getNotification().contentIntent);
    }

    @Test
    public void newerStatusReplacesTheOlderNotificationForTheSameOrder() throws InterruptedException {
        Map<String, String> data = new HashMap<>();
        data.put("orderId", "42");
        data.put("status", "PREPARING");
        NotificationHelper.showOrderUpdate(context, "Preparing", "Order #42", data);
        data.put("status", "ON_THE_WAY");
        NotificationHelper.showOrderUpdate(context, "On the way", "Order #42", data);

        awaitByChannel(NotificationHelper.CHANNEL_ORDERS);
        Thread.sleep(500); // let both posts settle
        int forThisOrder = 0;
        for (StatusBarNotification n : manager.getActiveNotifications()) {
            if (NotificationHelper.CHANNEL_ORDERS.equals(n.getNotification().getChannelId())) forThisOrder++;
        }
        assertEquals("one notification per order, updated in place", 1, forThisOrder);
        assertEquals("On the way",
                findByChannel(NotificationHelper.CHANNEL_ORDERS).getNotification().extras.getString(Notification.EXTRA_TITLE));
    }

    @Test
    public void offerIsShownOnTheOffersChannel() throws InterruptedException {
        NotificationHelper.showOffer(context, "20% OFF — Weekend Fresh Sale", "Fresh produce, fresh prices.");

        StatusBarNotification posted = awaitByChannel(NotificationHelper.CHANNEL_OFFERS);
        assertNotNull("offer notification should be in the shade", posted);
        assertEquals("20% OFF — Weekend Fresh Sale", posted.getNotification().extras.getString(Notification.EXTRA_TITLE));
        assertNull("an offer must not land on the order channel", findByChannel(NotificationHelper.CHANNEL_ORDERS));
    }
}
