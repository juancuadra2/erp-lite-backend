SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- V2: Insert Colombia document types
INSERT INTO document_types (uuid, code, name, description, active, created_at, updated_at)
VALUES
    (UUID_TO_BIN(UUID()), 'NIT', 'Número de Identificación Tributaria', 'Documento de identificación para empresas en Colombia', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'CC', 'Cédula de Ciudadanía', 'Documento de identificación para ciudadanos colombianos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'CE', 'Cédula de Extranjería', 'Documento de identificación para extranjeros residentes en Colombia', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'PA', 'Pasaporte', 'Documento de identificación internacional', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'TI', 'Tarjeta de Identidad', 'Documento de identificación para menores de edad en Colombia', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (UUID_TO_BIN(UUID()), 'RC', 'Registro Civil', 'Documento de identificación para recién nacidos en Colombia', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
