# Functional Specification: Módulo de Tipos de Impuestos

**Feature**: Tax Types Module  
**Created**: February 1, 2026  
**Module Type**: Independent Catalog (Sin dependencias)  
**Phase**: PHASE 1 - Draft

---

## 📋 Overview

El módulo de Tipos de Impuestos gestiona el catálogo de tipos de impuestos aplicables en transacciones comerciales (IVA, ReteFuente, ReteIVA, ICA, etc.). Es un catálogo base independiente sin dependencias de otros módulos, requerido por múltiples módulos del sistema (Products, Sales, Purchases) para el cálculo correcto de impuestos y cumplimiento tributario.

### Business Value
- Centraliza configuración de impuestos en un único catálogo
- Garantiza cálculo consistente de impuestos en todo el sistema
- Facilita cumplimiento de normativa tributaria colombiana
- Permite generación automática de reportes fiscales
- Simplifica adaptación a cambios en tasas impositivas

### Scope
Este módulo gestiona únicamente el catálogo de tipos de impuestos y sus configuraciones básicas. **No incluye** el cálculo de impuestos en transacciones (responsabilidad de módulos Sales/Purchases) ni la generación de reportes tributarios complejos.

---

## 👥 User Stories

### User Story 1: Gestión de Tipos de Impuestos (Priority: P1)

**Como** administrador del sistema  
**Quiero** gestionar el catálogo de tipos de impuestos (IVA, ReteFuente, etc.)  
**Para** poder aplicarlos correctamente en productos, ventas y compras

**Why this priority?** Es fundamental para el cumplimiento tributario y el cálculo correcto de impuestos en todas las transacciones comerciales.

**Acceptance Criteria:**

1. ✅ Puedo crear tipos de impuestos con código único, nombre y porcentaje
2. ✅ Puedo configurar si el impuesto es incluido o adicional al precio
3. ✅ Puedo especificar el tipo de aplicación (ventas, compras o ambos)
4. ✅ Puedo listar tipos de impuestos activos con paginación
5. ✅ Puedo buscar tipos de impuestos por nombre o código
6. ✅ Puedo actualizar información de tipos de impuestos existentes
7. ✅ Puedo desactivar/activar tipos de impuestos (soft delete)
8. ✅ El sistema valida códigos únicos
9. ✅ El sistema previene eliminación si hay productos/transacciones asociadas
10. ✅ Todas las operaciones quedan registradas en auditoría

**Acceptance Scenarios:**

#### Scenario 1.1: Crear tipo de impuesto IVA 19%
- **Given** estoy autenticado como administrador
- **When** envío POST /api/tax-types con:
  ```json
  {
    "code": "IVA19",
    "name": "IVA 19%",
    "percentage": 19.0,
    "isIncluded": false,
    "applicationType": "BOTH",
    "description": "Impuesto sobre el valor agregado tarifa general",
    "enabled": true
  }
  ```
- **Then** recibo status 201 con el tipo de impuesto creado
- **And** el tipo de impuesto tiene UUID asignado
- **And** se registra en AuditLog: action=TAX_TYPE_CREATED

#### Scenario 1.2: Validación de código único
- **Given** existe tipo de impuesto con code "IVA19"
- **When** intento crear otro tipo de impuesto con code "IVA19"
- **Then** recibo status 409 con mensaje "Tax type code already exists"

#### Scenario 1.3: Crear ReteFuente con porcentaje decimal
- **Given** estoy autenticado como administrador
- **When** envío POST /api/tax-types con:
  ```json
  {
    "code": "RETE2.5",
    "name": "ReteFuente 2.5%",
    "percentage": 2.5,
    "isIncluded": false,
    "applicationType": "PURCHASE",
    "description": "Retención en la fuente servicios",
    "enabled": true
  }
  ```
- **Then** recibo status 201 con el tipo de impuesto creado
- **And** percentage se almacena con precisión decimal (2.5000)

#### Scenario 1.4: Listar tipos de impuestos activos
- **Given** existen 15 tipos de impuestos, 12 activos y 3 inactivos
- **When** envío GET /api/tax-types?enabled=true&page=0&size=20
- **Then** recibo status 200 con 12 tipos de impuestos activos
- **And** metadata de paginación correcta (total=12, pages=1, currentPage=0)
- **And** resultados ordenados alfabéticamente por código

#### Scenario 1.5: Búsqueda de tipo de impuesto por nombre
- **Given** existen múltiples tipos de impuestos
- **When** envío GET /api/tax-types/search?name=IVA
- **Then** recibo status 200 con tipos de impuestos cuyo nombre contenga "IVA"
- **And** búsqueda es case-insensitive
- **And** resultados incluyen: "IVA 19%", "IVA 5%", "IVA 0%"

