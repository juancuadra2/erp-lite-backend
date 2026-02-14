SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- V7: Create tax_types table
CREATE TABLE tax_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    percentage DECIMAL(7,4) NOT NULL,
    is_included BOOLEAN NOT NULL DEFAULT FALSE,
    application_type ENUM('SALE', 'PURCHASE', 'BOTH') NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    -- Indexes for common queries
    INDEX idx_code (code),
    INDEX idx_enabled (enabled),
    INDEX idx_uuid (uuid),
    INDEX idx_application_type (application_type),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_enabled_deleted (enabled, deleted_at),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comments for documentation
ALTER TABLE tax_types
    COMMENT = 'Catálogo de tipos de impuestos (IVA, ReteFuente, ReteIVA, ICA, etc.)';
