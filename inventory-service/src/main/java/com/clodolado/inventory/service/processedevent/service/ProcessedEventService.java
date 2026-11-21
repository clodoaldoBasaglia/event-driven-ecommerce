package com.clodolado.inventory.service.processedevent.service;

import com.clodolado.inventory.service.processedevent.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Controle de idempotência dos consumidores via {@code processed_event}.
 *
 * @author clodoaldo
 * @since 0.0.1 (21/11/2026)
 */
@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public boolean markProcessed(UUID eventId, String handler) {

        return processedEventRepository.insertIfAbsent(eventId, handler) == 1;
    }
}
