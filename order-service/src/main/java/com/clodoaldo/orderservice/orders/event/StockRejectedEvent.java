package com.clodoaldo.orderservice.orders.event;

import java.time.Instant;
import java.util.UUID;

public record StockRejectedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt
) {
}
