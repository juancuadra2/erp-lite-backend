# ERP Lite Backend - Estado General

**Última actualización**: 2026-02-13  
**Sprint actual**: Sprint 6 (2026-02-05 → 2026-02-18)

---

## 📊 Dashboard General

### Resumen de Features
- **Total features planeados**: 10
- **Completados**: 0 (0%) - *Revertidos por ajuste arquitectónico*
- **En preparación**: 1 (10%) - *tax-types (especificación completa)*
- **En desarrollo activo**: 1 (10%) - *document-types (architecture redesign)*
- **Pendientes**: 8 (80%)

### Progreso General del Proyecto
```
█░░░░░░░░░░░░░░░░░░░░░░░░ 5% completado
```

### Métricas Clave
- **Test Coverage Global**: 0% (proyecto reiniciado)
- **Features en producción**: 0
- **Blockers activos**: 0
- **Technical Debt**: Ninguno (nueva arquitectura)

---

## 🔄 Cambios Arquitectónicos Recientes

### 2026-02-06: Ajuste a Hexagonal Architecture
**Decisión**: Migrar todos los módulos al nuevo scaffolding hexagonal

**Impacto**:
- ✅ `document-types` ajustado a nueva arquitectura
- ✅ Documentación completa actualizada
- ⚠️ Código implementado anterior debe ser revisado/refactorizado
- 📝 Features movidos de `features/` a `wip/` para ajuste

**Beneficios**:
- Mayor aislamiento del dominio
- Mejor testabilidad
- Independencia de frameworks
- Consistencia con scaffolding estándar

---

## 🟡 Features En Desarrollo (WIP)

### document-types (Tipos de Documento) - v2.0.0
- **Estado**: 🔴 Planning & Documentation (Architecture Redesign)
- **Developer**: Development Team
- **Progress**: 0/32 tareas (0%)
- **Architecture**: Hexagonal (Aligned with Scaffolding)
- **ETA**: 2026-03-06 (4-5 semanas)
- **Priority**: 🔴 High
- **Documentación**: [wip/document-types/](wip/document-types/)
- **Tracking**: [wip/document-types/STATUS.md](wip/document-types/status.md)
- **Cambios v2.0**:
  - Repository interface en `domain/port/out/`
  - Use cases CQRS: Compare (Query) y Manage (Command)
  - Infrastructure separado en `in/` y `out/`
  - BeanConfiguration para DI manual
  - Validators y Utils según scaffolding

### geography (Departamentos y Municipios) - ❌ ELIMINADO
- **Estado**: ❌ ELIMINADO (2026-02-06)
- **Razón**: Implementación removida del proyecto
- **Progress**: N/A (módulo eliminado)
- **Nota**: La implementación completa fue eliminada. Solo se conserva documentación de especificaciones para referencia.
- **Documentación**: [wip/geography/](wip/geography/)
- **README**: [wip/geography/README.md](wip/geography/README.md) - Detalles de eliminación

### tax-types (Tipos de Impuestos) - v1.0.0
- **Estado**: ✅ Implementado y validado
- **Developer**: AI Assistant
- **Progress**: 32/32 tareas (100%)
- **Architecture**: Hexagonal (Aligned with Scaffolding)
- **ETA**: Completado 2026-02-13
- **Priority**: 🔴 High
- **Documentación**: [wip/04-tax-types/](wip/04-tax-types/)
- **Specs**:
  - ✅ 1-functional-spec.md (Versión 1.1 - Completada)
  - ✅ 2-technical-spec.md (Versión 1.0 - Completada)
  - ✅ 3-tasks.json (32 tareas en 8 fases - 45 story points)
- **Features Clave**:
  - Catálogo de tipos de impuestos (IVA, ReteFuente, ICA)
  - Campo percentage (BigDecimal 4 decimales, 0-100%)
  - Campo isIncluded (incluido en precio o aparte)
  - Enum TaxApplicationType (SALE/PURCHASE/BOTH)
  - 10 tipos de impuestos colombianos en seed data
  - CQRS: Compare (Query) y Manage (Command)
  - 7 endpoints REST API in /api/v1/tax-types (consolidados)
  - Validaciones exhaustivas (código, porcentaje, nombre)
- **Fase Actual**: PHASE 8 completada
- **Resultado**: 157 tests OK, cobertura dominio 100%, application ~99%, infrastructure ~98%

---

## 📋 Features Pendientes (Backlog)

### Orden de Implementación Recomendado

#### Fase 1: Catálogos Base (Sin Dependencias)

**03-payment-methods** (Métodos de Pago)
- **Prioridad**: 🟡 Media
- **Descripción**: Catálogo de métodos de pago
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Estado**: Sin especificación detallada
- **Documentación**: [payment-methods/](payment-methods/)

**05-units-of-measure** (Unidades de Medida)
- **Prioridad**: 🟡 Media
- **Descripción**: Catálogo de unidades de medida
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Estado**: Sin especificación detallada
- **Documentación**: [units-of-measure/](units-of-measure/)

#### Fase 2: Seguridad y Estructura (P1 - Crítica)

