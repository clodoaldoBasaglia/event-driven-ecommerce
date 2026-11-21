package com.clodoaldo.orderservice.orders.consumer;

import com.clodoaldo.orderservice.orders.event.PaymentApprovedEvent;
import com.clodoaldo.orderservice.orders.service.OrderSagaService;
import com.clodoaldo.orderservice.processedevent.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consumer Kafka do evento {@code payment.approved}; confirma o pedido via Saga.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentApprovedConsumer {

    private static final String HANDLER = "PaymentApprovedHandler";

    private final JsonMapper jsonMapper;
    private final ProcessedEventService processedEventService;
    private final OrderSagaService orderSagaService;

    @KafkaListener(topics = "payment.approved", groupId = "order-service")
    @Transactional
    public void onMessage(String payload) {
        PaymentApprovedEvent event = jsonMapper.readValue(payload, PaymentApprovedEvent.class);

        if (!processedEventService.markProcessed(event.eventId(), HANDLER)) {
            log.info("Duplicate PaymentApproved event {} ignored", event.eventId());
            return;
        }

        orderSagaService.onPaymentApproved(event.orderId());
        log.info("Order {} confirmed after payment approval", event.orderId());
    }
}
