package com.clodoaldo.orderservice.orders.consumer;

import com.clodoaldo.orderservice.orders.event.StockRejectedEvent;
import com.clodoaldo.orderservice.orders.service.OrderSagaService;
import com.clodoaldo.orderservice.processedevent.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consumer Kafka do evento {@code stock.rejected}; cancela o pedido sem compensação.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockRejectedConsumer {

    private static final String HANDLER = "StockRejectedHandler";

    private final JsonMapper jsonMapper;
    private final ProcessedEventService processedEventService;
    private final OrderSagaService orderSagaService;

    @KafkaListener(topics = "stock.rejected", groupId = "order-service")
    @Transactional
    public void onMessage(String payload) {
        StockRejectedEvent event = jsonMapper.readValue(payload, StockRejectedEvent.class);

        if (!processedEventService.markProcessed(event.eventId(), HANDLER)) {
            log.info("Duplicate StockRejected event {} ignored", event.eventId());
            return;
        }

        orderSagaService.onStockRejected(event.orderId(), event.reason());
        log.info("Order {} cancelled because stock was rejected", event.orderId());
    }
}
