package com.clodoaldo.orderservice.processedevent.repository;

import com.clodoaldo.orderservice.domain.processedevent.ProcessedEvent;
import com.clodoaldo.orderservice.domain.processedevent.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository para tratar {@link ProcessedEvent}.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {

    @Modifying
    @Query(value = """
        INSERT INTO processed_event(event_id, handler)
        VALUES(:eventId, :handler) ON CONFLICT DO NOTHING
    """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") UUID eventId, @Param("handler") String handler);
}
