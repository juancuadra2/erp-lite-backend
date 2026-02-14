SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- V6: Insert Colombia payment methods
-- Common payment methods used in Colombia

INSERT INTO payment_methods (uuid, code, name, enabled, created_at) VALUES
-- Efectivo
(UUID_TO_BIN(UUID()), 'CASH', 'Efectivo', TRUE, NOW()),

-- Tarjetas
(UUID_TO_BIN(UUID()), 'CREDIT_CARD', 'Tarjeta de Crédito', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'DEBIT_CARD', 'Tarjeta Débito', TRUE, NOW()),

-- Transferencias bancarias
(UUID_TO_BIN(UUID()), 'BANK_TRANSFER', 'Transferencia Bancaria', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'PSE', 'PSE - Pagos Seguros en Línea', TRUE, NOW()),

-- Cheques
(UUID_TO_BIN(UUID()), 'CHECK', 'Cheque', TRUE, NOW()),

-- Billeteras digitales
(UUID_TO_BIN(UUID()), 'NEQUI', 'Nequi', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'DAVIPLATA', 'Daviplata', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'BANCOLOMBIA_TRANSFER', 'Transferencia Bancolombia', TRUE, NOW()),

-- Créditos y otros
(UUID_TO_BIN(UUID()), 'CREDIT', 'Crédito', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'CONSIGNMENT', 'Consignación', TRUE, NOW()),

-- Pagos electrónicos adicionales
(UUID_TO_BIN(UUID()), 'PAYPAL', 'PayPal', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'MERCADO_PAGO', 'Mercado Pago', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'WOMPI', 'Wompi', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'EPAYCO', 'ePayco', TRUE, NOW()),

-- Bonos y otros
(UUID_TO_BIN(UUID()), 'GIFT_CARD', 'Tarjeta de Regalo', TRUE, NOW()),
(UUID_TO_BIN(UUID()), 'VOUCHER', 'Bono o Vale', TRUE, NOW()),

-- Otros métodos
(UUID_TO_BIN(UUID()), 'OTHER', 'Otro Método de Pago', TRUE, NOW());

-- Add audit information
UPDATE payment_methods 
SET created_by = 1, updated_by = 1, updated_at = NOW()
WHERE created_by IS NULL;
