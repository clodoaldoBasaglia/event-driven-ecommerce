package com.clodoaldo.orderservice.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.clodoaldo.orderservice.domain.orders.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para tratar {@link Order}.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public interface OrdersRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByCustomerIdAndIdempotencyKey(UUID customerID, String idempotencyKey);
}
