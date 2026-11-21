package com.clodoaldo.orderservice.orders.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento consumido quando a reserva de estoque do pedido é recusada.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public record StockRejectedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt
) {
}
