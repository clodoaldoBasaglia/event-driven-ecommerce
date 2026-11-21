package com.clodoaldo.orderservice.orders.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentRejectedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt
) {
}
