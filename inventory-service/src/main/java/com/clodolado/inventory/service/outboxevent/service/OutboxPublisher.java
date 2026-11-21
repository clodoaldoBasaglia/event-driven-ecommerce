package com.clodolado.inventory.service.outboxevent.service;

import com.clodolado.inventory.service.domain.outboxevent.OutboxEvent;
import com.clodolado.inventory.service.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publica no Kafka os eventos pendentes do Outbox, marcando sucesso ou falha.
 *
 * <p>O poller lê as linhas com {@code published_at IS NULL} usando
 * {@code FOR UPDATE SKIP LOCKED}, o que permite múltiplas instâncias sem
 * publicar o mesmo evento em paralelo. A entrega é {@code at-least-once}:
 * os consumidores são idempotentes via {@code processed_event}.</p>
 *
 * @author clodoaldo
 * @since 0.0.1 (21/11/2026)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "outbox.publisher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${outbox.publisher.batch-size:50}")
    private int batchSize;

    @Value("${outbox.publisher.send-timeout-seconds:5}")
    private long sendTimeoutSeconds;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:1000}")
    @Transactional
    public void publishPending() {

        List<OutboxEvent> pending = outboxEventRepository.findPendingForUpdate(batchSize);

        if (pending.isEmpty()) {

            return;
        }

        pending.forEach(this::publish);

        log.debug("Outbox: {} evento(s) processado(s)", pending.size());
    }

    private void publish(OutboxEvent event) {
        try {

            kafkaTemplate
                    .send(event.getTopic(), event.getAggregateId().toString(), event.getPayload())
                    .get(sendTimeoutSeconds, TimeUnit.SECONDS);

            event.markPublished(Instant.now());

            log.info("Outbox event {} ({}) publicado em {}", event.getId(), event.getEventType(), event.getTopic());
        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            event.recordFailure("Interrupted while publishing: " + e.getMessage());

            log.warn("Publicação interrompida para o evento {}", event.getId());
        } catch (ExecutionException | TimeoutException e) {

            event.recordFailure(e.getMessage());

            log.error("Falha ao publicar evento {} em {}", event.getId(), event.getTopic(), e);
        }
    }
}
