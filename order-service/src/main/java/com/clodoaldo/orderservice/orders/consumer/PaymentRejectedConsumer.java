package com.clodoaldo.orderservice.orders.consumer;

import com.clodoaldo.orderservice.orders.event.PaymentRejectedEvent;
import com.clodoaldo.orderservice.orders.service.OrderSagaService;
import com.clodoaldo.orderservice.processedevent.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRejectedConsumer {

    private static final String HANDLER = "PaymentRejectedHandler";

    private final JsonMapper jsonMapper;
    private final ProcessedEventService processedEventService;
    private final OrderSagaService orderSagaService;

    @KafkaListener(topics = "payment.rejected", groupId = "order-service")
    @Transactional
    public void onMessage(String payload) {
        PaymentRejectedEvent event = jsonMapper.readValue(payload, PaymentRejectedEvent.class);

        if (!processedEventService.markProcessed(event.eventId(), HANDLER)) {
            log.info("Duplicate PaymentRejected event {} ignored", event.eventId());
            return;
        }

        orderSagaService.onPaymentRejected(event.orderId(), event.reason());
        log.info("Order {} cancelled because payment was rejected", event.orderId());
    }
}