#### Scenario 1.6: Actualizar porcentaje de tipo de impuesto
- **Given** existe tipo de impuesto IVA con percentage=19.0
- **When** envío PUT /api/tax-types/{uuid} con:
  ```json
  {
    "code": "IVA19",
    "name": "IVA 19%",
    "percentage": 21.0,
    "isIncluded": false,
    "applicationType": "BOTH",
    "enabled": true
  }
  ```
- **Then** recibo status 200 con el tipo de impuesto actualizado
- **And** percentage es ahora 21.0
- **And** se registra en AuditLog: action=TAX_TYPE_UPDATED, oldValue={"percentage": 19.0}, newValue={"percentage": 21.0}

#### Scenario 1.7: Desactivar tipo de impuesto sin uso
- **Given** tipo de impuesto "IVA5" activo sin productos asociados
- **When** envío PATCH /api/tax-types/{uuid}/deactivate
- **Then** recibo status 200 con enabled=false
- **And** el tipo de impuesto no aparece en búsquedas con filtro enabled=true
- **And** se registra en AuditLog: action=TAX_TYPE_DEACTIVATED

#### Scenario 1.8: Validación de eliminación con productos asociados
- **Given** tipo de impuesto "IVA19" tiene 500 productos asociados
- **When** intento DELETE /api/tax-types/{uuid}
- **Then** recibo status 409 con mensaje "Cannot delete tax type with associated products"
- **And** el tipo de impuesto permanece activo

#### Scenario 1.9: Obtener tipo de impuesto por UUID
- **Given** existe tipo de impuesto con UUID "550e8400-e29b-41d4-a716-446655440000"
- **When** envío GET /api/tax-types/550e8400-e29b-41d4-a716-446655440000
- **Then** recibo status 200 con los detalles completos del tipo de impuesto
- **And** response incluye todas las propiedades: code, name, percentage, isIncluded, applicationType, description, enabled, createdAt, updatedAt

#### Scenario 1.10: Validación de porcentaje válido
- **Given** estoy creando un tipo de impuesto
- **When** envío POST /api/tax-types con percentage=-5.0
- **Then** recibo status 400 con mensaje "Percentage must be between 0 and 100"

---

### User Story 2: Consulta de Tipos de Impuestos por Aplicación (Priority: P2)

**Como** desarrollador del módulo de productos  
**Quiero** consultar tipos de impuestos filtrados por aplicación (ventas, compras)  
**Para** mostrar solo los impuestos relevantes en cada contexto

**Why this priority?** Mejora la experiencia de usuario al configurar productos, mostrando solo impuestos aplicables.

**Acceptance Criteria:**

1. ✅ Puedo filtrar tipos de impuestos por applicationType
2. ✅ El filtro acepta valores: SALE, PURCHASE, BOTH
3. ✅ Los tipos con applicationType=BOTH aparecen en ambos filtros
4. ✅ Los resultados están ordenados alfabéticamente

**Acceptance Scenarios:**

#### Scenario 2.1: Filtrar impuestos para ventas
- **Given** existen los siguientes tipos de impuestos:
  - IVA19 (applicationType=BOTH)
  - IVA5 (applicationType=BOTH)
  - ReteFuente2.5 (applicationType=PURCHASE)
  - ICA (applicationType=SALE)
- **When** envío GET /api/tax-types?applicationType=SALE&enabled=true
- **Then** recibo status 200 con 3 tipos de impuestos:
  - IVA19
  - IVA5
  - ICA
- **And** NO incluye ReteFuente2.5

#### Scenario 2.2: Filtrar impuestos para compras
- **Given** mismos tipos de impuestos del scenario anterior
- **When** envío GET /api/tax-types?applicationType=PURCHASE&enabled=true
- **Then** recibo status 200 con 3 tipos de impuestos:
  - IVA19
  - IVA5
  - ReteFuente2.5
- **And** NO incluye ICA

---

### User Story 3: Seed Data de Colombia (Priority: P1)

**Como** implementador del sistema  
**Quiero** que el sistema incluya datos iniciales de impuestos de Colombia  
**Para** facilitar el despliegue y reducir configuración inicial

**Why this priority?** Reduce tiempo de configuración inicial y errores en setup.

**Acceptance Criteria:**

