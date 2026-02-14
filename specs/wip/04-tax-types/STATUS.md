# Status: Módulo de Tipos de Impuestos (Tax Types)

**Última actualización**: 2026-02-13 00:00  
**Developer**: Por asignar  
**Estado general**: 🟡 Especificación Completa - Listo para Implementación  
**Versión**: 1.0.0

---

## 📊 Progreso General

- **Completado**: 0/32 tareas (0%)
- **En progreso**: 0 tareas
- **Bloqueado**: 0 tareas
- **Por hacer**: 32 tareas
- **Estimación total**: 45 story points

```
░░░░░░░░░░░░░░░░░░░░░░░░░ 0% completado
```

---

## 🎯 Estado Actual

### Especificación - ✅ COMPLETADA

**Documentos generados**:
- ✅ `1-functional-spec.md` (v1.1) - Especificación funcional completa
  - 3 User Stories con 14+ acceptance scenarios
  - Tabla detallada de 10 impuestos colombianos
  - Business rules BR-TT-001 a BR-TT-007
  - Validaciones de porcentaje y código
- ✅ `2-technical-spec.md` (v1.0) - Especificación técnica completa (~1200 líneas)
  - Arquitectura hexagonal completa
  - CQRS pattern (Compare/Manage use cases)
  - 7 endpoints REST API (consolidados)
  - Flyway migrations V7 (schema) + V8 (seed data)
- ✅ `3-tasks.json` - Plan de implementación con 32 tareas en 8 fases
- ✅ `STATUS.md` - Este documento

**Próximo paso**: T000 - Validación y aprobación de specs antes de implementación

---

## 📐 Características Técnicas

### Modelo de Dominio
- **Aggregate Root**: TaxType
- **Value Objects**: TaxApplicationType (enum)
- **Campos clave**:
  - `percentage`: BigDecimal (7,4) - rango 0-100%, max 4 decimales
  - `isIncluded`: Boolean - incluido en precio o calculado aparte
  - `applicationType`: Enum (SALE/PURCHASE/BOTH)
  - `code`: String (^[A-Z0-9._-]+$, max 20 chars)
  - `name`: String (max 100 chars)

### Seed Data
10 tipos de impuestos colombianos:
1. IVA19 (19%, incluido, ambos)
2. IVA5 (5%, incluido, ambos)
3. IVA0 (0%, incluido, ambos)
4. RETE_SERV_2.5 (2.5%, no incluido, compra)
5. RETE_SERV_4.0 (4%, no incluido, compra)
6. RETE_HON_10.0 (10%, no incluido, compra)
7. RETE_COMP_2.5 (2.5%, no incluido, compra)
8. RETEIVA_15 (15%, no incluido, compra)
9. ICA_BOG_SERV (0.966%, no incluido, ambos)
10. ICA_BOG_IND (0.414%, no incluido, ambos)

### API REST (7 endpoints)
- POST /api/v1/tax-types
- GET /api/v1/tax-types/{uuid}
- GET /api/v1/tax-types?enabled=&applicationType=&name=&page=&size= (consolidado: listado, búsqueda y filtros)
- PUT /api/v1/tax-types/{uuid}
- PATCH /api/v1/tax-types/{uuid}/activate
- PATCH /api/v1/tax-types/{uuid}/deactivate
- DELETE /api/v1/tax-types/{uuid}

---

## 📋 Resumen por Fase

### Phase 0: Pre-Implementation Validation (1 tarea - 1 SP)
- ⏳ T000: Validar alineación y aprobación de specs

### Phase 1: Foundation & Domain Models (5 tareas - 7 SP)
- ⏳ T001: Crear entidad de dominio TaxType (2 SP)
- ⏳ T002: Crear enum TaxApplicationType (1 SP)
- ⏳ T003: Crear excepciones específicas del dominio (1 SP)
- ⏳ T004: Implementar TaxTypeDomainService (2 SP)
- ⏳ T005: Implementar TaxTypeValidationService (1 SP)

### Phase 2: Database Schema & Migrations (3 tareas - 3 SP)
- ⏳ T006: Crear migración V7 para tabla tax_types (1 SP)
- ⏳ T007: Crear migración V8 con seed data de Colombia (1 SP)
- ⏳ T008: Crear scripts Docker para inicialización MySQL (1 SP)

