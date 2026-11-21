package com.clodoaldo.orderservice.orders.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento consumido quando o pagamento do pedido é aprovado.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public record PaymentApprovedEvent(
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        BigDecimal amount,
        Instant occurredAt
) {
}
