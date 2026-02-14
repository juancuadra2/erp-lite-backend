# Functional Specification: Módulo de Unidades de Medida

**Feature**: 05-units-of-measure  
**Version**: 1.1  
**Created**: 2026-02-01  
**Last Updated**: 2026-02-13  
**Status**: ⚠️ PHASE 1 - Draft Refinement (v1.1)

---

## 🎯 Overview

### Purpose

Este módulo proporciona un **catálogo de unidades de medida** utilizado para la gestión de productos, inventarios, ventas y compras. Permite estandarizar la cuantificación de productos (unidades, cajas, kilogramos, litros, metros, etc.) y facilita la configuración de conversiones entre unidades.

### Why This Feature Exists

**Business Driver**: Necesidad de manejar productos en diferentes unidades de medida según el contexto comercial (comprar por cajas, vender por unidades, medir en litros, etc.).

**Problem**: Sin un catálogo centralizado, las unidades de medida se duplican, no hay estándares y las conversiones son inconsistentes.

**Solution**: Catálogo base independiente con unidades predefinidas y extensibles para Colombia.

### Scope

**✅ In Scope:**
- CRUD completo de unidades de medida
- Abreviaturas estándar internacionales (UN, KG, L, M)
- Soft delete para preservar historial
- Búsqueda por nombre/abreviatura
- Validación de duplicados
- Configuración activa/inactiva
- 15 unidades precargadas para Colombia

**❌ Out of Scope:**
- Conversiones automáticas entre unidades (se maneja en ProductUnitConversion)
- Categorización funcional configurable (la clasificación en seed data es solo informativa)
- Equivalencias internacionales (Imperial ↔ Métrico)
- Unidades compuestas (kg/m², m³)
- Multiidioma

---

## ✅ Definición Complementaria (Iteración v1.1)

### DC-01: Convención de API
- **Base path canónico**: `/api/v1/units-of-measure`
- Los escenarios y contratos de esta spec se interpretan sobre `v1`.

### DC-02: Identificador Externo
- El identificador expuesto por API es `uuid`.
- En mensajes y escenarios previos donde aparece `id`, se interpreta como `uuid`.

### DC-03: Estado de Registro
- El estado funcional de la unidad se maneja con bandera booleana (`enabled`/`active`).
- Para mantener trazabilidad con otros módulos, el estado representa **unidad disponible para uso** y soporta soft delete.

### DC-04: Soft Delete
- DELETE desactiva lógicamente el registro (soft delete).
- Una unidad desactivada no debe aparecer en listados por defecto.
- Se mantiene endpoint de activación para reversión controlada.

### DC-05: Reglas de Búsqueda
- Búsqueda por `name` y `abbreviation` es **case-insensitive**.
- El resultado de búsqueda es por coincidencia parcial (`contains`).
- Si se envían ambos filtros, se prioriza `name` cuando exista valor no vacío.

---

## 👥 User Stories

### US-01: Gestión de Unidades de Medida (Prioridad: P0 - Crítica)

**Como** administrador del sistema  
**Quiero** gestionar el catálogo de unidades de medida  
**Para** estandarizar la cuantificación de productos en el ERP

**Why this priority?**: Módulo fundacional requerido antes de Products, Inventory, Sales y Purchases.

**Independent Test**: Crear unidad "Caja" con abreviatura "CJ", luego intentar crearla nuevamente y verificar error de duplicado.

#### Acceptance Scenarios

##### 1. Crear nueva unidad de medida

**Given** estoy autenticado como Admin  
**When** envío POST `/api/units-of-measure`
```json
{
  "name": "Caja",
  "abbreviation": "CJ"
}
```
**Then** sistema responde HTTP 201 Created  
**And** retorna UUID único  
**And** campos `active=true`, `createdAt`, `createdBy` son auto-generados

##### 2. Validar duplicado por nombre

