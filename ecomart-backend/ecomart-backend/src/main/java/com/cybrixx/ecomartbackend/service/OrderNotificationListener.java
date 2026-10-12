package com.cybrixx.ecomartbackend.service;

import com.cybrixx.ecomartbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Turns an order-status change into a push notification for the customer who placed the order. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    // AFTER_COMMIT: never tell a customer about a status that ended up rolled back.
    // @Async: a slow Firebase call must not hold up the admin's request.
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.fcmToken() == null) {
            log.info("Order #{} is now {} but the customer has no registered device; skipping push.",
                    event.orderId(), event.status());
            return;
        }

        String title;
        String body;
        switch (event.status()) {
            case PREPARING -> {
                title = "We're preparing your order 🥕";
                body = "Order #" + event.orderId() + " is being packed with fresh produce.";
            }
            case ON_THE_WAY -> {
                title = "Your order is on the way 🚚";
                body = "Order #" + event.orderId() + " is out for delivery. Tap to follow it.";
            }
            case DELIVERED -> {
                title = "Order delivered ✅";
                body = "Order #" + event.orderId() + " has arrived. Enjoy your fresh groceries!";
            }
            case CANCELLED -> {
                title = "Order cancelled";
                body = "Order #" + event.orderId() + " was cancelled. Contact support if this is unexpected.";
            }
            default -> {
                return; // PENDING is the initial state; nothing to announce
            }
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "ORDER_STATUS");
        data.put("title", title);
        data.put("body", body);
        data.put("orderId", String.valueOf(event.orderId()));
        data.put("status", event.status().name());
        if (event.latitude() != null) data.put("latitude", String.valueOf(event.latitude()));
        if (event.longitude() != null) data.put("longitude", String.valueOf(event.longitude()));

        FcmService.PushResult result = fcmService.sendToToken(event.fcmToken(), data, Duration.ofDays(1));
        log.info("Order #{} -> {}: push result {}", event.orderId(), event.status(), result);

        // FCM says the token is dead (app uninstalled / data cleared): forget it.
        if (result == FcmService.PushResult.INVALID_TOKEN) {
            userRepository.clearToken(event.userId());
        }
    }
}
