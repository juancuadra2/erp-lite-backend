# Functional Specification: Módulo de Gestión de Bodegas

**Feature**: 08-warehouses
**Version**: 1.1
**Created**: 2026-02-19
**Last Updated**: 2026-02-20
**Status**: ✅ Approved — Ready for Implementation

---

## 🎯 Overview

El módulo de **Gestión de Bodegas** permite administrar las ubicaciones físicas donde se almacena el inventario. Es un catálogo maestro que sirve como referencia para el módulo de inventario (stock, movimientos, transferencias).

### Business Context

- **Problem**: Sin un catálogo de bodegas, el sistema de inventario no puede asociar stock a ubicaciones físicas
- **Solution**: Módulo simple de CRUD con soporte para múltiples tipos de bodega y datos de ubicación
- **Value**: Desbloquea el módulo 07-inventory; permite gestión multi-bodega controlada

### Scope

**In Scope**:
- CRUD de bodegas con información completa
- Tipos de bodega: PRINCIPAL, SUCURSAL, CONSIGNACION, TEMPORAL
- Referencia geográfica (municipio)
- Activar / desactivar bodegas
- Eliminación lógica con validación de integridad referencial
- Filtros dinámicos y paginación

**Out of Scope**:
- Gestión de stock y movimientos (módulo 07-inventory)
- Layout interno de bodega (zonas, estantes, ubicaciones)
- Control de capacidad en tiempo real

---

## 👥 User Stories

### US-01: Gestión de Bodegas 🔴 P1

**Como** administrador del sistema
**Quiero** crear y mantener el catálogo de bodegas con información completa
**Para** que el módulo de inventario pueda asociar stock a ubicaciones físicas

#### Acceptance Scenarios

**✅ Scenario 1: Crear bodega principal**
```gherkin
Given estoy autenticado con permisos de administrador
When envío POST /api/v1/warehouses con:
  {
    "code": "BOD-001",
    "name": "Bodega Principal",
    "description": "Bodega central de operaciones",
    "type": "PRINCIPAL",
    "address": "Calle 10 # 20-30",
    "municipalityId": "550e8400-e29b-41d4-a716-446655440000",
    "responsible": "Juan Pérez",
    "email": "bodega@empresa.com",
    "phone": "3001234567"
  }
Then recibo status 201 con la bodega creada
And el campo uuid es generado automáticamente
And active = true
```

**✅ Scenario 2: Validación de código único**
```gherkin
Given existe una bodega con código "BOD-001"
When intento crear otra bodega con código "BOD-001"
Then recibo status 409 con mensaje "Ya existe una bodega con el código BOD-001"
```

**✅ Scenario 3: Listar bodegas con filtros**
```gherkin
Given existen 5 bodegas (3 activas, 2 inactivas)
When envío GET /api/v1/warehouses?active=true&type=SUCURSAL
Then recibo status 200 con las bodegas que cumplan los filtros
And la respuesta incluye paginación (page, size, totalElements)
```

**✅ Scenario 4: Actualizar información de bodega**
```gherkin
Given existe la bodega "BOD-001"
When envío PUT /api/v1/warehouses/{uuid} con dirección actualizada
Then recibo status 200 con los datos actualizados
And updatedAt se actualiza al momento actual
```

**✅ Scenario 5: Desactivar bodega**
```gherkin
Given bodega "BOD-001" está activa
When envío PATCH /api/v1/warehouses/{uuid}/deactivate
Then recibo status 200
And active = false
And la bodega sigue existiendo en el sistema
```

**✅ Scenario 6: Eliminación de la bodega PRINCIPAL activa**
```gherkin
Given bodega "BOD-001" es de tipo PRINCIPAL y está activa
When envío DELETE /api/v1/warehouses/{uuid}
Then recibo status 409 con mensaje "No se puede eliminar la bodega PRINCIPAL activa. Es el punto de venta activo del sistema"
```

**✅ Scenario 7: Validación de eliminación con stock activo**
```gherkin
Given bodega "BOD-002" tiene stock de productos asignado en el módulo de inventario
When intento DELETE /api/v1/warehouses/{uuid}
Then recibo status 409 con mensaje "No se puede eliminar una bodega con stock activo"
```

