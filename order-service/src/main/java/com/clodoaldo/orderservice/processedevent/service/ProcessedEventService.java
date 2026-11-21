package com.clodoaldo.orderservice.processedevent.service;

import com.clodoaldo.orderservice.processedevent.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public boolean markProcessed(UUID eventId, String handler) {

        return processedEventRepository.insertIfAbsent(eventId, handler) == 1;
    }
}
