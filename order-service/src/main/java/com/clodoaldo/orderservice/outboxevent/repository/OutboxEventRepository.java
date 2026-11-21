package com.clodoaldo.orderservice.outboxevent.repository;

import com.clodoaldo.orderservice.domain.outboxevent.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository para tratar {@link OutboxEvent}.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT * FROM outbox_event WHERE published_at IS NULL
        ORDER BY created_at LIMIT :limit FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> findPendingForUpdate(@Param("limit") int limit);
}
