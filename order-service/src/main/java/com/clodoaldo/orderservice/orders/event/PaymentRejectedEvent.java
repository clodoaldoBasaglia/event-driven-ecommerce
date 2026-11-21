package com.clodoaldo.orderservice.orders.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento consumido quando o pagamento do pedido é recusado.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public record PaymentRejectedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt
) {
}
