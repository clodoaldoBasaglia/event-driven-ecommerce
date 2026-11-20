package com.clodoaldo.orderservice.domain.processedevent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Domain que representa .
 *
 * @author clodoaldo
 * @since 0.0.1 (20/11/2026)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProcessedEventId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID eventId;

    private String handler;
}