**06-security** (Autenticación y Autorización)
- **Prioridad**: 🔴 Crítica
- **Descripción**: Módulo de usuarios, roles, permisos y autenticación JWT
- **Dependencias**: Document Types (01)
- **Estimación**: 4 semanas
- **Estado**: Planeado (spec completo)
- **Documentación**: [security/](security/)
- **⚠️ BLOQUEADOR**: Sin este módulo no hay control de acceso

**07-product-categories** (Categorías de Productos)
- **Prioridad**: 🟡 Media
- **Descripción**: Catálogo de categorías de productos con jerarquía
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Estado**: Incluido en inventory-spec.md

#### Fase 3: Infraestructura de Inventario (P1)

**08-warehouses** (Bodegas)
- **Prioridad**: 🔴 Alta
- **Descripción**: Gestión de bodegas con restricciones de acceso
- **Dependencias**: Security (06), Geography (02)
- **Estimación**: 2 semanas
- **Estado**: Incluido en inventory-spec.md

**09-products** (Productos)
- **Prioridad**: 🔴 Alta
- **Descripción**: Gestión completa de productos con múltiples códigos de barras
- **Dependencias**: Product Categories (07), Units of Measure (05), Tax Types (03)
- **Estimación**: 3 semanas
- **Estado**: Planeado (spec completo)
- **Documentación**: [inventory/](inventory/)

#### Fase 4: Gestión de Inventario (P1)

**10-stock-management** (Gestión de Stock)
- **Prioridad**: 🔴 Alta
- **Descripción**: Control de inventario por bodega
- **Dependencias**: Products (09), Warehouses (08)
- **Estimación**: 3 semanas
- **Estado**: Incluido en inventory-spec.md

---

## 🔴 Blockers Globales

_No hay blockers globales actualmente_

**Recomendación**: Iniciar con Geography (02) o Security (06) según prioridad de negocio.

---

## 📅 Timeline del Sprint

- **Sprint 5**: 2026-01-29 → 2026-02-11
- **Objetivo**: Migración al framework SDD completada
- **Estado**: ✅ Completado

---

## 📝 Decisiones Recientes

### 2026-02-13: Tax Types Specification Completed
- ✅ **Especificación funcional completa** (1-functional-spec.md v1.1)
  - 3 User Stories con 14+ acceptance scenarios
  - Tabla detallada de seed data con 10 impuestos colombianos
  - Business rules BR-TT-001 a BR-TT-007
  - Validaciones exhaustivas (código, porcentaje, nombre)
- ✅ **Especificación técnica completa** (2-technical-spec.md v1.0)
  - Arquitectura hexagonal completa (~1200 líneas)
  - Domain: TaxType aggregate, TaxApplicationType enum, services, exceptions
  - Application: CQRS use cases (Compare/Manage)
  - Infrastructure: REST API (7 endpoints), DTOs, JPA entities, mappers
  - Database: Flyway V7 (schema), V8 (seed data), Docker scripts 07/08
- ✅ **Plan de tareas detallado** (3-tasks.json)
  - 32 tareas organizadas en 8 fases
  - 68 horas estimadas
  - Riesgos y notas técnicas documentadas
- 📋 **Próximo paso**: Validación de specs y inicio de implementación (T000)

### 2026-02-06: Ajuste Arquitectónico Global
- 🔄 **Migración a Hexagonal Architecture** con scaffolding estandarizado
- 🔄 `document-types` movido de `features/` a `wip/` para rediseño (v2.0.0)
- ✅ Documentación completa actualizada según nuevo scaffolding
- ✅ 32 tareas nuevas definidas en 8 fases
- ✅ Separación CQRS en use cases (Compare/Manage)
- ✅ Repository interface movido a `domain/port/out/`
- ✅ Infrastructure separado en `in/` (web) y `out/` (persistence)
- ⚠️ Código anterior debe ser refactorizado para alinearse

### 2026-02-01
- ✅ Migración al framework SDD completada
- ✅ Document Types (01) movido a features/ con documentación completa (v1.0.0 - deprecado)
- ✅ Estructura features/ y wip/ creada
- ✅ Orden de implementación definido según análisis de dependencias
- ✅ **Geography (02)** movido a `wip/geography/` siguiendo framework
- ✅ Geography specs separadas en functional + technical (PHASE 1-2)
- ✅ Geography plan y tasks.json creados (PHASE 3-4)
- ✅ Geography STATUS.md y APPROVALS.md inicializados
- ⚠️ Geography esperando aprobaciones antes de iniciar PHASE 5

### 2026-01-15
- ✅ Document Types implementado y en producción (v1.0.0 - arquitectura anterior)
- ✅ 85%+ test coverage alcanzado
- ✅ 6 tipos de documento colombianos cargados
## 📊 Grafo de Dependencias

