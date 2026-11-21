package com.clodoaldo.orderservice.outboxevent.service;

import com.clodoaldo.orderservice.domain.outboxevent.OutboxEvent;
import com.clodoaldo.orderservice.outboxevent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public OutboxEvent save(OutboxEvent event) {
        return outboxEventRepository.save(event);
    }
}