**✅ Scenario 8: Eliminación lógica de bodega sin stock**
```gherkin
Given bodega "BOD-003" no tiene stock activo y no es la PRINCIPAL activa
When envío DELETE /api/v1/warehouses/{uuid}
Then recibo status 204
And la bodega queda con deletedAt establecido (soft delete)
And no aparece en listados normales
```

---

## 📋 Business Rules

### BR-01: Unicidad
- **R01.1**: El código de bodega debe ser único en todo el sistema (case-insensitive)
- **R01.2**: El nombre de bodega debe ser único en todo el sistema (case-insensitive)

### BR-02: Tipos de Bodega
- **R02.1**: Los tipos válidos son: `PRINCIPAL`, `SUCURSAL`, `CONSIGNACION`, `TEMPORAL`
- **R02.2**: Solo puede existir una bodega de tipo `PRINCIPAL` activa a la vez. La bodega PRINCIPAL es la que usa el módulo POS para operaciones de venta. Intentar crear o activar una segunda bodega PRINCIPAL produce error 409 (`SinglePrincipalWarehouseException`). Las demás bodegas admiten todas las operaciones de inventario (ingresos, traslados, etc.) pero no operan como punto de venta activo.

### BR-03: Integridad Referencial
- **R03.1**: No se puede eliminar una bodega con stock activo en el módulo de inventario
- **R03.2**: No se puede desactivar una bodega con transferencias pendientes (estado PENDING o APPROVED)
- **R03.3**: No se puede eliminar la bodega PRINCIPAL activa. Lanza `WarehouseInUseException (409)` con mensaje "No se puede eliminar la bodega PRINCIPAL activa. Es el punto de venta activo del sistema". Para poder eliminarla, primero debe activarse otra bodega como PRINCIPAL o desactivarse.

### BR-04: Soft Delete
- **R04.1**: La eliminación es lógica (deletedAt != null)
- **R04.2**: Las bodegas eliminadas no aparecen en listados normales
- **R04.3**: El código de una bodega eliminada **no puede ser reutilizado**. La restricción de unicidad persiste aunque la bodega esté eliminada.

---

## 🔌 API Contracts

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/v1/warehouses` | POST | Crear bodega | ADMIN |
| `/api/v1/warehouses/{uuid}` | GET | Obtener bodega por UUID | Authenticated |
| `/api/v1/warehouses` | GET | Listar bodegas (paginado + filtros) | Authenticated |
| `/api/v1/warehouses/{uuid}` | PUT | Actualizar bodega | ADMIN |
| `/api/v1/warehouses/{uuid}` | DELETE | Eliminar bodega (soft) | ADMIN |
| `/api/v1/warehouses/{uuid}/activate` | PATCH | Activar bodega | ADMIN |
| `/api/v1/warehouses/{uuid}/deactivate` | PATCH | Desactivar bodega | ADMIN |

### Query Parameters para GET /api/v1/warehouses

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `active` | Boolean | Filtrar por estado activo |
| `type` | String | Filtrar por tipo: PRINCIPAL, SUCURSAL, CONSIGNACION, TEMPORAL |
| `municipalityId` | UUID | Filtrar por municipio (UUID público del municipio) |
| `name` | String | Búsqueda parcial por nombre |
| `code` | String | Búsqueda parcial por código |
| `page` | int | Número de página (0-based) |
| `size` | int | Tamaño de página (default 20) |
| `sort` | String | Campo y dirección (ej: name,asc) |

---

## 🌱 Seed Data

Crear las siguientes bodegas de ejemplo para desarrollo:

| Código | Nombre | Tipo | Municipio |
|--------|--------|------|-----------|
| BOD-001 | Bodega Principal | PRINCIPAL | Bogotá D.C. |
| BOD-002 | Sucursal Norte | SUCURSAL | Bogotá D.C. |

---

## ✅ Success Metrics

| Metric | Target |
|--------|--------|
| CRUD completo funcional | 100% |
| Validaciones de negocio | 100% |
| Cobertura de tests | >= 90% |
| Build sin errores | 100% |

---

**Status**: ✅ Approved — Ready for Implementation
**Next Step**: Implementation
