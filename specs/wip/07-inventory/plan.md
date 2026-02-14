# Implementation Plan: Inventory Module

**Feature**: 07-inventory  
**Module**: Gestión Integral de Inventarios  
**Created**: 2026-02-01  
**Status**: ⏳ Awaiting Approval

---

## 📋 Project Summary

| Property | Value |
|----------|-------|
| **Estimated Complexity** | 128 Story Points (~16 days @ 8 SP/day) |
| **Total Tasks** | 128 tasks |
| **Total Phases** | 11 phases |
| **Risk Level** | 🔴 HIGH (Complex concurrency, multi-warehouse) |
| **Dependencies** | Document Types, Units of Measure, Warehouses, Tax Types |
| **Blocks** | Sales, Purchases, Production modules |

---

## 🎯 Objectives

1. **Product Management**: CRUD completo con múltiples códigos de barras y categorización jerárquica
2. **Multi-warehouse Stock**: Control independiente por bodega con mínimos/máximos
3. **Concurrency Control**: Optimistic locking para prevenir inconsistencias
4. **Stock Alerts**: Sistema automático de alertas de stock bajo
5. **Inventory Adjustments**: Ajustes auditados con motivo obligatorio
6. **Warehouse Transfers**: Flujo completo PENDING → APPROVED → RECEIVED
7. **Unit Conversions**: Conversiones automáticas entre unidades de medida
8. **Kardex Tracking**: Trazabilidad completa de todos los movimientos

---

## 📦 Task Distribution by Phase

| Phase | Description | Tasks | SP | Duration |
|-------|-------------|-------|----|------------|
| **PHASE 1** | Setup | 4 | 4 | 0.5 days |
| **PHASE 2** | Foundational | 5 | 5 | 0.63 days |
| **PHASE 3** | Products & Barcodes (US-01) | 23 | 23 | 2.88 days |
| **PHASE 4** | Hierarchical Categories (US-02) | 13 | 13 | 1.63 days |
| **PHASE 5** | Multi-warehouse Stock (US-03) | 21 | 21 | 2.63 days |
| **PHASE 6** | Stock Alerts (US-04) | 5 | 5 | 0.63 days |
| **PHASE 7** | Inventory Adjustments (US-05) | 6 | 6 | 0.75 days |
| **PHASE 8** | Warehouse Transfers (US-06) | 20 | 20 | 2.5 days |
| **PHASE 9** | Unit Conversions (US-07) | 11 | 11 | 1.38 days |
| **PHASE 10** | Kardex Tracking (US-08) | 7 | 7 | 0.88 days |
| **PHASE 11** | Polish & Testing | 13 | 13 | 1.63 days |

---

## 🚀 Detailed Implementation Plan

### PHASE 1: Setup (4 SP)

**Goal**: Configurar infraestructura base del módulo.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-001 | Crear estructura de carpetas inventory/ | 1 | - |
| INV-002 | Configurar dependencias (decimal, mapstruct) | 1 | - |
| INV-003 | Crear enums (InventoryMovementType, TransferStatus) | 1 | - |
| INV-004 | Crear excepciones específicas (InsufficientStockException, etc.) | 1 | - |

---

### PHASE 2: Foundational (5 SP)

**Goal**: Modelos base y tablas de base de datos.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-005 | Crear UnitOfMeasure domain model | 1 | INV-001 |
| INV-006 | Crear ProductCategory domain model | 1 | INV-001 |
| INV-007 | Crear interfaces de repositorio base | 1 | INV-005, INV-006 |
| INV-008 | Migración Flyway V1.4__inventory_base_tables.sql | 1 | - |
| INV-009 | Seed datos: unidades de medida predefinidas | 1 | INV-008 |

---

### PHASE 3: Products & Barcodes - US-01 (23 SP)

**Goal**: Gestión completa de productos con múltiples códigos de barras.

#### Domain Layer (8 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-010 | Crear Product domain model | 2 | INV-007 |
| INV-011 | Crear ProductBarcode domain model | 1 | INV-010 |
| INV-012 | Ampliar ProductPort con métodos de búsqueda | 1 | INV-010 |
| INV-013 | Contract tests POST /api/products | 1 | - |
| INV-014 | Contract tests GET /api/products/{id} | 1 | - |
| INV-015 | Integration test validación SKU único | 1 | - |
| INV-016 | Integration test búsqueda por barcode | 1 | - |

