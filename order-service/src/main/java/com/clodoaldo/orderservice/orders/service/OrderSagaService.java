package com.clodoaldo.orderservice.orders.service;

import com.clodoaldo.orderservice.domain.orders.Order;
import com.clodoaldo.orderservice.domain.orders.OrderStatus;
import com.clodoaldo.orderservice.domain.outboxevent.OutboxEvent;
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

@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private static final String ORDER_AGGREGATE = "Order";
    private static final String ORDER_CANCELLED_EVENT = "OrderCancelled";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled";

    private final OrdersRepository ordersRepository;
    private final OutboxEventService outboxEventService;
    private final JsonMapper jsonMapper;

    @Transactional
    public void onStockReserved(UUID orderId) {
        Order order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        order.reserveStock();
    }

    @Transactional
    public void onPaymentRejected(UUID orderId, String reason) {
        Order order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        boolean stockWasReserved = order.getStatus() == OrderStatus.STOCK_RESERVED;

        order.cancel(reason);

        if (stockWasReserved) {
            outboxEventService.save(buildOrderCancelled(order, reason));
        }
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
}
