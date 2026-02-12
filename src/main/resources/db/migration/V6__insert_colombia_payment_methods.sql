-- Insert Colombia payment methods
-- Using INSERT ... ON DUPLICATE KEY UPDATE for idempotency
INSERT INTO payment_methods (uuid, code, name, enabled, created_at, updated_at)
VALUES
    (UUID_TO_BIN(UUID()), 'CASH', 'Efectivo', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'CC', 'Tarjeta de Crédito', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'DC', 'Tarjeta Débito', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'TRANSFER', 'Transferencia Bancaria', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'PSE', 'PSE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'CHECK', 'Cheque', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'CREDIT', 'Crédito', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    enabled = VALUES(enabled),
    updated_at = CURRENT_TIMESTAMP;
