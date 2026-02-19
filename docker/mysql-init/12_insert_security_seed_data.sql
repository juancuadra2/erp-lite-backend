-- Roles por defecto del sistema
INSERT INTO roles (id, name, description, active, created_at)
VALUES
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')), 'ADMIN', 'Administrador del sistema con acceso total', TRUE, NOW()),
    (UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')), 'USER',  'Usuario estándar con acceso básico',         TRUE, NOW());

-- Usuario administrador inicial
-- Password: Admin123! (BCrypt hash generado con cost factor 12)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, active, failed_attempts, created_at)
VALUES (
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')),
    'admin',
    'admin@erplite.local',
    '$2a$12$LcVMqHMBSFCiCNFJstMB7OuXFuMaRXPBDU2JF9dksSJ8kRl0GNnki',
    'Admin',
    'Sistema',
    TRUE,
    0,
    NOW()
);

-- Asignar rol ADMIN al usuario admin
INSERT INTO user_roles (user_id, role_id)
VALUES (
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')),
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''))
);
