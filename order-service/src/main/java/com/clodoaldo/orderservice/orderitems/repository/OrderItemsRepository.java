package com.clodoaldo.orderservice.orderitems.repository;

import com.clodoaldo.orderservice.domain.orderitems.OrderItem;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository somente leitura para tratar {@link OrderItem}.
 * Escritas devem passar pelo agregado {@code Order}.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
public interface OrderItemsRepository extends Repository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);
}
