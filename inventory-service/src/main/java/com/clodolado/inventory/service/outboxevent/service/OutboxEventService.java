package com.clodolado.inventory.service.outboxevent.service;

import com.clodolado.inventory.service.domain.outboxevent.OutboxEvent;
import com.clodolado.inventory.service.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste eventos no Outbox (padrão Outbox) para publicação posterior.
 *
 * @author clodoaldo
 * @since 0.0.1 (21/11/2026)
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
