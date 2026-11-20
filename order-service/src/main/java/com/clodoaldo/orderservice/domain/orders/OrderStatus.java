package com.clodoaldo.orderservice.domain.orders;

/**
 * Enum dos status para order.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public enum OrderStatus {
    PENDING,
    STOCK_RESERVED,
    CONFIRMED,
    CANCELLED
}
