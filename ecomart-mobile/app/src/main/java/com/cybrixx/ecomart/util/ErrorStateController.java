package com.cybrixx.ecomart.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.cybrixx.ecomart.R;
import com.cybrixx.ecomart.activity.LoginActivity;
import com.cybrixx.ecomart.network.NetworkErrors;
import com.cybrixx.ecomart.network.NetworkErrors.Kind;
import com.google.android.material.button.MaterialButton;

/**
 * Drives the "connection lost / try again" page (layout/view_error_state.xml) that screens overlay
 * on their content when a request fails.
 *
 * - Shows a message matching the failure (offline, server down, timeout, expired session...).
 * - "Try again" re-runs the screen's own load function.
 * - While offline it also watches for the connection coming back and retries automatically.
 * - Call {@link #release()} from onDestroy.
 */
public class ErrorStateController {

    private final Context context;
    private final View root;
    private final ImageView icon;
    private final TextView title;
    private final TextView message;
    private final MaterialButton action;
    private final Handler main = new Handler(Looper.getMainLooper());

    private ConnectivityManager.NetworkCallback networkCallback;

    public ErrorStateController(@NonNull View root) {
        this.context = root.getContext();
        this.root = root;
        this.icon = root.findViewById(R.id.ivErrorIcon);
        this.title = root.findViewById(R.id.tvErrorTitle);
        this.message = root.findViewById(R.id.tvErrorMessage);
        this.action = root.findViewById(R.id.btnErrorAction);
    }

    /** A request failed before getting any HTTP response (no network, timeout, server down...). */
    public void showFailure(Throwable t, Runnable onRetry) {
        show(NetworkErrors.classify(context, t), onRetry);
    }

    /** The server answered with an error status. */
    public void showHttpError(int httpCode, Runnable onRetry) {
        show(NetworkErrors.classify(httpCode), onRetry);
    }

    public boolean isShowing() {
        return root.getVisibility() == View.VISIBLE;
    }

    public void hide() {
        stopWatchingNetwork();
        root.setVisibility(View.GONE);
    }

    public void release() {
        stopWatchingNetwork();
        main.removeCallbacksAndMessages(null);
    }

    private void show(Kind kind, Runnable onRetry) {
        boolean connectionProblem = kind == Kind.OFFLINE || kind == Kind.SERVER_UNREACHABLE || kind == Kind.TIMEOUT;
        icon.setImageResource(connectionProblem ? R.drawable.ic_cloud_off : R.drawable.ic_alert);
        title.setText(NetworkErrors.title(kind));
        message.setText(NetworkErrors.message(kind));

        if (kind == Kind.SESSION_EXPIRED) {
            action.setText("Log in");
            action.setOnClickListener(v -> logOut());
        } else {
            action.setText("Try again");
            action.setOnClickListener(v -> {
                hide();
                onRetry.run();
            });
        }

        root.setVisibility(View.VISIBLE);
        stopWatchingNetwork();
        if (kind == Kind.OFFLINE) {
            retryWhenBackOnline(onRetry);
        }
    }

    private void logOut() {
        new SharedPrefsManager(context).clearSession();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // Only used for OFFLINE: there is no default network yet, so onAvailable fires when one appears.
    private void retryWhenBackOnline(Runnable onRetry) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // Give the connection a moment to settle before hitting the server.
                main.postDelayed(() -> {
                    if (isShowing()) {
                        hide();
                        onRetry.run();
                    }
                }, 800);
            }
        };
        try {
            cm.registerDefaultNetworkCallback(networkCallback);
        } catch (RuntimeException e) {
            networkCallback = null; // auto-retry is a convenience; the button still works
        }
    }

    private void stopWatchingNetwork() {
        if (networkCallback == null) return;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (cm != null) cm.unregisterNetworkCallback(networkCallback);
        } catch (RuntimeException ignored) {
            // already unregistered
        }
        networkCallback = null;
    }
}
