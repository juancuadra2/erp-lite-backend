-- =============================================
-- Flyway schema history table
-- This ensures Flyway skips migrations already applied via Docker init
-- =============================================
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_flyway_schema_history_success ON flyway_schema_history (success);

INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES
    (1, '1', 'create document types tables', 'SQL', 'V1__create_document_types_tables.sql', NULL, 'docker-init', CURRENT_TIMESTAMP, 0, TRUE),
    (2, '2', 'insert colombia document types', 'SQL', 'V2__insert_colombia_document_types.sql', NULL, 'docker-init', CURRENT_TIMESTAMP, 0, TRUE),
    (3, '3', 'create geography tables', 'SQL', 'V3__create_geography_tables.sql', NULL, 'docker-init', CURRENT_TIMESTAMP, 0, TRUE),
    (4, '4', 'insert colombia geography', 'SQL', 'V4__insert_colombia_geography.sql', NULL, 'docker-init', CURRENT_TIMESTAMP, 0, TRUE);
