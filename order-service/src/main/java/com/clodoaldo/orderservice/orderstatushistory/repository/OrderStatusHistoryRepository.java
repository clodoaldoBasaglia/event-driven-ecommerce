package com.clodoaldo.orderservice.orderstatushistory.repository;

import com.clodoaldo.orderservice.domain.orderstatushistory.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository para tratar {@link OrderStatusHistory}.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {


    List<OrderStatusHistory> findByOrderIdOrderByOccurredAtAsc(UUID orderId);
}
