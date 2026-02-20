INSERT IGNORE INTO permissions (id, entity, action, condition_expr, description)
VALUES
    (UNHEX(REPLACE('10000000-0000-0000-0000-000000000001', '-', '')), 'WAREHOUSE', 'CREATE', NULL, 'Crear bodegas'),
    (UNHEX(REPLACE('10000000-0000-0000-0000-000000000002', '-', '')), 'WAREHOUSE', 'READ',   NULL, 'Consultar bodegas'),
    (UNHEX(REPLACE('10000000-0000-0000-0000-000000000003', '-', '')), 'WAREHOUSE', 'UPDATE', NULL, 'Actualizar bodegas'),
    (UNHEX(REPLACE('10000000-0000-0000-0000-000000000004', '-', '')), 'WAREHOUSE', 'DELETE', NULL, 'Eliminar bodegas');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')),
    id
FROM permissions
WHERE entity = 'WAREHOUSE';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT
    UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')),
    id
FROM permissions
WHERE entity = 'WAREHOUSE' AND action = 'READ';
