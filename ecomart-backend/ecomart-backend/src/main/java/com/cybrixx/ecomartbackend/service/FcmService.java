package com.cybrixx.ecomartbackend.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Thin wrapper around Firebase Cloud Messaging.
 *
 * Push is optional: if no service-account key is configured the backend still starts and every
 * send returns {@link PushResult#DISABLED}, so the rest of the system never depends on Firebase.
 */
@Slf4j
@Service
public class FcmService {

    public enum PushResult { SENT, DISABLED, INVALID_TOKEN, FAILED }

    /** Topic every customer device subscribes to for broadcast offers. */
    public static final String OFFERS_TOPIC = "offers";

    private final String credentialsPath;
    private boolean enabled = false;

    public FcmService(@Value("${ecomart.fcm.credentials-path:}") String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    @PostConstruct
    void init() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("FCM disabled: 'ecomart.fcm.credentials-path' is not set. "
                    + "Push notifications will be skipped until a Firebase service-account key is configured.");
            return;
        }
        Path path = Path.of(credentialsPath);
        if (!Files.isReadable(path)) {
            log.warn("FCM disabled: cannot read service-account key at '{}'.", path.toAbsolutePath());
            return;
        }
        try (InputStream in = new FileInputStream(path.toFile())) {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .build());
            }
            enabled = true;
            log.info("FCM enabled using service-account key '{}'.", path.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            log.error("FCM disabled: failed to initialise Firebase from '{}': {}", path.toAbsolutePath(), e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Push to a single device (e.g. an order-status update for the customer who placed it). */
    public PushResult sendToToken(String token, Map<String, String> data, Duration timeToLive) {
        if (token == null || token.isBlank()) {
            return PushResult.INVALID_TOKEN;
        }
        return send(Message.builder().setToken(token), data, timeToLive);
    }

    /** Push to every device subscribed to a topic (e.g. a new offer for all customers). */
    public PushResult sendToTopic(String topic, Map<String, String> data, Duration timeToLive) {
        return send(Message.builder().setTopic(topic), data, timeToLive);
    }

    private PushResult send(Message.Builder builder, Map<String, String> data, Duration timeToLive) {
        if (!enabled) {
            return PushResult.DISABLED;
        }
        // Data-only (no "notification" block): the app's messaging service then runs in the
        // foreground AND background, so it controls the channel, icon and tap destination.
        Message message = builder
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setTtl(Math.max(timeToLive.toMillis(), 0))
                        .build())
                .build();
        try {
            String id = FirebaseMessaging.getInstance().send(message);
            log.info("FCM message sent: {}", id);
            return PushResult.SENT;
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            // Only UNREGISTERED means "this device is gone" (app reinstalled / data cleared). INVALID_ARGUMENT
            // can also mean a bad payload, so it must not make us forget a perfectly good token.
            if (code == MessagingErrorCode.UNREGISTERED) {
                log.warn("FCM says the device token is no longer registered ({}): {}", code, e.getMessage());
                return PushResult.INVALID_TOKEN;
            }
            log.error("FCM send failed ({}): {}", code, e.getMessage());
            return PushResult.FAILED;
        }
    }
}
