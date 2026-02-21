# ERP Lite Backend - Estado General

**Última actualización**: 2026-02-20
**Estado del proyecto**: En desarrollo activo

---

## 📊 Dashboard General

### Métricas Globales

| Métrica | Valor |
|---------|-------|
| Tests totales | **1332** (0 fallos) |
| Build | **SUCCESS** |
| Features completados | **6/8** |

---

## 🗂️ Estado de Features

| # | Feature | Estado | Progreso | Tests |
|---|---------|--------|----------|-------|
| 01 | document-types | 🟡 En cierre | 87% (33/38 tasks) | ✅ |
| 02 | geography | ✅ Completo | 100% | ✅ |
| 03 | payment-methods | ✅ Completo | 100% (29/29 tasks) | ✅ |
| 04 | tax-types | ✅ Completo | 100% (32/32 tasks) | ✅ |
| 05 | units-of-measure | ✅ Completo | 100% (38/38 tasks) | ✅ |
| 06 | security | ✅ Completo | 100% | ✅ |
| 07 | inventory | ⏳ No iniciado | 0% (0/128 tasks) | — |
| 08 | warehouses | ✅ Completo | 100% (39/39 tasks) | ✅ (95 tests) |

---

## ✅ Features Completados

### 02 - Geography (Geografía)
- **Última validación**: 2026-02-18
- **Tests**: pasando
- **Cobertura**: 99.13% global
- **Destacado**: Endpoint `GET /departments/{uuid}/municipalities` con `MunicipalitySimplifiedDto`
- **Endpoints**: 10 departments + 8 municipalities = 18 total
- **Seed data**: 33 departamentos + ~400 municipios (DANE)
- **Detalle**: [features/02-geography/STATUS.md](features/02-geography/STATUS.md)

### 03 - Payment Methods (Métodos de Pago)
- **Última validación**: 2026-02-17
- **Tests**: pasando
- **Endpoints**: 8 (`/api/v1/payment-methods`)
- **Seed data**: métodos de pago colombianos
- **Detalle**: [features/03-payment-methods/STATUS.md](features/03-payment-methods/STATUS.md)

### 04 - Tax Types (Tipos de Impuesto)
- **Última validación**: 2026-02-17
- **Tests**: 157 en el módulo
- **Endpoints**: 7 (`/api/v1/tax-types`)
- **Seed data**: 10 impuestos colombianos (IVA, ReteFuente, ICA, etc.)
- **Detalle**: [features/04-tax-types/STATUS.md](features/04-tax-types/STATUS.md)

### 05 - Units of Measure (Unidades de Medida)
- **Última validación**: 2026-02-18
- **Cobertura módulo**: 100% en todas las clases
- **Endpoints**: 8 (`/api/v1/units-of-measure`)
- **Seed data**: 15 unidades de medida colombianas
- **Detalle**: [features/05-units-of-measure/STATUS.md](features/05-units-of-measure/STATUS.md)

### 06 - Security (Autenticación y Autorización)
- **Última validación**: 2026-02-20
- **Tests**: en el módulo (1318 globales)
- **Endpoints**: 13 (`/api/v1/auth`, `/api/v1/users`, `/api/v1/roles`, `/api/v1/permissions`, `/api/v1/audit-logs`)
- **JWT**: JJWT 0.12.5, access token 30min, refresh token 7 días con rotation
- **Permisos**: roles desde JWT, permisos cargados frescos desde BD en cada request (`UserPermissionsUseCase`)
- **Seed data**: admin user + ADMIN/USER roles via Flyway
- **Detalle**: [wip/06-security/STATUS.md](wip/06-security/STATUS.md)

### 08 - Warehouses (Bodegas)
- **Última validación**: 2026-02-20
- **Tests**: 95 en el módulo (1326 globales)
- **Endpoints**: 7 (`/api/v1/warehouses`)
- **Seed data**: BOD-001 (PRINCIPAL) + BOD-002 (SUCURSAL) vía V14
- **Migraciones**: V13 (schema) + V14 (seed) + docker/mysql-init sincronizado
- **Detalle**: [features/08-warehouses/STATUS.md](features/08-warehouses/STATUS.md)

---

## 🟡 Features En Progreso

### 01 - Document Types (Tipos de Documento)
- **Estado**: Implementación completa, cierre pendiente
- **Progress**: 33/38 tareas (87%)
- **Tareas pendientes**:
  - T030, T031, T032 (Phase 8: Documentación/Swagger) — no bloqueadas
  - T033–T035, T038 (Phase 9: Security integration) — ya desbloqueadas
- **Endpoints**: 7 (`/api/v1/document-types`)
- **Seed data**: tipos de documento colombianos
- **Detalle**: [wip/01-document-types/STATUS.md](wip/01-document-types/STATUS.md)

---

## ⏳ Features Pendientes

### 07 - Inventory (Inventario)
- **Prioridad**: 🔴 Alta
- **Progress**: 0/128 tareas (0%)
- **Dependencias**: `06-security`, `08-warehouses`, `05-units-of-measure`, `04-tax-types`, `02-geography`
- **Descripción**: Productos, categorías, bodegas, stock
- **Detalle**: [wip/07-inventory/STATUS.md](wip/07-inventory/STATUS.md)

---

## 📐 Grafo de Dependencias

```
Catálogos base (independientes):
├─ 01-document-types  🟡 87%
├─ 02-geography       ✅ 100%
├─ 03-payment-methods ✅ 100%
├─ 04-tax-types       ✅ 100%
└─ 05-units-of-measure ✅ 100%

Nivel 1:
├─ 06-security   → depende de [01-document-types]           ✅ 100%
└─ 08-warehouses → depende de [02-geography]                ✅ 100%

Nivel 2+:
└─ 07-inventory → depende de [06-security, 08-warehouses, 02-geography, 04-tax-types, 05-units-of-measure]  ⏳ 0%
```

---

## 🎬 Próximos Pasos Recomendados

1. **Completar cierre de `01-document-types`** (T033-T035, T038: security integration) — Ya desbloqueado
2. **Implementar `07-inventory`** — Todos los prerequisitos completados
