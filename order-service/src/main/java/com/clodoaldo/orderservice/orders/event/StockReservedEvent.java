package com.clodoaldo.orderservice.orders.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento consumido quando o estoque do pedido é reservado.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public record StockReservedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) {
}
