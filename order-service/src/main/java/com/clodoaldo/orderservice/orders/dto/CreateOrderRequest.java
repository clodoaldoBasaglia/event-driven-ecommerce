package com.clodoaldo.orderservice.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * DTO de entrada para criação de um pedido ({@code POST /orders}).
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public record CreateOrderRequest(
        @NotNull UUID customerId,
        String currency,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
}
