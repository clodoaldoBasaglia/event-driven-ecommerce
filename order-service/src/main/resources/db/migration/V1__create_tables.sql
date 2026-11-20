-- =============================================================================
-- order-service | V1: schema inicial
-- =============================================================================

-- -----------------------------------------------------------------------------
-- orders: agregado principal (pedido)
-- Status do fluxo da Saga:
--   PENDING -> STOCK_RESERVED -> CONFIRMED
--   PENDING | STOCK_RESERVED -> CANCELLED
-- customer_id não tem FK: clientes pertencem a outro contexto (não há tabela aqui).
-- -----------------------------------------------------------------------------
CREATE TABLE orders (
    id                  UUID            PRIMARY KEY,
    customer_id         UUID            NOT NULL,
    status              VARCHAR(30)     NOT NULL,
    total_amount        NUMERIC(12, 2)  NOT NULL,
    currency            VARCHAR(3)      NOT NULL DEFAULT 'BRL',
    cancellation_reason VARCHAR(255),
    idempotency_key     VARCHAR(100),
    version             BIGINT          NOT NULL DEFAULT 0,  -- lock otimista (@Version)
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT ck_orders_status
        CHECK (status IN ('PENDING', 'STOCK_RESERVED', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT ck_orders_total_amount
        CHECK (total_amount >= 0),
    -- mesma chave de idempotência não cria dois pedidos para o mesmo cliente
    CONSTRAINT uq_orders_customer_idempotency
        UNIQUE (customer_id, idempotency_key)
);

CREATE INDEX idx_orders_customer_created ON orders (customer_id, created_at DESC);
CREATE INDEX idx_orders_status           ON orders (status);

-- -----------------------------------------------------------------------------
-- order_items: itens do pedido (preço e nome são "fotografados" no momento da compra)
-- -----------------------------------------------------------------------------
CREATE TABLE order_items (
    id           UUID            PRIMARY KEY,
    order_id     UUID            NOT NULL,
    product_id   UUID            NOT NULL,
    product_name VARCHAR(200)    NOT NULL,
    quantity     INTEGER         NOT NULL,
    unit_price   NUMERIC(12, 2)  NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_order_items_quantity   CHECK (quantity > 0),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price >= 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- -----------------------------------------------------------------------------
-- order_status_history: trilha de auditoria das transições de status
-- -----------------------------------------------------------------------------
CREATE TABLE order_status_history (
    id          BIGSERIAL       PRIMARY KEY,
    order_id    UUID            NOT NULL,
    from_status VARCHAR(30),                    -- NULL na criação do pedido
    to_status   VARCHAR(30)     NOT NULL,
    reason      VARCHAR(255),
    occurred_at TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT fk_order_status_history_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_status_history_order ON order_status_history (order_id, occurred_at);

-- -----------------------------------------------------------------------------
-- outbox_event: Outbox Pattern
-- Gravado na MESMA transação que altera o pedido; um publisher separado lê as
-- linhas com published_at IS NULL e envia ao Kafka.
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
    id             UUID         PRIMARY KEY,       -- também usado como eventId no consumidor
    aggregate_type VARCHAR(50)  NOT NULL,          -- ex.: 'Order'
    aggregate_id   UUID         NOT NULL,          -- ex.: id do pedido (vira a key da mensagem)
    event_type     VARCHAR(100) NOT NULL,          -- ex.: 'OrderCreated'
    topic          VARCHAR(100) NOT NULL,          -- ex.: 'order.created'
    payload        JSONB        NOT NULL,
    attempts       INTEGER      NOT NULL DEFAULT 0,
    last_error     VARCHAR(500),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ                     -- NULL = ainda não publicado
);

-- índice parcial: só as pendentes, na ordem de criação (o poller usa essa consulta)
CREATE INDEX idx_outbox_event_pending
    ON outbox_event (created_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_event_aggregate ON outbox_event (aggregate_type, aggregate_id);

-- -----------------------------------------------------------------------------
-- processed_event: consumidor idempotente
-- Antes de tratar uma mensagem do Kafka, insere (event_id, handler); se a chave
-- já existir, a mensagem é duplicada e deve ser ignorada.
-- -----------------------------------------------------------------------------
CREATE TABLE processed_event (
    event_id     UUID         NOT NULL,
    handler      VARCHAR(100) NOT NULL,            -- ex.: 'StockReservedHandler'
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now(),

    PRIMARY KEY (event_id, handler)
);