-- =============================================================================
-- inventory-service | V2: schema inicial
-- =============================================================================

-- -----------------------------------------------------------------------------
-- product_stock: saldo de estoque por produto
--   available_quantity : livre para novas reservas
--   reserved_quantity  : reservado para pedidos ainda não confirmados
-- Reservar  = available - n, reserved + n
-- Liberar   = available + n, reserved - n   (compensação da Saga)
-- Consumir  = reserved - n                  (pedido confirmado/pago)
-- Os CHECKs impedem saldo negativo (proteção final contra overselling).
-- -----------------------------------------------------------------------------
CREATE TABLE product_stock (
                               product_id         UUID           PRIMARY KEY,
                               sku                VARCHAR(50)    NOT NULL,
                               name               VARCHAR(200)   NOT NULL,
                               available_quantity INTEGER        NOT NULL DEFAULT 0,
                               reserved_quantity  INTEGER        NOT NULL DEFAULT 0,
                               version            BIGINT         NOT NULL DEFAULT 0,  -- lock otimista (@Version)
                               created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
                               updated_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),

                               CONSTRAINT uq_product_stock_sku UNIQUE (sku),
                               CONSTRAINT ck_product_stock_available CHECK (available_quantity >= 0),
                               CONSTRAINT ck_product_stock_reserved  CHECK (reserved_quantity  >= 0)
);

-- -----------------------------------------------------------------------------
-- stock_reservation: uma reserva por pedido
--   RESERVED  -> CONSUMED   (pagamento aprovado, pedido confirmado)
--   RESERVED  -> RELEASED   (pagamento recusado ou pedido cancelado)
-- UNIQUE(order_id) torna a reserva idempotente: reprocessar OrderCreated
-- não reserva o estoque duas vezes.
-- order_id não tem FK: o pedido pertence ao order-service.
-- -----------------------------------------------------------------------------
CREATE TABLE stock_reservation (
                                   id         UUID          PRIMARY KEY,
                                   order_id   UUID          NOT NULL,
                                   status     VARCHAR(20)   NOT NULL,
                                   reason     VARCHAR(255),                          -- motivo da liberação/rejeição
                                   created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
                                   updated_at TIMESTAMPTZ   NOT NULL DEFAULT now(),

                                   CONSTRAINT uq_stock_reservation_order UNIQUE (order_id),
                                   CONSTRAINT ck_stock_reservation_status
                                       CHECK (status IN ('RESERVED', 'CONSUMED', 'RELEASED'))
);

CREATE INDEX idx_stock_reservation_status ON stock_reservation (status);

-- -----------------------------------------------------------------------------
-- stock_reservation_item: produtos e quantidades de cada reserva
-- -----------------------------------------------------------------------------
CREATE TABLE stock_reservation_item (
                                        id             UUID     PRIMARY KEY,
                                        reservation_id UUID     NOT NULL,
                                        product_id     UUID     NOT NULL,
                                        quantity       INTEGER  NOT NULL,

                                        CONSTRAINT fk_reservation_item_reservation
                                            FOREIGN KEY (reservation_id) REFERENCES stock_reservation (id) ON DELETE CASCADE,
                                        CONSTRAINT fk_reservation_item_product
                                            FOREIGN KEY (product_id) REFERENCES product_stock (product_id),
                                        CONSTRAINT uq_reservation_item_product UNIQUE (reservation_id, product_id),
                                        CONSTRAINT ck_reservation_item_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_reservation_item_product ON stock_reservation_item (product_id);

-- -----------------------------------------------------------------------------
-- stock_movement: livro-razão (ledger) imutável das movimentações
-- Só recebe INSERT; permite auditar e reconstruir o saldo de qualquer produto.
-- -----------------------------------------------------------------------------
CREATE TABLE stock_movement (
                                id             BIGSERIAL    PRIMARY KEY,
                                product_id     UUID         NOT NULL,
                                reservation_id UUID,                              -- NULL em entrada/ajuste manual
                                type           VARCHAR(20)  NOT NULL,
                                quantity       INTEGER      NOT NULL,
                                occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

                                CONSTRAINT fk_stock_movement_product
                                    FOREIGN KEY (product_id) REFERENCES product_stock (product_id),
                                CONSTRAINT fk_stock_movement_reservation
                                    FOREIGN KEY (reservation_id) REFERENCES stock_reservation (id),
                                CONSTRAINT ck_stock_movement_type
                                    CHECK (type IN ('INBOUND', 'RESERVE', 'RELEASE', 'CONSUME', 'ADJUSTMENT')),
                                CONSTRAINT ck_stock_movement_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_stock_movement_product ON stock_movement (product_id, occurred_at DESC);

-- -----------------------------------------------------------------------------
-- outbox_event: Outbox Pattern (mesma estrutura do order-service)
-- Publica StockReserved, StockRejected e StockReleased.
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
                              id             UUID         PRIMARY KEY,
                              aggregate_type VARCHAR(50)  NOT NULL,
                              aggregate_id   UUID         NOT NULL,
                              event_type     VARCHAR(100) NOT NULL,
                              topic          VARCHAR(100) NOT NULL,
                              payload        JSONB        NOT NULL,
                              attempts       INTEGER      NOT NULL DEFAULT 0,
                              last_error     VARCHAR(500),
                              created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                              published_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_event_pending
    ON outbox_event (created_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_event_aggregate ON outbox_event (aggregate_type, aggregate_id);

-- -----------------------------------------------------------------------------
-- processed_event: consumidor idempotente (mesma estrutura do order-service)
-- -----------------------------------------------------------------------------
CREATE TABLE processed_event (
                                 event_id     UUID         NOT NULL,
                                 handler      VARCHAR(100) NOT NULL,
                                 processed_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

                                 PRIMARY KEY (event_id, handler)
);