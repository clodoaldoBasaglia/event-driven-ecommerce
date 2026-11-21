package com.clodoaldo.orderservice.orders.controller;

import com.clodoaldo.orderservice.orders.dto.CreateOrderRequest;
import com.clodoaldo.orderservice.orders.dto.OrderResponse;
import com.clodoaldo.orderservice.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * API REST para criação e consulta de pedidos.
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse post(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest createOrderRequest) {

        return orderService.create(createOrderRequest, idempotencyKey);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {

        return orderService.findOneById(id);
    }
}
