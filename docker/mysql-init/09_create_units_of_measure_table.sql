SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- V9: Create units_of_measure table
CREATE TABLE units_of_measure (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL UNIQUE,
    abbreviation VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    -- Indexes for common queries
    INDEX idx_units_of_measure_uuid (uuid),
    INDEX idx_units_of_measure_name (name),
    INDEX idx_units_of_measure_abbreviation (abbreviation),
    INDEX idx_units_of_measure_enabled (enabled),
    INDEX idx_units_of_measure_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE units_of_measure
    COMMENT = 'Catálogo de unidades de medida';
