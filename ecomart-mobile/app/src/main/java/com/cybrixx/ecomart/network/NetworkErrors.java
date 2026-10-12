package com.cybrixx.ecomart.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import java.io.InterruptedIOException;

/**
 * Works out *why* a request failed so screens can show a helpful message instead of a raw
 * exception: no connection at all, server unreachable, timeout, expired login, or a server fault.
 */
public final class NetworkErrors {

    public enum Kind { OFFLINE, SERVER_UNREACHABLE, TIMEOUT, SESSION_EXPIRED, SERVER_ERROR, UNKNOWN }

    private NetworkErrors() {}

    /** True when the device has a network that claims internet access (Wi-Fi or mobile data). */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /** Classify a failed call (Retrofit's onFailure: the request never produced an HTTP response). */
    public static Kind classify(Context context, Throwable t) {
        if (!isOnline(context)) return Kind.OFFLINE;
        if (t instanceof InterruptedIOException) return Kind.TIMEOUT; // includes SocketTimeoutException
        if (t instanceof java.io.IOException) return Kind.SERVER_UNREACHABLE;
        return Kind.UNKNOWN;
    }

    /** Classify an HTTP error response (the server answered, but not with success). */
    public static Kind classify(int httpCode) {
        // Spring Security answers 403 (not 401) for a missing/expired token.
        if (httpCode == 401 || httpCode == 403) return Kind.SESSION_EXPIRED;
        if (httpCode >= 500) return Kind.SERVER_ERROR;
        return Kind.UNKNOWN;
    }

    public static String title(Kind kind) {
        switch (kind) {
            case OFFLINE: return "You're offline";
            case SERVER_UNREACHABLE: return "Can't reach EcoMart";
            case TIMEOUT: return "Connection timed out";
            case SESSION_EXPIRED: return "Session expired";
            default: return "Something went wrong";
        }
    }

    public static String message(Kind kind) {
        switch (kind) {
            case OFFLINE: return "Check your Wi-Fi or mobile data, then try again.";
            case SERVER_UNREACHABLE: return "We couldn't connect to our servers. Please try again in a moment.";
            case TIMEOUT: return "The server is taking too long to respond. Please try again.";
            case SESSION_EXPIRED: return "Please log in again to continue.";
            case SERVER_ERROR: return "Our servers ran into a problem. Please try again shortly.";
            default: return "We couldn't load this right now. Please try again.";
        }
    }

    /** One-line version for toasts and snackbars. */
    public static String shortMessage(Kind kind) {
        switch (kind) {
            case OFFLINE: return "No internet connection";
            case SERVER_UNREACHABLE: return "Can't reach the server";
            case TIMEOUT: return "The connection timed out";
            case SESSION_EXPIRED: return "Your session expired, please log in again";
            default: return "Something went wrong, please try again";
        }
    }

    public static String shortMessage(Context context, Throwable t) {
        return shortMessage(classify(context, t));
    }
}
