package com.clodoaldo.orderservice.domain.orders;

import com.clodoaldo.orderservice.domain.orderitems.OrderItem;
import com.clodoaldo.orderservice.domain.orderstatushistory.OrderStatusHistory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

/**
 * Domain que representa a tabela orders.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public static Order create(UUID customerId, String currency, String idempotencyKey) {

        if (isNull(customerId)) {
            throw new IllegalArgumentException("customerId is required");
        }

        Order order = new Order();

        order.id = UUID.randomUUID();
        order.customerId = customerId;
        order.status = OrderStatus.PENDING;
        order.totalAmount = BigDecimal.ZERO;
        order.currency = currency == null ? "BRL" : currency;
        order.idempotencyKey = idempotencyKey;
        order.recordTransition(null, OrderStatus.PENDING, null);

        return order;
    }

    public void addItem(UUID productId, String productName, int quantity, BigDecimal unitPrice) {
        if (status != OrderStatus.PENDING) {

            throw new IllegalStateException("Items can only be changed while the order is PENDING");
        }

        items.add(OrderItem.create(this, productId, productName, quantity, unitPrice));

        recalculateTotal();
    }

    public void reserveStock() {
        requireStatus(OrderStatus.PENDING, "reserve stock");

        recordTransition(OrderStatus.PENDING, OrderStatus.STOCK_RESERVED, null);
    }

    public void confirm() {
        requireStatus(OrderStatus.STOCK_RESERVED, "confirm");

        recordTransition(OrderStatus.STOCK_RESERVED, OrderStatus.CONFIRMED, null);
    }

    public void cancel(String reason) {
        if (status != OrderStatus.PENDING && status != OrderStatus.STOCK_RESERVED) {

            throw new IllegalStateException("Order cannot be cancelled from status " + status);
        }
        this.cancellationReason = reason;

        recordTransition(status, OrderStatus.CANCELLED, reason);
    }

    private void recordTransition(OrderStatus from, OrderStatus to, String reason) {
        this.status = to;
        this.statusHistory.add(OrderStatusHistory.create(this, from, to, reason));
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException("Cannot " + action + " an order in status " + status);
        }
    }

    private void recalculateTotal() {

        this.totalAmount = items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    void onPrePersist() {
        Instant now = Instant.now();
        if (isNull(id)) {

            id = UUID.randomUUID();
        }

        if (isNull(createdAt)) {

            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = Instant.now();
    }
}