#### Application Layer (5 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-017 | Crear DTOs (ProductDTO, CreateProductRequest, etc.) | 1 | INV-010 |
| INV-018 | Implementar CreateProductUseCase | 1 | INV-012, INV-017 |
| INV-019 | Implementar UpdateProductUseCase | 1 | INV-012, INV-017 |
| INV-020 | Implementar DeleteProductUseCase (soft delete) | 1 | INV-012 |
| INV-021 | Implementar SearchProductByBarcodeUseCase | 1 | INV-012 |

#### Infrastructure Layer (10 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-022 | Crear ProductEntity (JPA) | 1 | INV-010 |
| INV-023 | Crear ProductBarcodeEntity (JPA) | 1 | INV-011 |
| INV-024 | Crear ProductJpaRepository | 1 | INV-022 |
| INV-025 | Crear ProductBarcodeJpaRepository | 1 | INV-023 |
| INV-026 | Crear ProductMapper (MapStruct) | 1 | INV-010, INV-022 |
| INV-027 | Implementar ProductAdapter | 2 | INV-012, INV-024, INV-026 |
| INV-028 | Crear ProductController (REST API) | 2 | INV-018 to INV-021 |
| INV-029 | Migración V1.4.1__products_tables.sql | 1 | INV-008 |

---

### PHASE 4: Hierarchical Categories - US-02 (13 SP)

**Goal**: Categorías con jerarquía padre-hijo.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-030 | Contract tests CRUD categorías | 1 | - |
| INV-031 | Unit test validación de ciclos | 1 | INV-006 |
| INV-032 | Crear DTOs (CategoryDTO, CategoryTreeDTO) | 1 | INV-006 |
| INV-033 | Implementar CreateCategoryUseCase | 1 | INV-007 |
| INV-034 | Implementar UpdateCategoryUseCase | 1 | INV-007 |
| INV-035 | Implementar DeleteCategoryUseCase | 1 | INV-007 |
| INV-036 | Implementar ListCategoriesUseCase (árbol) | 1 | INV-007 |
| INV-037 | Crear CategoryEntity (JPA) | 1 | INV-006 |
| INV-038 | Crear CategoryJpaRepository | 1 | INV-037 |
| INV-039 | Crear CategoryMapper | 1 | INV-006, INV-037 |
| INV-040 | Implementar CategoryAdapter | 1 | INV-007, INV-038 |
| INV-041 | Crear CategoryController | 2 | INV-033 to INV-036 |

---

### PHASE 5: Multi-warehouse Stock - US-03 (21 SP)

**Goal**: Control de stock por producto/bodega con concurrencia optimista.

#### Domain Layer (8 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-042 | Crear Stock domain model (con @Version) | 2 | INV-010 |
| INV-043 | Crear InventoryMovement domain model | 2 | INV-010 |
| INV-044 | Crear AverageCostCalculator service | 1 | INV-042 |
| INV-045 | Crear StockValidator service | 1 | INV-042 |
| INV-046 | Unit test Stock domain logic | 1 | INV-042 |
| INV-047 | Unit test AverageCostCalculator | 1 | INV-044 |

#### Application Layer (6 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-048 | Crear DTOs (StockDTO, StockSummaryDTO) | 1 | INV-042 |
| INV-049 | Implementar InitializeStockUseCase | 1 | INV-042, INV-043 |
| INV-050 | Implementar GetStockByProductUseCase | 1 | INV-042 |
| INV-051 | Crear StockMapper | 1 | INV-042 |
| INV-052 | Contract tests gestión de stock | 1 | - |
| INV-053 | Integration test concurrencia optimista (Testcontainers) | 1 | INV-042 |

#### Infrastructure Layer (7 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-054 | Crear StockEntity (JPA con @Version) | 1 | INV-042 |
| INV-055 | Crear InventoryMovementEntity (JPA) | 1 | INV-043 |
| INV-056 | Crear StockJpaRepository | 1 | INV-054 |
| INV-057 | Crear InventoryMovementJpaRepository | 1 | INV-055 |
| INV-058 | Implementar StockAdapter | 2 | INV-056 |
| INV-059 | Crear StockController | 2 | INV-049, INV-050 |
| INV-060 | Migración V1.4.2__stock_tables.sql | 1 | INV-029 |