1. ✅ El sistema incluye migración con datos iniciales de Colombia
2. ✅ Los datos iniciales son idempotentes (re-ejecutables)
3. ✅ Los datos iniciales incluyen al menos:
   - IVA 19% (tarifa general)
   - IVA 5% (tarifa reducida)
   - IVA 0% (bienes exentos)
   - ReteFuente servicios (2.5%)
   - ReteFuente honorarios (10%)
   - ReteIVA (15%)
   - ICA (varía por municipio, configuración base)

**Acceptance Scenarios:**

#### Scenario 3.1: Primera instalación con seed data
- **Given** la base de datos está vacía
- **When** se ejecutan las migraciones Flyway
- **Then** la tabla tax_types contiene 7 registros de impuestos colombianos
- **And** todos los impuestos están activos (enabled=true)
- **And** los códigos son únicos
- **And** los porcentajes son correctos según normativa colombiana 2026

#### Scenario 3.2: Re-ejecución de migración es idempotente
- **Given** ya existen los 7 tipos de impuestos iniciales
- **When** se re-ejecuta la migración de seed data
- **Then** no se crean registros duplicados
- **And** los registros existentes no se modifican

---

## 🚫 Out of Scope

Lo siguiente NO está incluido en este módulo:

1. **Cálculo de impuestos en transacciones**: Responsabilidad de módulos Sales/Purchases
2. **Reportes tributarios complejos**: Responsabilidad de módulo Reports
3. **Impuestos compuestos o combinados**: Solo manejo de impuestos simples
4. **Configuración de impuestos por región/ciudad**: Todos los impuestos son a nivel nacional
5. **Gestión de periodos fiscales**: Fuera de alcance
6. **Declaraciones tributarias**: Fuera de alcance
7. **Integración con DIAN**: Fuera de alcance de este módulo

---

## 📊 Business Rules

### BR-TT-001: Código Único
- Cada tipo de impuesto debe tener un código único en el sistema
- El código es case-sensitive
- Formato recomendado: Letras mayúsculas + números (ej: IVA19, RETE2.5)
- Longitud máxima: 20 caracteres

### BR-TT-002: Porcentaje Válido
- El porcentaje debe estar entre 0.0000 y 100.0000
- Se permite precisión de 4 decimales
- Porcentaje 0 es válido (bienes exentos)

### BR-TT-003: Tipo de Aplicación
- Valores permitidos: SALE, PURCHASE, BOTH
- Un tipo de impuesto con BOTH aparece en contextos de ventas y compras
- El tipo de aplicación no puede ser NULL

### BR-TT-004: Soft Delete
- Los tipos de impuestos se desactivan (enabled=false), no se eliminan físicamente
- Un tipo de impuesto desactivado no aparece en listados de activos
- Un tipo de impuesto desactivado no puede ser asignado a nuevos productos
- Los productos con tipos de impuestos desactivados mantienen el histórico

### BR-TT-005: Validación de Eliminación
- No se puede eliminar (ni desactivar) un tipo de impuesto si:
  - Tiene productos asociados activos
  - Tiene transacciones históricas (ventas/compras) que lo referencian
- Se debe mostrar mensaje de error descriptivo indicando la restricción

### BR-TT-006: Impuesto Incluido vs Adicional
- `isIncluded=true`: El impuesto está incluido en el precio mostrado (ej: IVA en retail)
- `isIncluded=false`: El impuesto se suma al precio base (ej: ReteFuente se resta)
- Esta configuración afecta cómo se calcula el subtotal en transacciones

### BR-TT-007: Auditoría Obligatoria
- Todas las operaciones (CREATE, UPDATE, DEACTIVATE) deben registrarse en auditoría
- Los campos de auditoría son obligatorios: createdBy, createdAt, updatedBy, updatedAt
- Para desactivaciones se registra también: deletedBy, deletedAt

---

## 🎯 Success Criteria

Este módulo se considera exitoso cuando:

1. ✅ Se pueden gestionar tipos de impuestos con CRUD completo
2. ✅ El sistema incluye datos iniciales de Colombia (7 impuestos)
3. ✅ Los módulos de Products, Sales y Purchases pueden consultar y usar los tipos de impuestos
4. ✅ Se previene la eliminación de impuestos con referencias
5. ✅ Todas las operaciones tienen auditoría completa
6. ✅ La cobertura de tests es >= 85%
7. ✅ La documentación API está completa y actualizada
8. ✅ Los tiempos de respuesta cumplen con SLA: < 100ms p95

---

## 📚 References

- Normativa tributaria colombiana 2026
- Estatuto Tributario - Impuesto al Valor Agregado (IVA)
- Retención en la Fuente - Decreto 1625/2016
- DIAN - Dirección de Impuestos y Aduanas Nacionales

---

## 📝 Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-01 | Development Team | Initial version |
