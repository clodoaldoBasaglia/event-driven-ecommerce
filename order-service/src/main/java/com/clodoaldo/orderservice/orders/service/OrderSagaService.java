package com.clodoaldo.orderservice.orders.service;

import com.clodoaldo.orderservice.domain.orders.Order;
import com.clodoaldo.orderservice.domain.orders.OrderStatus;
import com.clodoaldo.orderservice.domain.outboxevent.OutboxEvent;
import com.clodoaldo.orderservice.orders.event.OrderApprovedEvent;
import com.clodoaldo.orderservice.orders.event.OrderCancelledEvent;
import com.clodoaldo.orderservice.orders.repository.OrdersRepository;
import com.clodoaldo.orderservice.outboxevent.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Orquestra as transições de status do pedido a partir dos eventos da Saga
 * coreografada, incluindo a compensação.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private static final String ORDER_AGGREGATE = "Order";
    private static final String ORDER_CANCELLED_EVENT = "OrderCancelled";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled";
    private static final String ORDER_APPROVED_EVENT = "OrderApproved";
    private static final String ORDER_APPROVED_TOPIC = "order.approved";

    private final OrdersRepository ordersRepository;
    private final OutboxEventService outboxEventService;
    private final JsonMapper jsonMapper;

    @Transactional
    public void onStockReserved(UUID orderId) {
        Order order = findOrder(orderId);

        order.reserveStock();
    }

    @Transactional
    public void onStockRejected(UUID orderId, String reason) {
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        order.cancel(reason);
    }

    @Transactional
    public void onPaymentApproved(UUID orderId) {
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return;
        }

        order.confirm();
        outboxEventService.save(buildOrderApproved(order));
    }

    @Transactional
    public void onPaymentRejected(UUID orderId, String reason) {
        Order order = findOrder(orderId);

        boolean stockWasReserved = order.getStatus() == OrderStatus.STOCK_RESERVED;

        order.cancel(reason);

        if (stockWasReserved) {
            outboxEventService.save(buildOrderCancelled(order, reason));
        }
    }

    private Order findOrder(UUID orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
    }

    private OutboxEvent buildOrderCancelled(Order order, String reason) {
        UUID eventId = UUID.randomUUID();

        List<OrderCancelledEvent.Item> items = order.getItems().stream()
                .map(item -> new OrderCancelledEvent.Item(item.getProductId(), item.getQuantity()))
                .toList();

        OrderCancelledEvent event = new OrderCancelledEvent(
                eventId,
                order.getId(),
                reason,
                items,
                Instant.now());
        String payload = jsonMapper.writeValueAsString(event);

        return OutboxEvent.create(eventId, ORDER_AGGREGATE, order.getId(), ORDER_CANCELLED_EVENT, ORDER_CANCELLED_TOPIC, payload);
    }

    private OutboxEvent buildOrderApproved(Order order) {
        UUID eventId = UUID.randomUUID();

        OrderApprovedEvent event = new OrderApprovedEvent(
                eventId,
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getCurrency(),
                Instant.now());
        String payload = jsonMapper.writeValueAsString(event);

        return OutboxEvent.create(eventId, ORDER_AGGREGATE, order.getId(), ORDER_APPROVED_EVENT, ORDER_APPROVED_TOPIC, payload);
    }
}