---

### PHASE 6: Stock Alerts - US-04 (5 SP)

**Goal**: Alertas automáticas de stock bajo.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-061 | Crear LowStockAlertEvent (domain event) | 1 | INV-042 |
| INV-062 | Implementar GetLowStockAlertsUseCase | 1 | INV-042 |
| INV-063 | Crear LowStockAlertDTO | 1 | INV-061 |
| INV-064 | Event listener para publicar alertas | 1 | INV-061 |
| INV-065 | Endpoint GET /api/stock/low-alerts | 1 | INV-062 |

---

### PHASE 7: Inventory Adjustments - US-05 (6 SP)

**Goal**: Ajustes de inventario con auditoría.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-066 | Crear AdjustStockCommand DTO | 1 | INV-042 |
| INV-067 | Implementar AdjustStockUseCase | 2 | INV-042, INV-043, INV-066 |
| INV-068 | Validar permisos ADJUST_INVENTORY | 1 | INV-067 |
| INV-069 | Endpoint POST /api/stock/adjust | 1 | INV-067 |
| INV-070 | Integration test ajustes | 1 | INV-067 |

---

### PHASE 8: Warehouse Transfers - US-06 (20 SP)

**Goal**: Transferencias entre bodegas con flujo completo.

#### Domain Layer (8 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-071 | Crear WarehouseTransfer domain model (con @Version) | 2 | INV-003, INV-042 |
| INV-072 | Crear WarehouseTransferDetail domain model | 1 | INV-071 |
| INV-073 | Ampliar TransferPort interface | 1 | INV-071 |
| INV-074 | Unit test flujo de estados (PENDING → APPROVED → RECEIVED) | 2 | INV-071 |
| INV-075 | Unit test validaciones de transferencia | 2 | INV-071 |

#### Application Layer (6 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-076 | Crear DTOs (TransferDTO, CreateTransferRequest) | 1 | INV-071 |
| INV-077 | Implementar CreateTransferUseCase | 1 | INV-071, INV-045 |
| INV-078 | Implementar ApproveTransferUseCase | 1 | INV-071 |
| INV-079 | Implementar ReceiveTransferUseCase | 2 | INV-071, INV-042, INV-043 |
| INV-080 | Implementar CancelTransferUseCase | 1 | INV-071 |

#### Infrastructure Layer (6 SP)

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-081 | Crear WarehouseTransferEntity (JPA) | 1 | INV-071 |
| INV-082 | Crear WarehouseTransferDetailEntity (JPA) | 1 | INV-072 |
| INV-083 | Crear TransferJpaRepository | 1 | INV-081 |
| INV-084 | Implementar TransferAdapter | 1 | INV-073, INV-083 |
| INV-085 | Crear TransferController (6 endpoints) | 2 | INV-077 to INV-080 |
| INV-086 | Migración V1.4.3__transfers_tables.sql | 1 | INV-060 |
| INV-087 | Integration test flujo completo de transferencia | 2 | INV-077 to INV-080 |

---

### PHASE 9: Unit Conversions - US-07 (11 SP)

**Goal**: Conversiones automáticas entre unidades de medida.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-088 | Crear ProductUnitConversion domain model | 1 | INV-010 |
| INV-089 | Crear UnitConverter service | 2 | INV-088 |
| INV-090 | Ampliar ProductPort con conversiones | 1 | INV-088 |
| INV-091 | Crear DTOs (UnitConversionDTO) | 1 | INV-088 |
| INV-092 | Implementar AddUnitConversionUseCase | 1 | INV-088 |
| INV-093 | Crear ProductUnitConversionEntity (JPA) | 1 | INV-088 |
| INV-094 | Crear UnitConversionJpaRepository | 1 | INV-093 |
| INV-095 | Endpoint POST /api/products/{id}/unit-conversions | 1 | INV-092 |
| INV-096 | Migración V1.4.4__unit_conversions.sql | 1 | INV-086 |
| INV-097 | Unit test conversiones | 1 | INV-089 |

