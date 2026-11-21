package com.clodoaldo.orderservice.orderitems.repository;

import com.clodoaldo.orderservice.domain.orderitems.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para tratar {@link OrderItem}.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public interface OrderItemsRepository extends JpaRepository<OrderItem, UUID> {

    Optional<OrderItem> findByOrderId(UUID orderId);

}
