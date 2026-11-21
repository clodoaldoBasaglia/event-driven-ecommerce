package com.clodoaldo.orderservice.orders.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Evento publicado na criação do pedido (Outbox), consumido pelo inventory-service.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        String currency,
        List<Item> items,
        Instant occurredAt
) {
    public record Item(UUID productId, int quantity) {
    }
}