```
Nivel 0 (Catálogos Base Independientes):
├─ 01-document-types ⚠️ EN REDESIGN (v2.0.0)
├─ 02-geography ❌ ELIMINADO
├─ 04-tax-types 🟡 ESPECIFICACIÓN COMPLETA (0/32 tareas)
├─ 03-payment-methods ⚪ PENDIENTE
└─ 05-units-of-measure ⚪ PENDIENTE

Nivel 1 (Dependen de catálogos base):
├─ 06-security → depende de: [01-document-types]
└─ 07-product-categories → independiente

Nivel 2 (Dependen de Nivel 0 y 1):
└─ 08-warehouses → depende de: [06-security, 02-geography]

Nivel 3 (Dependen de múltiples módulos):
└─ 09-products → depende de: [07-product-categories, 05-units-of-measure, 03-tax-types]

Nivel 4+ (Módulos complejos):
└─ 10-stock-management → depende de: [09-products, 08-warehouses]
```

---

## 🎯 Próximos Pasos Recomendados

### Opción A: Implementar Tax Types (04)
**Ventajas**:
- ✅ Especificación 100% completa y lista
- ✅ 32 tareas detalladas con estimaciones
- ✅ Sin dependencias de otros módulos
- ✅ 1 semana de implementación
- ✅ Crítico para módulos de ventas y compras

### Opción B: Completar especificaciones de catálogos restantes
**Payment Methods (03)** y **Units of Measure (05)**:
- Seguir mismo patrón usado en tax-types
- Estimar 2-3 días por especificación
- Tener catálogos base documentados antes de Security (06)

### Opción C: Iniciar Security (06)
**Bloqueador crítico**:
- ⚠️ Sin este módulo no hay control de acceso
- ⚠️ 4 semanas de implementación
- ⚠️ Depende de Document Types (01) que está en redesign

**Recomendación**: Opción A (implementar tax-types) → tiene mayor ROI inmediato

---

## 📚 Documentación del Framework

- **Framework SDD**: [framework/proyecto-framework-sdd.md](framework/proyecto-framework-sdd.md)

---

## 💡 Workflow de Trabajo

### Para iniciar un nuevo feature:
1. Mover de raíz a `wip/feature-name/`
2. Crear `STATUS.md` con plantilla del framework
3. Actualizar diariamente el STATUS.md
4. Al completar: mover a `features/XX-feature-name/`
5. Crear `IMPLEMENTED.md`
6. Actualizar este STATUS.md global

#### 07-units-of-measure (Unidades de Medida)
- **Prioridad**: 🟢 Baja
- **Descripción**: Catálogo de unidades de medida
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Asignado**: Por asignar
- **Estado**: Planeado
- **Documentación**: [specs/units-of-measure/](units-of-measure/)

---

## 🔴 Blockers Globales

_No hay blockers globales actualmente_

---

## 📅 Roadmap 2026

### Q1 (Enero - Marzo)
- [x] ✅ **01-document-types** (Completado: 2026-01-15)
- [ ] 🎯 **02-geography** (Target: Febrero 2026)
- [ ] 🎯 **03-security** (Target: Marzo 2026)

### Q2 (Abril - Junio)
- [ ] **05-payment-methods**
- [ ] **06-tax-types**
- [ ] **07-units-of-measure**

### Q3 (Julio - Septiembre)
- [ ] **04-inventory** (depende de Security)
- [ ] Contacts Module

### Q4 (Octubre - Diciembre)
- [ ] Sales Module
- [ ] Reports Module

---

## 📈 Progreso por Sprint

### Sprint 5 (Actual: 2026-01-29 → 2026-02-11)
- **Objetivo**: Planificación de próximos módulos y mejora de documentación
- **Estado**: 🟢 On Track
- **Completado**: 
  - Creación de framework SDD
  - Actualización de PROJECT_INFO.md
  - Definición de estructura de documentación

### Sprint 4 (2026-01-15 → 2026-01-28)
- **Objetivo**: Completar módulo Document Types
- **Estado**: ✅ Completado
- **Logros**: 
  - 01-document-types implementado y tested
  - 85% test coverage alcanzado
  - Documentación completa

---

## 📝 Decisiones Recientes

### 2026-02-01
- ✅ Aprobado framework SDD con estructura simplificada
- ✅ Decidido separar specs funcional y técnico
- ✅ Agregado tasks.json para tracking estructurado
- ✅ Implementado STATUS.md general para visión global

### 2026-01-28
- ✅ Decidido uso de Testcontainers para tests de integración en todos los módulos
- ✅ Estandarización de estructura de packages en arquitectura hexagonal

### 2026-01-15
- ✅ Aprobado uso de MapStruct para todos los mappers
- ✅ Implementado soft delete por defecto en catálogos base
- ✅ UUID obligatorio para referencias externas

---

## 🎯 Objetivos del Trimestre (Q1 2026)

- [ ] Completar 3 features (Document Types ✅, Geography, Security)
- [ ] Mantener test coverage > 80% en todos los módulos
- [ ] Documentación completa de todos los features
- [ ] Technical debt bajo

---

## 📞 Puntos de Contacto

- **Tech Lead**: Por definir
- **Product Owner**: Por definir
- **Documentación**: [framework/proyecto-framework-sdd.md](framework/proyecto-framework-sdd.md)
- **Project Info**: [PROJECT_INFO.md](PROJECT_INFO.md)

---

**Próxima actualización**: 2026-02-08
