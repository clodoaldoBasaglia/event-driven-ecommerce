package com.clodoaldo.orderservice.orderstatushistory.repository;

import com.clodoaldo.orderservice.domain.orderstatushistory.OrderStatusHistory;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository somente leitura para tratar {@link OrderStatusHistory}.
 * Escritas devem passar pelo agregado {@code Order}.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public interface OrderStatusHistoryRepository extends Repository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByOccurredAtAsc(UUID orderId);
}
