package com.clodoaldo.orderservice.orders.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evento de compensação publicado quando um pedido é cancelado, para devolver o estoque.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
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
