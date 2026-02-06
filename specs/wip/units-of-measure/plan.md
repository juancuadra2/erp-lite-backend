# Implementation Plan: Módulo de Unidades de Medida

**Feature**: 05-units-of-measure  
**Version**: 1.0  
**Created**: 2026-02-01  
**Estimated Story Points**: 24  
**Estimated Duration**: ~3 días (24 horas)

---

## 📋 Overview

Este plan describe la implementación del módulo de **Unidades de Medida** siguiendo la arquitectura hexagonal y el framework SDD.

---

## 🎯 Objectives

- Implementar catálogo CRUD de unidades de medida
- Cargar 15 unidades predefinidas para Colombia
- Validar unicidad de nombre y abreviatura
- Implementar soft delete con validación de uso
- Alcanzar cobertura de tests >= 85%

---

## 📊 Phases & Tasks

### Phase 1: Foundation & Domain Models (2 SP)

**Objetivo**: Crear modelos de dominio puros sin dependencias externas.

#### Tasks

**T001**: Crear entidad de dominio `UnitOfMeasure`
- **File**: `domain/unitofmeasure/model/UnitOfMeasure.java`
- **Story Points**: 1
- **Description**: 
  - Atributos: id, name, abbreviation, active, audit fields
  - Factory method: `create(name, abbreviation, userId)`
  - Business methods: `update()`, `deactivate()`, `activate()`, `isActive()`
- **Dependencies**: Ninguna

**T002**: Crear excepciones del dominio
- **File**: `domain/unitofmeasure/exception/*.java`
- **Story Points**: 0.5
- **Description**:
  - `UnitOfMeasureException` (base)
  - `UnitOfMeasureNotFoundException`
  - `DuplicateUnitOfMeasureNameException`
  - `DuplicateUnitOfMeasureAbbreviationException`
  - `UnitOfMeasureInUseException`
  - `InvalidUnitOfMeasureDataException`
- **Dependencies**: Ninguna

**T003**: Crear commands
- **File**: `application/command/*.java`
- **Story Points**: 0.5
- **Description**:
  - `CreateUnitOfMeasureCommand` (name, abbreviation, userId)
  - `UpdateUnitOfMeasureCommand` (name, abbreviation, userId)
- **Dependencies**: T001

---

### Phase 2: Domain Services (2 SP)

**Objetivo**: Implementar lógica de negocio y validaciones.

#### Tasks

**T004**: Implementar `UnitOfMeasureDomainService`
- **File**: `domain/unitofmeasure/service/UnitOfMeasureDomainService.java`
- **Story Points**: 1
- **Description**:
  - `validateName()`: formato, longitud, caracteres permitidos
  - `validateAbbreviation()`: formato, longitud, caracteres permitidos
  - `ensureCanBeDeleted()`: verificar si está en uso
- **Dependencies**: T001, T002

**T005**: Implementar `UnitOfMeasureValidationService`
- **File**: `domain/unitofmeasure/service/UnitOfMeasureValidationService.java`
- **Story Points**: 1
- **Description**:
  - `ensureNameIsUnique()`: case-insensitive
  - `ensureAbbreviationIsUnique()`: case-insensitive
- **Dependencies**: T002

---

### Phase 3: Application Ports (1 SP)

**Objetivo**: Definir interfaces de casos de uso y repositorio.

#### Tasks

**T006**: Crear Input Ports (Use Cases)
- **Files**: `application/port/input/*.java`
- **Story Points**: 0.5
- **Description**: Crear 7 interfaces:
  - `CreateUnitOfMeasureUseCase`
  - `GetUnitOfMeasureUseCase`
  - `UpdateUnitOfMeasureUseCase`
  - `DeactivateUnitOfMeasureUseCase`
  - `ActivateUnitOfMeasureUseCase`
  - `ListUnitsOfMeasureUseCase`
  - `SearchUnitsOfMeasureUseCase`
- **Dependencies**: T001, T003