---

### PHASE 10: Kardex Tracking - US-08 (7 SP)

**Goal**: Consulta de historial de movimientos (kardex).

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-098 | Crear KardexEntry DTO | 1 | INV-043 |
| INV-099 | Crear KardexFilter DTO | 1 | INV-098 |
| INV-100 | Implementar GetKardexUseCase (con filtros) | 2 | INV-043, INV-099 |
| INV-101 | Implementar ExportKardexUseCase (Excel) | 2 | INV-100 |
| INV-102 | Endpoint GET /api/products/{id}/kardex | 1 | INV-100 |
| INV-103 | Endpoint GET /api/products/{id}/kardex/export | 1 | INV-101 |
| INV-104 | Integration test kardex query | 1 | INV-100 |

---

### PHASE 11: Polish & Testing (13 SP)

**Goal**: Pulir, optimizar y completar testing.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| INV-105 | Performance test: 50 transacciones concurrentes | 2 | INV-042 |
| INV-106 | Performance test: Kardex con 100k movimientos | 2 | INV-100 |
| INV-107 | End-to-end test: flujo completo de venta | 2 | ALL |
| INV-108 | Documentación OpenAPI completa (Swagger) | 1 | ALL |
| INV-109 | Optimización de queries (JOIN FETCH, indexes) | 2 | ALL |
| INV-110 | Retry mechanism para OptimisticLockException | 1 | INV-042 |
| INV-111 | Global exception handler para errores de inventario | 1 | INV-004 |
| INV-112 | Validación cobertura de tests >= 80% | 1 | ALL |
| INV-113 | Code review y refactoring | 1 | ALL |

---

## 📊 Dependencies Graph

```
PHASE 1 (Setup)
    ↓
PHASE 2 (Foundational: Base models & DB)
    ↓
PHASE 3 (Products) + PHASE 4 (Categories)
    ↓
PHASE 5 (Stock)
    ↓
PHASE 6 (Alerts) + PHASE 7 (Adjustments)
    ↓
PHASE 8 (Transfers)
    ↓
PHASE 9 (Unit Conversions) + PHASE 10 (Kardex)
    ↓
PHASE 11 (Polish & Testing)
```

---

## 🎯 Milestones

| Milestone | Tasks | Completion Criteria |
|-----------|-------|---------------------|
| **M1: Foundation Ready** | INV-001 to INV-009 | Base structure and DB ready |
| **M2: Products Functional** | INV-010 to INV-029 | Full product CRUD with barcodes |
| **M3: Stock Control Live** | INV-042 to INV-060 | Multi-warehouse stock working |
| **M4: Transfers Operational** | INV-071 to INV-087 | Complete transfer flow working |
| **M5: Full Traceability** | INV-098 to INV-104 | Kardex query and export ready |
| **M6: Production Ready** | INV-105 to INV-113 | All tests passing, optimized, documented |

---

## ⚠️ Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Race conditions en stock | 🔴 Critical | Medium | Optimistic locking + retry mechanism + integration tests |
| Performance degradation (kardex) | 🟡 Medium | Medium | Indexes, pagination, query optimization |
| Cálculo incorrecto de costo promedio | 🔴 Critical | Low | Unit tests exhaustivos, validación con contabilidad |
| Transferencias sin validación de stock | 🔴 Critical | Low | Validación en CreateTransfer antes de guardar |
| Inconsistencia entre stock y movimientos | 🔴 Critical | Low | Transacciones atómicas, events para sincronización |

---

## 📏 Definition of Done

✅ All 128 tasks completed and code merged  
✅ Unit test coverage >= 80%  
✅ Integration tests passing (including concurrency tests)  
✅ Performance tests meet targets (< 200ms stock query p95)  
✅ All endpoints documented with Swagger  
✅ Code review approved by Tech Lead  
✅ Manual QA testing completed  
✅ Database migrations applied successfully  
✅ No critical or high severity bugs open  

---

**Status**: ⏳ Awaiting Approval  
**Next Step**: Product Owner & Tech Lead Approval → Move to PHASE 1 Implementation  
**Estimated Duration**: 16 days (128 SP @ 8 SP/day)