### Phase 3: Domain Ports & Application Layer (4 tareas - 7 SP)
- ⏳ T009: Crear puerto de salida TaxTypeRepository (1 SP)
- ⏳ T010: Crear puertos de entrada (Use Cases) - CQRS (1 SP)
- ⏳ T011: Implementar CompareTaxTypesUseCaseImpl (2 SP)
- ⏳ T012: Implementar ManageTaxTypeUseCaseImpl (3 SP)

### Phase 4: Infrastructure - Persistence Layer (5 tareas - 6 SP)
- ⏳ T013: Crear TaxTypeEntity (JPA) (1 SP)
- ⏳ T014: Crear TaxTypeJpaRepository (1 SP)
- ⏳ T015: Crear TaxTypeEntityMapper (MapStruct) (1 SP)
- ⏳ T016: Crear TaxTypeSpecificationUtil (1 SP)
- ⏳ T017: Implementar TaxTypeRepositoryAdapter (2 SP)

### Phase 5: Infrastructure - Web Layer (REST API) (6 tareas - 7 SP)
- ⏳ T018: Crear DTOs de request (1 SP)
- ⏳ T019: Crear DTO de response (1 SP)
- ⏳ T020: Crear TaxTypeDtoMapper (MapStruct) (1 SP)
- ⏳ T021: Crear TaxTypeController - Parte 1: CRUD básico (2 SP)
- ⏳ T022: Crear TaxTypeController - Parte 2: Operaciones adicionales (1 SP)
- ⏳ T023: Agregar exception handlers a GlobalExceptionHandler (1 SP)

### Phase 6: Configuration & Bean Setup (2 tareas - 2 SP)
- ⏳ T024: Registrar beans de dominio en BeanConfiguration (1 SP)
- ⏳ T025: Verificar configuración de MapStruct (1 SP)

### Phase 7: Testing & Quality Assurance (4 tareas - 9 SP)
- ⏳ T026: Tests de dominio - Coverage 100% (~80 tests) (2 SP)
- ⏳ T027: Tests de application - Coverage >= 95% (~60 tests) (3 SP)
- ⏳ T028: Tests de infrastructure - Coverage >= 85% (~70 tests) (3 SP)
- ⏳ T029: Ejecutar suite completa y verificar cobertura (1 SP)

### Phase 8: Documentation & Final Validation (3 tareas - 3 SP)
- ⏳ T030: Documentar API con SpringDoc (1 SP)
- ⏳ T031: Build completo y validación final (1 SP)
- ⏳ T032: Actualizar STATUS.md con métricas finales (1 SP)

---

## 📊 Métricas

### Métricas de Implementación
- **Test Coverage**: -% (objetivo: >= 85%, dominio 100%, application >= 95%, infra >= 85%)
- **API Endpoints**: 0/7 implementados
- **Domain Models**: 0/1 implementados (TaxType)
- **Domain Services**: 0/2 implementados
- **Use Cases**: 0/2 implementados (Compare + Manage CQRS)
- **Migrations**: 0/2 creadas (V7 schema + V8 seed)
- **Seed Data**: 0/10 registros cargados
- **Tests Estimados**: ~210 tests totales

### Distribución de Tests Estimados
- **Domain Tests**: ~80 tests (TaxType, services, validations, exceptions)
- **Application Tests**: ~60 tests (use cases con mocks)
- **Infrastructure Tests**: ~70 tests (controller MockMvc, adapters H2, mappers)

### Archivos a Crear (Estimados)
- **Domain**: ~8 archivos (model, services, exceptions)
- **Application**: ~4 archivos (ports, use cases)
- **Infrastructure**: ~12 archivos (entities, repos, adapters, DTOs, controllers, mappers)
- **Tests**: ~15 archivos de test
- **Database**: 4 archivos (2 Flyway + 2 Docker)
- **Total**: ~43 archivos nuevos

---

## 📊 Métricas

- **Test Coverage**: -% (objetivo: >= 85%, dominio 100%, application >= 95%)
- **API Endpoints**: 0/7 implementados
- **Domain Models**: 0/1 implementados (TaxType)
- **Domain Services**: 0/2 implementados
- **Use Cases**: 0/2 implementados (Compare + Manage CQRS)
- **Migrations**: 0/2 creadas (V7 schema + V8 seed)
- **Seed Data**: 0/10 registros cargados
- **Tests Estimados**: ~210 tests totales

---

## ⚠️ Blockers

_No hay blockers actualmente. Especificación completa y lista para implementación._

---

## � Riesgos Identificados