**T007**: Crear Output Port (Repository Interface)
- **File**: `application/port/output/UnitOfMeasurePort.java`
- **Story Points**: 0.5
- **Description**: Interfaz con métodos:
  - `save()`, `findById()`, `findAll()`, `findByActive()`
  - `searchByName()`, `searchByAbbreviation()`
  - `existsByNameIgnoreCaseAndIdNot()`
  - `existsByAbbreviationIgnoreCaseAndIdNot()`
  - `countProductsByUnitOfMeasureId()`
- **Dependencies**: T001

---

### Phase 4: Application Services (Use Case Implementations) (4 SP)

**Objetivo**: Implementar lógica de aplicación.

#### Tasks

**T008**: Implementar `CreateUnitOfMeasureService`
- **File**: `application/service/CreateUnitOfMeasureService.java`
- **Story Points**: 1
- **Description**:
  - Validar formato (name, abbreviation)
  - Validar unicidad
  - Crear domain object
  - Persistir
- **Dependencies**: T003, T004, T005, T006, T007

**T009**: Implementar `GetUnitOfMeasureService`
- **File**: `application/service/GetUnitOfMeasureService.java`
- **Story Points**: 0.5
- **Description**: Obtener por UUID con manejo de NotFound
- **Dependencies**: T006, T007

**T010**: Implementar `UpdateUnitOfMeasureService`
- **File**: `application/service/UpdateUnitOfMeasureService.java`
- **Story Points**: 1
- **Description**:
  - Validar formato
  - Validar unicidad (excluir ID actual)
  - Actualizar domain object
  - Persistir
- **Dependencies**: T003, T004, T005, T006, T007

**T011**: Implementar `DeactivateUnitOfMeasureService`
- **File**: `application/service/DeactivateUnitOfMeasureService.java`
- **Story Points**: 0.5
- **Description**:
  - Verificar si está en uso
  - Desactivar
  - Persistir
- **Dependencies**: T004, T006, T007

**T012**: Implementar `ActivateUnitOfMeasureService`
- **File**: `application/service/ActivateUnitOfMeasureService.java`
- **Story Points**: 0.5
- **Description**: Reactivar unidad inactiva
- **Dependencies**: T006, T007

**T013**: Implementar `ListUnitsOfMeasureService`
- **File**: `application/service/ListUnitsOfMeasureService.java`
- **Story Points**: 0.5
- **Description**: Listar con filtro enabled opcional
- **Dependencies**: T006, T007

**T014**: Implementar `SearchUnitsOfMeasureService`
- **File**: `application/service/SearchUnitsOfMeasureService.java`
- **Story Points**: 0.5
- **Description**: Buscar por name o abbreviation (case-insensitive)
- **Dependencies**: T006, T007

---

### Phase 5: Infrastructure - Persistence (3 SP)

**Objetivo**: Implementar persistencia con JPA.

#### Tasks

**T015**: Crear `UnitOfMeasureEntity`
- **File**: `infrastructure/persistence/entity/UnitOfMeasureEntity.java`
- **Story Points**: 1
- **Description**:
  - Anotaciones JPA (@Entity, @Table, @Index, @UniqueConstraint)
  - Índices: name, abbreviation, active
  - Constraints únicos: name, abbreviation
- **Dependencies**: T001

**T016**: Crear `UnitOfMeasureJpaRepository`
- **File**: `infrastructure/persistence/repository/UnitOfMeasureJpaRepository.java`
- **Story Points**: 1
- **Description**:
  - Extender JpaRepository
  - Métodos custom: searchByName, searchByAbbreviation
  - Queries de existencia y conteo
- **Dependencies**: T015

**T017**: Crear `UnitOfMeasureEntityMapper`
- **File**: `infrastructure/persistence/mapper/UnitOfMeasureEntityMapper.java`
- **Story Points**: 0.5
- **Description**: MapStruct mapper bidireccional
- **Dependencies**: T001, T015

