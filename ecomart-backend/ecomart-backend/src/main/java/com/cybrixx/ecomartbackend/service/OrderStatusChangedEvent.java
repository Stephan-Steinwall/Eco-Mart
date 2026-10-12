package com.cybrixx.ecomartbackend.service;

import com.cybrixx.ecomartbackend.entity.enums.OrderStatus;

/**
 * Published when an admin moves an order to a new status. Carries plain values (not entities) so the
 * listener can run after the transaction has closed without touching lazy-loaded relations.
 */
public record OrderStatusChangedEvent(
        Long orderId,
        Long userId,
        String fcmToken,
        OrderStatus status,
        Double latitude,
        Double longitude) {
}
