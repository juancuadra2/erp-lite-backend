SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- V8: Insert Colombia tax types (10 common taxes)
INSERT INTO tax_types (uuid, code, name, percentage, is_included, application_type, enabled, created_at, updated_at)
VALUES
    -- IVA (Impuesto al Valor Agregado) - Aplicable a ventas y compras
    (UUID_TO_BIN(UUID()), 'IVA19', 'IVA 19%', 19.0000, FALSE, 'BOTH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'IVA5', 'IVA 5%', 5.0000, FALSE, 'BOTH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'IVA0', 'IVA 0%', 0.0000, FALSE, 'BOTH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- ReteFuente (Retención en la Fuente) - Solo compras
    (UUID_TO_BIN(UUID()), 'RETE_SERV_2.5', 'ReteFuente Servicios 2.5%', 2.5000, FALSE, 'PURCHASE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'RETE_SERV_4.0', 'ReteFuente Servicios 4%', 4.0000, FALSE, 'PURCHASE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'RETE_HON_10.0', 'ReteFuente Honorarios 10%', 10.0000, FALSE, 'PURCHASE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'RETE_COMP_2.5', 'ReteFuente Compras 2.5%', 2.5000, FALSE, 'PURCHASE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- ReteIVA (Retención de IVA) - Solo compras
    (UUID_TO_BIN(UUID()), 'RETEIVA_15', 'ReteIVA 15%', 15.0000, FALSE, 'PURCHASE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- ICA (Impuesto de Industria y Comercio - Bogotá) - Aplicable a ventas y compras
    (UUID_TO_BIN(UUID()), 'ICA_BOG_SERV', 'ICA Bogotá Servicios 0.414%', 0.4140, FALSE, 'BOTH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'ICA_BOG_IND', 'ICA Bogotá Industrial 0.966%', 0.9660, FALSE, 'BOTH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    percentage = VALUES(percentage),
    is_included = VALUES(is_included),
    application_type = VALUES(application_type),
    enabled = VALUES(enabled),
    updated_at = CURRENT_TIMESTAMP;
