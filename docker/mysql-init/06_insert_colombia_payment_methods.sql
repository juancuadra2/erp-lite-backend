SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- V6: Insert Colombia payment methods
-- Common payment methods used in Colombia

INSERT INTO payment_methods (uuid, code, name, enabled, created_at) VALUES
-- Efectivo
(UNHEX(REPLACE(UUID(), '-', '')), 'CASH', 'Efectivo', TRUE, NOW()),

-- Tarjetas
(UNHEX(REPLACE(UUID(), '-', '')), 'CREDIT_CARD', 'Tarjeta de Crédito', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'DEBIT_CARD', 'Tarjeta Débito', TRUE, NOW()),

-- Transferencias bancarias
(UNHEX(REPLACE(UUID(), '-', '')), 'BANK_TRANSFER', 'Transferencia Bancaria', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'PSE', 'PSE - Pagos Seguros en Línea', TRUE, NOW()),

-- Cheques
(UNHEX(REPLACE(UUID(), '-', '')), 'CHECK', 'Cheque', TRUE, NOW()),

-- Billeteras digitales
(UNHEX(REPLACE(UUID(), '-', '')), 'NEQUI', 'Nequi', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'DAVIPLATA', 'Daviplata', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'BANCOLOMBIA_TRANSFER', 'Transferencia Bancolombia', TRUE, NOW()),

-- Créditos y otros
(UNHEX(REPLACE(UUID(), '-', '')), 'CREDIT', 'Crédito', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'CONSIGNMENT', 'Consignación', TRUE, NOW()),

-- Pagos electrónicos adicionales
(UNHEX(REPLACE(UUID(), '-', '')), 'PAYPAL', 'PayPal', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'MERCADO_PAGO', 'Mercado Pago', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'WOMPI', 'Wompi', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'EPAYCO', 'ePayco', TRUE, NOW()),

-- Bonos y otros
(UNHEX(REPLACE(UUID(), '-', '')), 'GIFT_CARD', 'Tarjeta de Regalo', TRUE, NOW()),
(UNHEX(REPLACE(UUID(), '-', '')), 'VOUCHER', 'Bono o Vale', TRUE, NOW()),

-- Otros métodos
(UNHEX(REPLACE(UUID(), '-', '')), 'OTHER', 'Otro Método de Pago', TRUE, NOW());

-- Add audit information
UPDATE payment_methods 
SET created_by = 1, updated_by = 1, updated_at = NOW()
WHERE created_by IS NULL;