**T018**: Implementar `UnitOfMeasureRepositoryAdapter`
- **File**: `infrastructure/persistence/adapter/UnitOfMeasureRepositoryAdapter.java`
- **Story Points**: 0.5
- **Description**: Adapter implementando UnitOfMeasurePort
- **Dependencies**: T007, T016, T017

---

### Phase 6: Infrastructure - API REST (3 SP)

**Objetivo**: Exponer endpoints REST.

#### Tasks

**T019**: Crear DTOs
- **Files**: `infrastructure/api/dto/*.java`
- **Story Points**: 1
- **Description**:
  - `CreateUnitOfMeasureDto` (validaciones Jakarta)
  - `UpdateUnitOfMeasureDto`
  - `UnitOfMeasureResponseDto`
- **Dependencies**: T001

**T020**: Crear `UnitOfMeasureDtoMapper`
- **File**: `infrastructure/api/mapper/UnitOfMeasureDtoMapper.java`
- **Story Points**: 0.5
- **Description**: MapStruct mapper para DTOs ↔ Domain
- **Dependencies**: T001, T019

**T021**: Implementar `UnitOfMeasureController`
- **File**: `infrastructure/api/controller/UnitOfMeasureController.java`
- **Story Points**: 1
- **Description**:
  - 7 endpoints (POST, GET, PUT, DELETE, PATCH, GET list, GET search)
  - Swagger annotations
  - Authentication integration
- **Dependencies**: T006, T019, T020

**T022**: Crear Global Exception Handler
- **File**: `infrastructure/api/exception/GlobalExceptionHandler.java`
- **Story Points**: 0.5
- **Description**: Mapear excepciones del dominio a HTTP responses
- **Dependencies**: T002

---

### Phase 7: Database Migrations (1 SP)

**Objetivo**: Crear schema y seed data.

#### Tasks

**T023**: Crear migración `create_units_of_measure_table`
- **File**: `resources/db/migration/V1.7__create_units_of_measure_table.sql`
- **Story Points**: 0.5
- **Description**:
  - Tabla con índices y constraints
  - Indices: name, abbreviation, active
  - Unique constraints: name, abbreviation
- **Dependencies**: Ninguna

**T024**: Crear migración `insert_colombia_units_of_measure`
- **File**: `resources/db/migration/V1.8__insert_colombia_units_of_measure.sql`
- **Story Points**: 0.5
- **Description**: Insertar 15 unidades predefinidas
- **Dependencies**: T023

---

### Phase 8: Testing - Unit Tests (5 SP)

**Objetivo**: Cobertura >= 90% en domain y application.

#### Tasks

**T025**: Test de `UnitOfMeasure` domain model
- **File**: `test/.../domain/unitofmeasure/model/UnitOfMeasureTest.java`
- **Story Points**: 0.5
- **Description**: Tests de métodos create, update, deactivate, activate
- **Dependencies**: T001

**T026**: Test de `UnitOfMeasureDomainService`
- **File**: `test/.../domain/unitofmeasure/service/UnitOfMeasureDomainServiceTest.java`
- **Story Points**: 1
- **Description**: Tests de todas las validaciones
- **Dependencies**: T004

**T027**: Test de `UnitOfMeasureValidationService`
- **File**: `test/.../domain/unitofmeasure/service/UnitOfMeasureValidationServiceTest.java`
- **Story Points**: 0.5
- **Description**: Tests de ensureNameIsUnique, ensureAbbreviationIsUnique
- **Dependencies**: T005

**T028**: Test de `CreateUnitOfMeasureService`
- **File**: `test/.../application/service/CreateUnitOfMeasureServiceTest.java`
- **Story Points**: 1
- **Description**: Casos exitosos y errores (duplicado, formato inválido)
- **Dependencies**: T008

**T029**: Test de `GetUnitOfMeasureService`
- **File**: `test/.../application/service/GetUnitOfMeasureServiceTest.java`
- **Story Points**: 0.5
- **Description**: Obtener existente y NotFound
- **Dependencies**: T009

**T030**: Test de `UpdateUnitOfMeasureService`
- **File**: `test/.../application/service/UpdateUnitOfMeasureServiceTest.java`
- **Story Points**: 0.5
- **Description**: Actualizar exitoso y errores
- **Dependencies**: T010