### R001: Validación de porcentaje con 4 decimales
- **Descripción**: BigDecimal con 4 decimales puede tener problemas de precisión
- **Severidad**: 🟡 Media
- **Mitigación**: Usar BigDecimal.scale() y tests exhaustivos con valores límite (0.0001, 99.9999, 100.0000)
- **Estado**: Identificado

### R002: Lógica de filtrado por applicationType
- **Descripción**: El valor BOTH debe aparecer en filtros de SALE y PURCHASE, lógica puede ser compleja
- **Severidad**: 🟢 Baja
- **Mitigación**: Implementar TaxTypeSpecificationUtil con tests comprehensivos
- **Estado**: Identificado

### R003: Métodos TODO de conteo
- **Descripción**: countProductsWithTaxType y countTransactionsWithTaxType son TODOs (módulos futuros)
- **Severidad**: 🟢 Baja
- **Mitigación**: Retornar 0 temporalmente, documentar TODOs, crear issues para implementación futura
- **Estado**: Documentado

---

## �📅 Timeline

- **Fecha de especificación**: 2026-02-13
- **Fecha de inicio**: Por definir (pending T000 approval)
- **Fecha estimada de finalización**: ~1.5 semanas después de inicio
- **Estimación**: 45 story points
- **Distribución**:
  - Implementación: 26 SP (phases 0-6)
  - Testing: 9 SP (phase 7)
  - Documentación y validación: 3 SP (phase 8)
  - Contingencia: 7 SP

---

## 🔗 Links Relacionados

- [Functional Spec v1.1](1-functional-spec.md)
- [Technical Spec v1.0](2-technical-spec.md)
- [Tasks JSON](3-tasks.json) - 32 tareas en 8 fases
- [Framework SDD](../../framework/proyecto-framework-sdd.md)
- [STATUS Global](../../STATUS.md)
- [Scaffolding](../../scaffolding.md)
- [Project Rules](../../RULES.md)

---

## 📝 Notas Técnicas

- **Sin dependencias**: Módulo completamente independiente, puede implementarse en cualquier momento
- **Campo percentage**: DECIMAL(7,4) para soportar hasta 100.9999% con 4 decimales
- **Enum storage**: TaxApplicationType se almacena como STRING (no ORDINAL)
- **Filtrado BOTH**: El valor BOTH debe aparecer en filtros de SALE y PURCHASE
- **TODOs futuros**: countProductsWithTaxType y countTransactionsWithTaxType retornan 0 temporalmente
- **Validación código**: ^[A-Z0-9._-]+$ - permite mayúsculas, números, puntos, guiones, underscores
- **Seed data**: 10 impuestos colombianos vigentes en 2026
- **API versioning**: /api/v1/ mantenido según reglas del proyecto

---

## 🎯 Definition of Done

- [ ] Todas las 32 tareas completadas (T000-T032)
- [ ] Cobertura de tests >= 85% global (dominio 100%, application >= 95%, infra >= 85%)
- [ ] Todos los tests pasando (~210 tests estimados)
- [ ] API documentada en Swagger/SpringDoc (7 endpoints)
- [ ] Seed data de Colombia cargado (10 registros)
- [ ] Migraciones Flyway ejecutadas correctamente (V7 + V8)
- [ ] Docker scripts creados (07 + 08)
- [ ] Performance < 200ms p95 en endpoints de consulta
- [ ] Code review aprobado
- [ ] Sin errores ni warnings en build (mvn clean install)
- [ ] Sin warnings críticos de calidad
- [ ] STATUS.md global actualizado
- [ ] Módulo movido a features/04-tax-types/ al completar

---

## 🎬 Next Steps

1. **Revisar y aprobar specs** (T000 - 1 SP)
   - Validar 1-functional-spec.md v1.1
   - Validar 2-technical-spec.md v1.0
   - Validar 3-tasks.json (32 tareas)
2. **Asignar developer** una vez aprobado
3. **Iniciar Phase 1** - Foundation & Domain Models (7 SP)
4. **Actualizar STATUS.md** diariamente durante desarrollo
5. **Al completar**, mover a `features/04-tax-types/` y crear `IMPLEMENTED.md`

### Recomendaciones de Implementación
- Seguir estrictamente el orden de fases (0 → 8)
- Ejecutar tests continuamente (TDD preferido)
- Commit por tarea completada (mensajes descriptivos)
- Usar payment-methods como referencia (módulo 03 - 100% completo)
- Aplicar código limpio y SOLID en todo momento
- Validar con mvn clean install antes de cada commit
