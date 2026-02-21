# Status: Product Categories Module

**Feature**: Product Categories (Categorías de Productos)
**Status**: 🔴 NOT STARTED
**Version**: 1.0.0
**Architecture**: Hexagonal (Aligned with Scaffolding)
**Last Updated**: February 20, 2026
**Prerequisite of**: 11-items

---

## Progress Overview

| Phase | Status | Progress | Tasks |
|-------|--------|----------|-------|
| 1. Domain Foundation | 🔴 Not Started | 0/5 | 0% |
| 2. Database Schema | 🔴 Not Started | 0/1 | 0% |
| 3. Persistence Layer | 🔴 Not Started | 0/5 | 0% |
| 4. Application Layer | 🔴 Not Started | 0/3 | 0% |
| 5. Web Layer | 🔴 Not Started | 0/5 | 0% |
| 6. Testing | 🔴 Not Started | 0/5 | 0% |
| 7. Auditoría y Cierre | 🔴 Not Started | 0/1 | 0% |
| **TOTAL** | 🔴 **0%** | **0/25** | **0%** |

---

## Key Decisions

- **KD-01**: Catálogo **plano** — sin jerarquía padre/hijo. La clasificación adicional se gestiona con tags en Feature 11
- **KD-02**: El `code` es asignable solo en creación; no es modificable vía PUT (campo ausente en UpdateProductCategoryRequestDto)
- **KD-03**: No hay datos semilla; las categorías son definidas por cada instalación del sistema
- **KD-04**: `CategoryInUseException` y `canDelete()` son stubs que retornan OK hasta Feature 11 (Items)
- **KD-05**: Complejidad equivalente a Feature 10 (Brands) — mismo patrón de implementación

## Business Rules Summary

- **RN-01**: Código único, case-insensitive → 409 si duplicado
- **RN-02**: El código no es modificable tras la creación
- **RN-03**: No eliminar si hay ítems referenciando la categoría → stub retorna OK hasta Feature 11
- **RN-04**: Recién creados: `active = true` por defecto

## Dependencies

- **Upstream**: Ninguna (catálogo independiente)
- **Downstream**: Feature 11 (Items) usa categoryId (obligatorio) en cada ítem

## Migrations

- **V16**: `create_product_categories_table` — tabla plana con índices estándar

## Target Metrics

- Tests: ~50+ tests
- Cobertura: 100% instrucciones en archivos modificados/nuevos
- Endpoints: 7
