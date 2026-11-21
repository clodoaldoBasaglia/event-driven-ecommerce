-- =============================================================================
-- inventory-service | seed de DESENVOLVIMENTO (migration repetível)
-- Só é carregado quando spring.flyway.locations inclui classpath:db/seed
-- (configurado apenas no application-dev.yml).
--
-- É idempotente: pode rodar várias vezes sem duplicar nem sobrescrever dados.
-- IDs fixos e fáceis de digitar, para usar nas chamadas ao order-service.
-- =============================================================================

INSERT INTO product_stock (product_id, sku, name, available_quantity, reserved_quantity)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'NOTE-GAMER-15',  'Notebook Gamer 15"',            25, 0),
    ('00000000-0000-0000-0000-000000000002', 'MOUSE-SEM-FIO',  'Mouse Sem Fio Ergonômico',     200, 0),
    ('00000000-0000-0000-0000-000000000003', 'TECL-MEC-ABNT',  'Teclado Mecânico ABNT2',        80, 0),
    ('00000000-0000-0000-0000-000000000004', 'MON-27-4K',      'Monitor 27" 4K',                15, 0),
    ('00000000-0000-0000-0000-000000000005', 'HEADSET-USB',    'Headset USB com Microfone',    120, 0),
    ('00000000-0000-0000-0000-000000000006', 'WEBCAM-FHD',     'Webcam Full HD',                60, 0),
    ('00000000-0000-0000-0000-000000000007', 'SSD-1TB-NVME',   'SSD NVMe 1TB',                  40, 0),
    -- estoque baixo: serve para testar rejeição por quantidade insuficiente
    ('00000000-0000-0000-0000-000000000008', 'CADEIRA-ERGO',   'Cadeira Ergonômica (últimas)',   3, 0),
    -- sem estoque: serve para testar StockRejected
    ('00000000-0000-0000-0000-000000000009', 'CONSOLE-NEXT',   'Console Edição Limitada',        0, 0)
    ON CONFLICT (product_id) DO NOTHING;

-- Entrada inicial no livro-razão (uma única vez por produto com saldo inicial)
INSERT INTO stock_movement (product_id, reservation_id, type, quantity)
SELECT ps.product_id, NULL, 'INBOUND', ps.available_quantity
FROM product_stock ps
WHERE ps.available_quantity > 0
  AND ps.sku IN ('NOTE-GAMER-15', 'MOUSE-SEM-FIO', 'TECL-MEC-ABNT', 'MON-27-4K',
                 'HEADSET-USB', 'WEBCAM-FHD', 'SSD-1TB-NVME', 'CADEIRA-ERGO')
  AND NOT EXISTS (
    SELECT 1 FROM stock_movement sm
    WHERE sm.product_id = ps.product_id AND sm.type = 'INBOUND'
);