package com.clodoaldo.orderservice.domain.processedevent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@Table(name = "processed_event")
@IdClass(ProcessedEventId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Id
    @Column(name = "handler", nullable = false, length = 100)
    private String handler;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public static ProcessedEvent create(UUID eventId, String handler) {
        if (isNull(eventId)) {

            throw new IllegalArgumentException("eventId is required");
        }
        if (isNull(handler) || handler.isBlank()) {

            throw new IllegalArgumentException("handler is required");
        }

        ProcessedEvent processedEvent = new ProcessedEvent();

        processedEvent.eventId = eventId;
        processedEvent.handler = handler;
        processedEvent.processedAt = Instant.now();

        return processedEvent;
    }

    @PrePersist
    void onPrePersist() {

        if (isNull(processedAt)) {

            processedAt = Instant.now();
        }
    }
}
