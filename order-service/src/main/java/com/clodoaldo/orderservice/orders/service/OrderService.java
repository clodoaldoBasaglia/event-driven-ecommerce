package com.clodoaldo.orderservice.orders.service;

import com.clodoaldo.orderservice.domain.orderitems.OrderItem;
import com.clodoaldo.orderservice.domain.orders.Order;
import com.clodoaldo.orderservice.domain.outboxevent.OutboxEvent;
import com.clodoaldo.orderservice.orders.dto.CreateOrderRequest;
import com.clodoaldo.orderservice.orders.dto.OrderResponse;
import com.clodoaldo.orderservice.orders.event.OrderCreatedEvent;
import com.clodoaldo.orderservice.orders.repository.OrdersRepository;
import com.clodoaldo.orderservice.outboxevent.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

/**
 * Serviço de aplicação do agregado {@code Order}: criação idempotente com
 * gravação no Outbox na mesma transação.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_AGGREGATE = "Order";
    private static final String ORDER_CREATED_EVENT = "OrderCreated";
    private static final String ORDER_EVENTS_TOPIC = "order.events";

    private final OrdersRepository ordersRepository;
    private final OutboxEventService outboxEventService;
    private final JsonMapper jsonMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest createOrderRequest, String idempotencyKey) {

        if (!isNull(idempotencyKey) && !idempotencyKey.isBlank()) {

            return ordersRepository
                    .findByCustomerIdAndIdempotencyKey(createOrderRequest.customerId(), idempotencyKey)
                    .map(this::toResponse)
                    .orElseGet(() -> createNewOrder(createOrderRequest, idempotencyKey));
        }

        return createNewOrder(createOrderRequest, idempotencyKey);
    }

    private OrderResponse createNewOrder(CreateOrderRequest createOrderRequest, String idempotencyKey) {
        Order order = Order.create(createOrderRequest.customerId(), createOrderRequest.currency(), idempotencyKey);

        createOrderRequest.items()
                .forEach(item -> order.addItem(item.productId(), item.productName(), item.quantity(), item.unitPrice()));

        ordersRepository.save(order);
        outboxEventService.save(buildOrderCreated(order));

        return toResponse(order);
    }

    private OutboxEvent buildOrderCreated(Order order) {
        UUID eventId = UUID.randomUUID();

        List<OrderCreatedEvent.Item> items = order.getItems().stream()
                .map(item -> new OrderCreatedEvent.Item(item.getProductId(), item.getQuantity()))
                .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getCurrency(),
                items,
                Instant.now());
        String payload = jsonMapper.writeValueAsString(event);

        return OutboxEvent.create(eventId, ORDER_AGGREGATE, order.getId(), ORDER_CREATED_EVENT, ORDER_EVENTS_TOPIC, payload);
    }

    private OrderResponse toResponse(Order order) {

        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCancellationReason(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items);
    }

    private OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {

        return new OrderResponse.OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.subtotal());
    }
}