**T031**: Test de `DeactivateUnitOfMeasureService`
- **File**: `test/.../application/service/DeactivateUnitOfMeasureServiceTest.java`
- **Story Points**: 0.5
- **Description**: Desactivar sin uso y con productos asociados
- **Dependencies**: T011

**T032**: Test de `ListUnitsOfMeasureService`
- **File**: `test/.../application/service/ListUnitsOfMeasureServiceTest.java`
- **Story Points**: 0.5
- **Description**: Listar todas, solo activas, solo inactivas
- **Dependencies**: T013

**T033**: Test de `SearchUnitsOfMeasureService`
- **File**: `test/.../application/service/SearchUnitsOfMeasureServiceTest.java`
- **Story Points**: 0.5
- **Description**: Buscar por name y abbreviation
- **Dependencies**: T014

---

### Phase 9: Testing - Integration Tests (3 SP)

**Objetivo**: Validar integración DB y API.

#### Tasks

**T034**: Test de `UnitOfMeasureJpaRepository`
- **File**: `test/.../infrastructure/persistence/repository/UnitOfMeasureJpaRepositoryTest.java`
- **Story Points**: 1.5
- **Description**:
  - Tests con Testcontainers MySQL
  - Queries custom: searchByName, searchByAbbreviation
  - Constraints únicos
- **Dependencies**: T016, T023, T024

**T035**: Test de `UnitOfMeasureRepositoryAdapter`
- **File**: `test/.../infrastructure/persistence/adapter/UnitOfMeasureRepositoryAdapterTest.java`
- **Story Points**: 0.5
- **Description**: Tests del adapter con mocks
- **Dependencies**: T018

**T036**: Test de `UnitOfMeasureController`
- **File**: `test/.../infrastructure/api/controller/UnitOfMeasureControllerTest.java`
- **Story Points**: 1
- **Description**:
  - Tests con MockMvc
  - 7 endpoints
  - Validaciones Jakarta
- **Dependencies**: T021

---

### Phase 10: Documentation & Review (2 SP)

**Objetivo**: Documentación y limpieza final.

#### Tasks

**T037**: Actualizar documentación Swagger
- **Story Points**: 1
- **Description**: Completar Swagger con ejemplos y descripciones
- **Dependencies**: T021

**T038**: Code Review y Cleanup
- **Story Points**: 1
- **Description**:
  - Verificar estándares
  - Eliminar código muerto
  - Optimizar imports
  - Verificar coverage >= 85%
- **Dependencies**: T025-T037

---

## 📈 Story Points Summary

| Phase | Story Points |
|-------|--------------|
| 1. Foundation & Domain Models | 2 |
| 2. Domain Services | 2 |
| 3. Application Ports | 1 |
| 4. Application Services | 4 |
| 5. Infrastructure - Persistence | 3 |
| 6. Infrastructure - API REST | 3 |
| 7. Database Migrations | 1 |
| 8. Testing - Unit Tests | 5 |
| 9. Testing - Integration Tests | 3 |
| 10. Documentation & Review | 2 |
| **TOTAL** | **26** |

---

## 🎯 Definition of Done

- [ ] 38 tareas completadas
- [ ] Cobertura de tests >= 85%
- [ ] Todos los tests pasando
- [ ] API documentada con Swagger
- [ ] 15 unidades precargadas
- [ ] Performance targets alcanzados (< 100ms p95)
- [ ] Code review aprobado (2+ reviewers)
- [ ] STATUS.md global actualizado

---

## 🔗 Related Documents

- [Functional Spec](functional-spec.md)
- [Technical Spec](technical-spec.md)
- [Tasks JSON](tasks.json)
- [STATUS](STATUS.md)
- [APPROVALS](APPROVALS.md)

---

**Status**: ⏳ PHASE 3 - Awaiting Approval  
**Next Step**: Team Lead Review → Approve → Move to Implementation
