package com.clodoaldo.orderservice.outboxevent.service;

import com.clodoaldo.orderservice.domain.outboxevent.OutboxEvent;
import com.clodoaldo.orderservice.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste eventos no Outbox (padrão Outbox) para publicação posterior.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public OutboxEvent save(OutboxEvent event) {
        return outboxEventRepository.save(event);
    }
}
