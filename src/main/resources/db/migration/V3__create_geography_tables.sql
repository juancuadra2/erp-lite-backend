-- V3: Create geography tables (departments and municipalities)

CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_departments_code (code),
    INDEX idx_departments_name (name),
    INDEX idx_departments_enabled (enabled),
    INDEX idx_departments_uuid (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE municipalities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid BINARY(16) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_municipality_department
        FOREIGN KEY (department_id) REFERENCES departments(id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_municipality_code_department
        UNIQUE (code, department_id),
    INDEX idx_municipalities_code (code),
    INDEX idx_municipalities_name (name),
    INDEX idx_municipalities_enabled (enabled),
    INDEX idx_municipalities_uuid (uuid),
    INDEX idx_municipalities_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
