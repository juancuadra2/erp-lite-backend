# Status: Brands Module

**Feature**: Brands / Manufacturers (Marcas / Fabricantes)
**Status**: 🔴 NOT STARTED
**Version**: 1.1.0
**Architecture**: Hexagonal (Aligned with Scaffolding)
**Last Updated**: February 20, 2026
**Prerequisite of**: 11-items

---

## Progress Overview

| Phase | Status | Progress | Tasks |
|-------|--------|----------|-------|
| 1. Domain Foundation | 🔴 Not Started | 0/5 | 0% |
| 2. Database Schema | 🔴 Not Started | 0/2 | 0% |
| 3. Persistence Layer | 🔴 Not Started | 0/5 | 0% |
| 4. Application Layer | 🔴 Not Started | 0/3 | 0% |
| 5. Web Layer | 🔴 Not Started | 0/5 | 0% |
| 6. Testing | 🔴 Not Started | 0/5 | 0% |
| 7. Auditoría y Cierre | 🔴 Not Started | 0/1 | 0% |
| **TOTAL** | 🔴 **0%** | **0/26** | **0%** |

---

## Key Decisions

- **KD-01**: El `code` es asignable solo en creación; no es modificable vía PUT (campo ausente en UpdateBrandRequestDto)
- **KD-02**: No hay datos semilla; las marcas son definidas por cada instalación del sistema
- **KD-03**: `BrandInUseException` y `canDelete()` son stubs que retornan OK hasta Feature 11 (Items)
- **KD-04**: Catálogo plano sin jerarquía — similar en complejidad a ProductCategory (Feature 09)

## Business Rules Summary

- **RN-01**: Código único, case-insensitive → 409 si duplicado
- **RN-02**: El código no es modificable tras la creación
- **RN-03**: No eliminar si hay ítems referenciando la marca → stub retorna OK hasta Feature 11
- **RN-04**: Recién creados: `active = true` por defecto

## Dependencies

- **Upstream**: Ninguna (catálogo independiente)
- **Downstream**: Feature 11 (Items) usa brandId (opcional) en productos

## Migrations

- **V18**: `create_brands_table` — tabla plana con índices estándar
- **V19**: `insert_brand_permissions` — permisos BRAND para roles ADMIN (CRUD) y USER (READ)

## Target Metrics

- Tests: ~50+ tests
- Cobertura: 100% instrucciones en archivos modificados/nuevos
- Endpoints: 7
- Story Points: 51