**Given** existe unidad "Kilogramo"  
**When** intento crear otra con name="kilogramo" (case-insensitive)  
**Then** sistema responde HTTP 409 Conflict  
**And** mensaje: "Ya existe una unidad de medida con el nombre 'Kilogramo'"

##### 3. Validar duplicado por abreviatura

**Given** existe unidad con abbreviation="KG"  
**When** intento crear otra con abbreviation="kg"  
**Then** sistema responde HTTP 409 Conflict  
**And** mensaje: "Ya existe una unidad de medida con la abreviatura 'KG'"

##### 4. Obtener unidad por UUID

**Given** existe unidad con id=`550e8400-e29b-41d4-a716-446655440000`  
**When** envío GET `/api/units-of-measure/550e8400-e29b-41d4-a716-446655440000`  
**Then** sistema responde HTTP 200 OK  
**And** retorna datos completos (name, abbreviation, active, audit fields)

##### 5. Actualizar unidad de medida

**Given** existe unidad "Metro" con abreviatura "M"  
**When** envío PUT `/api/units-of-measure/{id}`
```json
{
  "name": "Metro Lineal",
  "abbreviation": "ML"
}
```
**Then** sistema responde HTTP 200 OK  
**And** actualiza campos modificables  
**And** `updatedAt` y `updatedBy` se actualizan

##### 6. Soft delete de unidad no utilizada

**Given** existe unidad "Pulgada" sin productos asociados  
**When** envío DELETE `/api/units-of-measure/{id}`  
**Then** sistema responde HTTP 204 No Content  
**And** campo `active=false`  
**And** unidad no aparece en listados por defecto

##### 7. Intentar eliminar unidad en uso

**Given** unidad "Kilogramo" está asociada a 50 productos  
**When** envío DELETE `/api/units-of-measure/{id}`  
**Then** sistema responde HTTP 409 Conflict  
**And** mensaje: "No se puede desactivar esta unidad porque está en uso por 50 productos"

##### 8. Listar unidades activas

**Given** existen 15 unidades, 12 activas y 3 inactivas  
**When** envío GET `/api/units-of-measure?enabled=true`  
**Then** sistema responde HTTP 200 OK  
**And** retorna solo las 12 unidades activas

##### 9. Buscar unidades por nombre

**Given** existen unidades "Kilogramo", "Gramo", "Tonelada"  
**When** envío GET `/api/units-of-measure/search?name=gram`  
**Then** sistema responde HTTP 200 OK  
**And** retorna ["Kilogramo", "Gramo"]

##### 10. Buscar unidades por abreviatura

**Given** existen unidades con abreviaturas "KG", "GR", "TON"  
**When** envío GET `/api/units-of-measure/search?abbreviation=G`  
**Then** sistema responde HTTP 200 OK  
**And** retorna ["KG", "GR"]

---

## 📊 Business Rules

### BR-01: Unicidad
- **Nombre** debe ser único (case-insensitive)
- **Abreviatura** debe ser única (case-insensitive)
- Validación al crear y actualizar

### BR-02: Validaciones de Formato
- **Nombre**: 2-50 caracteres, solo letras y espacios
- **Abreviatura**: 1-10 caracteres, letras/números y superíndices `²` `³`
- No se permiten otros caracteres especiales

### BR-03: Soft Delete
- DELETE solo marca `active=false`
- Unidades inactivas no aparecen en listados por defecto
- Historial de productos se preserva

### BR-04: Restricción de Eliminación
- No se puede desactivar unidad en uso por productos activos
- Sistema valida existencia de productos antes de desactivar

### BR-05: Auditoría
- Todos los cambios registran: `createdBy`, `updatedBy`, `createdAt`, `updatedAt`
- Formato de fechas: ISO 8601 (UTC)

---

## 🎨 API Contracts

### Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/units-of-measure` | Crear unidad | Admin |
| GET | `/api/units-of-measure/{id}` | Obtener por UUID | User |
| PUT | `/api/units-of-measure/{id}` | Actualizar unidad | Admin |
| DELETE | `/api/units-of-measure/{id}` | Soft delete | Admin |
| GET | `/api/units-of-measure` | Listar con filtros | User |
| GET | `/api/units-of-measure/search` | Buscar por nombre/abreviatura | User |

### DTOs

#### CreateUnitOfMeasureRequest
```json
{
  "name": "string (2-50 chars, required)",
  "abbreviation": "string (1-10 chars, required)"
}
```

#### UpdateUnitOfMeasureRequest
```json
{
  "name": "string (2-50 chars, required)",
  "abbreviation": "string (1-10 chars, required)"
}
```

#### UnitOfMeasureResponse
```json
{
  "id": "uuid",
  "name": "string",
  "abbreviation": "string",
  "active": "boolean",
  "createdAt": "ISO 8601 timestamp",
  "createdBy": "uuid",
  "updatedAt": "ISO 8601 timestamp",
  "updatedBy": "uuid"
}
```

---

## 📋 Data Catalog

### Unidades Precargadas para Colombia

> Nota: La columna **Category** es referencial/documental para legibilidad del catálogo inicial; no implica una capacidad funcional de categorización configurable en esta versión.

| ID | Name | Abbreviation | Category |
|----|------|--------------|----------|
| 1 | Unidad | UN | Cantidad |
| 2 | Caja | CJ | Empaque |
| 3 | Paquete | PQ | Empaque |
| 4 | Bulto | BL | Empaque |
| 5 | Kilogramo | KG | Peso |
| 6 | Gramo | GR | Peso |
| 7 | Tonelada | TON | Peso |
| 8 | Litro | L | Volumen |
| 9 | Mililitro | ML | Volumen |
| 10 | Galón | GAL | Volumen |
| 11 | Metro | M | Longitud |
| 12 | Centímetro | CM | Longitud |
| 13 | Metro Cuadrado | M² | Área |
| 14 | Docena | DOC | Cantidad |
| 15 | Par | PAR | Cantidad |

---

## 📈 Success Metrics

### Performance Targets

| Operation | Target | Measurement |
|-----------|--------|-------------|
| Create | < 100ms p95 | APM logs |
| Get by UUID | < 50ms p95 | APM logs |
| List (20 items) | < 100ms p95 | APM logs |
| Search | < 150ms p95 | APM logs |

### Quality Targets

- **Test Coverage**: >= 85%
- **API Availability**: 99.9%
- **Data Integrity**: 100% (no duplicados)

---

## 🚫 Out of Scope

Los siguientes elementos **NO** están incluidos en esta versión:

❌ Conversiones automáticas (delegado a ProductUnitConversion)  
❌ Gestión de categorías como entidad funcional configurable  
❌ Equivalencias internacionales (kg ↔ lb)  
❌ Unidades compuestas (kg/m², L/min)  
❌ Multiidioma (solo español)  
❌ Unidades obsoletas (onzas, quintales)  
❌ API pública sin autenticación  

---

## 🔗 Dependencies

### Upstream (Bloqueantes)
- ✅ Security Module (autenticación y autorización)

### Downstream (Dependientes)
- Products Module (usa `baseUnitId`)
- Inventory Module (usa unidades en Stock)
- Sales Module (usa unidades en líneas de venta)
- Purchases Module (usa unidades en líneas de compra)
- ProductUnitConversion Module (usa para conversiones)

---

## 📝 Notes

- Esta especificación cubre **solo la parte funcional** del módulo
- Ver [2-technical-spec.md](2-technical-spec.md) para detalles técnicos de arquitectura, base de datos, y diseño
- Este es un **catálogo base independiente** que debe implementarse antes de módulos que lo requieran
- Las conversiones entre unidades se manejan en **ProductUnitConversion** (asociadas a productos específicos)

---

**Status**: ⚠️ PHASE 1 - Draft Refinement (v1.1)  
**Next Step**: Functional Review (PO) → Clarify → Approve → Move to PHASE 2
