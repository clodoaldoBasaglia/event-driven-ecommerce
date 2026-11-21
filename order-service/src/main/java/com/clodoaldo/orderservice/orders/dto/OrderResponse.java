package com.clodoaldo.orderservice.orders.dto;

import com.clodoaldo.orderservice.domain.orders.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(
            UUID id,
            UUID productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
