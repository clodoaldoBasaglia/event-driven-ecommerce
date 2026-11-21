package com.clodoaldo.orderservice.orders.consumer;

import com.clodoaldo.orderservice.orders.event.StockReservedEvent;
import com.clodoaldo.orderservice.orders.service.OrderSagaService;
import com.clodoaldo.orderservice.processedevent.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consumer Kafka do evento {@code stock.reserved}; move o pedido para
 * {@code STOCK_RESERVED} via Saga.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockReservedConsumer {

    private static final String HANDLER = "StockReservedHandler";

    private final JsonMapper jsonMapper;
    private final ProcessedEventService processedEventService;
    private final OrderSagaService orderSagaService;

    @KafkaListener(topics = "stock.reserved", groupId = "order-service")
    @Transactional
    public void onMessage(String payload) {
        StockReservedEvent event = jsonMapper.readValue(payload, StockReservedEvent.class);

        if (!processedEventService.markProcessed(event.eventId(), HANDLER)) {
            log.info("Duplicate StockReserved event {} ignored", event.eventId());
            return;
        }

        orderSagaService.onStockReserved(event.orderId());
        log.info("Order {} moved to STOCK_RESERVED", event.orderId());
    }
}
