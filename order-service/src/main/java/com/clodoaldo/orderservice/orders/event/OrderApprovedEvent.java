package com.clodoaldo.orderservice.orders.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado quando o pagamento é aprovado e o pedido é confirmado (Outbox).
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public record OrderApprovedEvent(
        UUID eventId,
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        String currency,
        Instant occurredAt
) {
}
