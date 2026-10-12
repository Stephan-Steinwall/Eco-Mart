package com.cybrixx.ecomart;

import android.app.Application;

import com.cybrixx.ecomart.util.NotificationHelper;

public class EcoMartApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Create the channels up front so they appear in system settings before the first push.
        NotificationHelper.createChannels(this);
    }
}
