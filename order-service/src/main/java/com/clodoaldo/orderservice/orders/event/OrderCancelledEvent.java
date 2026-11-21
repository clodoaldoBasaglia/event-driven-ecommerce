package com.clodoaldo.orderservice.orders.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        List<Item> items,
        Instant occurredAt
) {
    public record Item(UUID productId, int quantity) {
    }
}
