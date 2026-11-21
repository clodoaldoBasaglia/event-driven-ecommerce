package com.clodoaldo.orderservice.domain.outboxevent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;

/**
 * Domain que representa a tabela outbox_event.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public static OutboxEvent create(UUID id, String aggregateType, UUID aggregateId, String eventType, String topic, String payload) {

        if (isNull(id)) {
            throw new IllegalArgumentException("id is required");
        }

        if (isNull(aggregateType) || aggregateType.isBlank()) {
            throw new IllegalArgumentException("aggregateType is required");
        }

        if (isNull(aggregateId)) {
            throw new IllegalArgumentException("aggregateId is required");
        }

        if (isNull(eventType)|| eventType.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }

        if (isNull(topic) || topic.isBlank()) {
            throw new IllegalArgumentException("topic is required");
        }

        if (isNull(payload) || payload.isBlank()) {
            throw new IllegalArgumentException("payload is required");
        }

        OutboxEvent event = new OutboxEvent();

        event.id = id;
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = eventType;
        event.topic = topic;
        event.payload = payload;
        event.attempts = 0;

        return event;
    }

    public static OutboxEvent create(String aggregateType, UUID aggregateId, String eventType, String topic, String payload) {
        return create(UUID.randomUUID(), aggregateType, aggregateId, eventType, topic, payload);
    }

    public void markPublished(Instant publishedAt) {

        this.publishedAt = publishedAt;
    }

    public void recordFailure(String error) {

        this.attempts = (attempts == null ? 0 : attempts) + 1;

        this.lastError = truncate(error, 500);
    }

    @PrePersist
    void onPrePersist() {
        if (id == null) {

            id = UUID.randomUUID();
        }

        if (attempts == null) {

            attempts = 0;
        }

        if (createdAt == null) {

            createdAt = Instant.now();
        }
    }

    private static String truncate(String value, int maxLength) {

        if (value == null || value.length() <= maxLength) {

            return value;
        }

        return value.substring(0, maxLength);
    }
}
