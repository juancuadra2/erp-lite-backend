INSERT IGNORE INTO warehouses (uuid, code, name, description, type, active, created_at)
VALUES
    (UUID(), 'BOD-001', 'Bodega Principal', 'Bodega central de operaciones', 'PRINCIPAL', TRUE, NOW()),
    (UUID(), 'BOD-002', 'Sucursal Norte',   'Bodega de distribución norte',  'SUCURSAL',  TRUE, NOW());
