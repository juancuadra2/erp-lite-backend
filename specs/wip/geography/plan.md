# Implementation Plan: Módulo de Geografía (Catálogos Base)

**Date**: January 10, 2026
**Spec**: [geography-spec.md](geography-spec.md)

## Summary

Desarrollo del módulo de catálogos geográficos simplificado que gestiona la jerarquía de ubicaciones (Departamento > Municipio). Este módulo no tiene dependencias de otros módulos y es requerido por Company, Contact, Warehouse, Sales y Purchases para gestión de direcciones. Incluye carga masiva inicial de datos de Colombia (32 departamentos, 1,100+ municipios), endpoints REST CRUD completos, búsqueda con autocompletado, y validaciones de integridad referencial.

## Technical Context

**Languages/Versions**: Java 17+
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, MySQL Connector, Lombok, MapStruct, Hibernate Validator, Flyway
**Storage**: MySQL 8.0+
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers
**Target Platforms**: RESTful API
**Performance Goals**: Autocompletado < 100ms p95, listados paginados < 200ms p95
**Constraints**: Soft delete obligatorio, auditoría completa, integridad referencial estricta
**Scale/Scope**: Single-tenant, catálogo base sin dependencias

## Project Structure

Estructura siguiendo arquitectura hexagonal dentro del módulo geography:

```text
src/
├── main/
│   ├── java/
│   │   └── com/jcuadrado/erplitebackend/
│   │       │
│   │       ├── domain/
│   │       │   └── geography/                           # DOMAIN LAYER
│   │       │       ├── model/
│   │       │       │   ├── Department.java              # Aggregate Root
│   │       │       │   └── Municipality.java            # Entity
│   │       │       ├── service/
│   │       │       │   ├── GeographyDomainService.java  # Business rules
│   │       │       │   └── GeographyValidationService.java
│   │       │       └── exception/
│   │       │           ├── DepartmentNotFoundException.java
│   │       │           ├── MunicipalityNotFoundException.java
│   │       │           ├── DuplicateCodeException.java
│   │       │           └── GeographyConstraintException.java
│   │       │
│   │       ├── application/
│   │       │   └── port/
│   │       │       ├── in/                              # INPUT PORTS (Use Cases)
│   │       │       │   ├── department/
│   │       │       │   │   ├── CreateDepartmentUseCase.java
│   │       │       │   │   ├── GetDepartmentUseCase.java
│   │       │       │   │   ├── UpdateDepartmentUseCase.java
│   │       │       │   │   ├── DeactivateDepartmentUseCase.java
│   │       │       │   │   ├── ListDepartmentsUseCase.java
│   │       │       │   │   └── SearchDepartmentsUseCase.java
│   │       │       │   └── municipality/
│   │       │       │       ├── CreateMunicipalityUseCase.java
│   │       │       │       ├── GetMunicipalityUseCase.java
│   │       │       │       ├── UpdateMunicipalityUseCase.java
│   │       │       │       ├── ListMunicipalitiesUseCase.java
│   │       │       │       ├── SearchMunicipalitiesUseCase.java
│   │       │       │       └── AutocompleteMunicipalitiesUseCase.java
│   │       │       └── out/                             # OUTPUT PORTS (Repository Interfaces)
│   │       │           ├── DepartmentPort.java
│   │       │           └── MunicipalityPort.java
│   │       │
│   │       └── infrastructure/
│   │           ├── out/                                 # OUTPUT ADAPTERS
│   │           │   └── geography/
│   │           │       ├── persistence/
│   │           │       │   ├── entity/
│   │           │       │   │   ├── DepartmentEntity.java
│   │           │       │   │   └── MunicipalityEntity.java
│   │           │       │   ├── repository/
│   │           │       │   │   ├── DepartmentJpaRepository.java
│   │           │       │   │   └── MunicipalityJpaRepository.java
│   │           │       │   └── adapter/
│   │           │       │       ├── DepartmentRepositoryAdapter.java
│   │           │       │       └── MunicipalityRepositoryAdapter.java
│   │           │       └── mapper/
│   │           │           ├── DepartmentEntityMapper.java
│   │           │           └── MunicipalityEntityMapper.java
│   │           │
│   │           └── in/                                  # INPUT ADAPTERS
│   │               └── api/
│   │                   └── geography/
│   │                       ├── rest/
│   │                       │   ├── DepartmentController.java
│   │                       │   └── MunicipalityController.java
│   │                       ├── dto/
│   │                       │   ├── department/
│   │                       │   │   ├── CreateDepartmentRequestDto.java
│   │                       │   │   ├── UpdateDepartmentRequestDto.java
│   │                       │   │   └── DepartmentResponseDto.java
│   │                       │   └── municipality/
│   │                       │       ├── CreateMunicipalityRequestDto.java
│   │                       │       ├── UpdateMunicipalityRequestDto.java
│   │                       │       ├── MunicipalityResponseDto.java
│   │                       │       └── MunicipalityAutocompleteDto.java
│   │                       ├── mapper/
│   │                       │   ├── DepartmentDtoMapper.java
│   │                       │   └── MunicipalityDtoMapper.java
│   │                       └── constant/
│   │                           └── GeographyApiConstants.java
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V1.1__create_geography_tables.sql
│               └── V1.2__insert_colombia_geography.sql
│
└── test/
    └── java/
        └── com/jcuadrado/erplitebackend/
            └── geography/
                ├── domain/
                │   ├── DepartmentTest.java
                │   └── MunicipalityTest.java
                ├── application/
                │   ├── CreateDepartmentUseCaseTest.java
                │   ├── CreateMunicipalityUseCaseTest.java
                │   ├── ListDepartmentsUseCaseTest.java
                │   └── AutocompleteMunicipalitiesUseCaseTest.java
                └── infrastructure/
                    ├── api/
                    │   ├── DepartmentControllerTest.java
                    │   └── MunicipalityControllerTest.java
                    └── persistence/
                        ├── DepartmentRepositoryTest.java
                        └── MunicipalityRepositoryTest.java
```

---

## Implementation Phases

### Phase 1: Foundation & Domain Models (4 tasks)

**Purpose**: Establecer domain models, value objects y excepciones del módulo.

**Tasks**:

- [ ] **T001**: Crear entidades de dominio `Department` y `Municipality` con atributos completos en `domain/geography/model/`
  - Atributos según spec (id, uuid, code, name, enabled, createdBy, updatedBy, createdAt, updatedAt)
  - Métodos de negocio: `activate()`, `deactivate()`, `validateUniqueness()`
  - Sin anotaciones JPA (domain puro)
  - Relaciones: Department → List<Municipality>

- [ ] **T002**: Crear excepciones específicas del dominio en `domain/geography/exception/`
  - `DepartmentNotFoundException extends DomainException`
  - `MunicipalityNotFoundException extends DomainException`
  - `DuplicateCodeException extends BusinessRuleException`
  - `GeographyConstraintException extends BusinessRuleException`

- [ ] **T003**: Implementar `GeographyDomainService` con reglas de negocio
  - `validateDepartmentCode(String code)`: validar formato (2 dígitos)
  - `validateMunicipalityCode(String code)`: validar formato (5 dígitos)
  - `canDeleteDepartment(Department department)`: verificar si tiene municipios
  - Tests unitarios > 80%

- [ ] **T004**: Implementar `GeographyValidationService`
  - `ensureDepartmentCodeUnique(String code, UUID excludeId)`
  - `ensureMunicipalityCodeUniqueInDepartment(UUID departmentId, String code, UUID excludeId)`
  - Tests unitarios

---

### Phase 2: Database Schema & Migrations (3 tasks)

**Purpose**: Crear schema de base de datos, índices y datos iniciales.

**Tasks**:

- [ ] **T005**: Crear migración inicial `V1.1__create_geography_tables.sql`
  ```sql
  CREATE TABLE departments (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      uuid CHAR(36) NOT NULL UNIQUE,
      code VARCHAR(10) NOT NULL UNIQUE,
      name VARCHAR(100) NOT NULL,
      enabled BOOLEAN DEFAULT TRUE,
      created_by_id BIGINT,
      updated_by_id BIGINT,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      INDEX idx_code (code),
      INDEX idx_name (name),
      INDEX idx_enabled (enabled),
      INDEX idx_uuid (uuid)
  );

  CREATE TABLE municipalities (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      uuid CHAR(36) NOT NULL UNIQUE,
      code VARCHAR(10) NOT NULL,
      name VARCHAR(100) NOT NULL,
      department_id BIGINT NOT NULL,
      enabled BOOLEAN DEFAULT TRUE,
      created_by_id BIGINT,
      updated_by_id BIGINT,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      CONSTRAINT fk_municipality_department 
        FOREIGN KEY (department_id) REFERENCES departments(id) 
        ON DELETE RESTRICT,
      CONSTRAINT uk_municipality_code_department 
        UNIQUE (code, department_id),
      INDEX idx_code (code),
      INDEX idx_name (name),
      INDEX idx_enabled (enabled),
      INDEX idx_uuid (uuid),
      INDEX idx_department_id (department_id)
  );
  ```

- [ ] **T006**: Crear script de carga inicial `V1.2__insert_colombia_geography.sql`
  - 32 departamentos de Colombia
  - 1,100+ municipios con sus códigos DANE
  - Datos reales actualizados de Colombia

- [ ] **T007**: Documentar modelo de base de datos
  - Diagrama ER (Department 1:N Municipality)
  - Índices y constraints
  - Plan de mantenimiento

---

### Phase 3: Persistence Layer (Output Adapters) (4 tasks)

**Purpose**: Implementar capa de persistencia con JPA entities, repositories y adapters.

**Tasks**:

- [ ] **T008**: Crear JPA entities en `infrastructure/out/geography/persistence/entity/`
  - `DepartmentEntity` con anotaciones JPA
  - `MunicipalityEntity` con relación @ManyToOne a DepartmentEntity
  - Mapeo de campos de auditoría (createdBy, updatedBy, etc.)

- [ ] **T009**: Crear Spring Data JPA repositories en `infrastructure/out/geography/persistence/repository/`
  - `DepartmentJpaRepository extends JpaRepository<DepartmentEntity, Long>`
    - `Optional<DepartmentEntity> findByCode(String code)`
    - `Optional<DepartmentEntity> findByUuid(UUID uuid)`
    - `List<DepartmentEntity> findByEnabled(Boolean enabled)`
    - `List<DepartmentEntity> findByNameContainingIgnoreCase(String name)`
  - `MunicipalityJpaRepository extends JpaRepository<MunicipalityEntity, Long>`
    - `Optional<MunicipalityEntity> findByUuid(UUID uuid)`
    - `List<MunicipalityEntity> findByDepartmentId(Long departmentId)`
    - `Optional<MunicipalityEntity> findByCodeAndDepartmentId(String code, Long departmentId)`
    - `List<MunicipalityEntity> findByNameContainingIgnoreCase(String name)`

- [ ] **T010**: Crear mappers con MapStruct en `infrastructure/out/geography/mapper/`
  - `DepartmentEntityMapper`: Department ↔ DepartmentEntity
  - `MunicipalityEntityMapper`: Municipality ↔ MunicipalityEntity

- [ ] **T011**: Implementar adapters en `infrastructure/out/geography/persistence/adapter/`
  - `DepartmentRepositoryAdapter implements DepartmentPort`
  - `MunicipalityRepositoryAdapter implements MunicipalityPort`
  - Tests de integración con Testcontainers

---

### Phase 4: Application Layer (Use Cases) (6 tasks)

**Purpose**: Implementar casos de uso (puertos de entrada) con lógica de negocio.

**Tasks**:

- [ ] **T012**: Definir puertos de salida (repository interfaces) en `application/port/out/`
  - `DepartmentPort` con métodos: save, findByUuid, findByCode, findAll, delete, etc.
  - `MunicipalityPort` con métodos: save, findByUuid, findByDepartment, delete, etc.

- [ ] **T013**: Implementar use cases de Department en `application/port/in/department/`
  - `CreateDepartmentUseCase`: validar código único, crear departamento
  - `UpdateDepartmentUseCase`: validar existencia, actualizar
  - `GetDepartmentUseCase`: obtener por UUID
  - `ListDepartmentsUseCase`: listado paginado con filtros
  - `DeactivateDepartmentUseCase`: desactivar departamento
  - `SearchDepartmentsUseCase`: búsqueda por nombre

- [ ] **T014**: Implementar use cases de Municipality en `application/port/in/municipality/`
  - `CreateMunicipalityUseCase`: validar código único por departamento, crear
  - `UpdateMunicipalityUseCase`: validar existencia, actualizar
  - `GetMunicipalityUseCase`: obtener por UUID con departamento
  - `ListMunicipalitiesUseCase`: listado paginado con filtros
  - `SearchMunicipalitiesUseCase`: búsqueda por nombre
  - `AutocompleteMunicipalitiesUseCase`: autocompletado para formularios

- [ ] **T015**: Implementar validaciones transversales
  - Validar que departamento exista antes de crear municipio
  - Validar códigos únicos
  - Validar que departamento no tenga municipios antes de eliminar

- [ ] **T016**: Tests unitarios de use cases
  - Mockear puertos de salida
  - Verificar lógica de negocio
  - Cobertura > 80%

- [ ] **T017**: Documentar casos de uso
  - Diagramas de secuencia para flujos principales
  - Reglas de negocio por caso de uso

---

### Phase 5: REST API Layer (Input Adapters) (5 tasks)

**Purpose**: Exponer endpoints REST con validación, manejo de errores y documentación.

**Tasks**:

- [ ] **T018**: Crear DTOs en `infrastructure/in/api/geography/dto/`
  - **Department**: CreateDepartmentRequestDto, UpdateDepartmentRequestDto, DepartmentResponseDto
  - **Municipality**: CreateMunicipalityRequestDto, UpdateMunicipalityRequestDto, MunicipalityResponseDto, MunicipalityAutocompleteDto
  - Anotaciones de validación: @NotBlank, @Size, @Pattern

- [ ] **T019**: Crear mappers DTO con MapStruct en `infrastructure/in/api/geography/mapper/`
  - `DepartmentDtoMapper`: Request/Response ↔ Domain
  - `MunicipalityDtoMapper`: Request/Response ↔ Domain

- [ ] **T020**: Implementar `DepartmentController` en `infrastructure/in/api/geography/rest/`
  - POST /api/geography/departments - Crear
  - GET /api/geography/departments - Listar (paginado)
  - GET /api/geography/departments/{uuid} - Obtener
  - PUT /api/geography/departments/{uuid} - Actualizar
  - DELETE /api/geography/departments/{uuid} - Eliminar
  - PATCH /api/geography/departments/{uuid}/deactivate - Desactivar
  - GET /api/geography/departments/search - Búsqueda

- [ ] **T021**: Implementar `MunicipalityController` en `infrastructure/in/api/geography/rest/`
  - POST /api/geography/municipalities - Crear
  - GET /api/geography/municipalities - Listar (paginado, filtro por departmentId)
  - GET /api/geography/municipalities/{uuid} - Obtener
  - PUT /api/geography/municipalities/{uuid} - Actualizar
  - DELETE /api/geography/municipalities/{uuid} - Eliminar
  - PATCH /api/geography/municipalities/{uuid}/deactivate - Desactivar
  - GET /api/geography/municipalities/search - Búsqueda
  - GET /api/geography/municipalities/autocomplete - Autocompletado

- [ ] **T022**: Implementar manejo global de excepciones
  - `@ControllerAdvice` para excepciones del módulo
  - Mapear DepartmentNotFoundException → 404
  - Mapear MunicipalityNotFoundException → 404
  - Mapear DuplicateCodeException → 409
  - Mapear GeographyConstraintException → 409
  - Formato de error estándar: `{timestamp, status, error, message, path}`

---

### Phase 6: Testing & Quality (4 tasks)

**Purpose**: Tests completos (unitarios, integración, API) y análisis de cobertura.

**Tasks**:

- [ ] **T023**: Tests de controllers con MockMvc
  - `DepartmentControllerTest`: todos los endpoints
  - `MunicipalityControllerTest`: todos los endpoints
  - Verificar status codes, headers, body
  - Casos de éxito y error

- [ ] **T024**: Tests de integración de repositories con Testcontainers
  - `DepartmentRepositoryTest`: CRUD completo con MySQL real
  - `MunicipalityRepositoryTest`: CRUD + relaciones
  - Verificar constraints de unicidad
  - Verificar integridad referencial

- [ ] **T025**: Tests E2E (End-to-End)
  - Flujo: crear departamento → crear municipio → listar → buscar → actualizar → desactivar
  - Flujo: intentar eliminar departamento con municipios → verificar error 409
  - Flujo: autocompletado de municipios

- [ ] **T026**: Análisis de cobertura y performance
  - JaCoCo: cobertura > 80%
  - Tests de performance: autocompletado < 100ms, listados < 200ms
  - SonarQube: sin code smells críticos

---

### Phase 7: Documentation (2 tasks)

**Purpose**: Documentar API y crear guía de usuario.

**Tasks**:

- [ ] **T027**: Documentar API con OpenAPI (Springdoc)
  - Anotaciones @Operation, @ApiResponse en controllers
  - Ejemplos de request/response
  - Generar Swagger UI en /swagger-ui.html

- [ ] **T028**: Crear guía de usuario
  - README del módulo con ejemplos de uso
  - Colección de Postman/Insomnia
  - Scripts de carga de datos iniciales

---

## Risk Management

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Performance en autocompletado con 1,100+ municipios | Medium | High | Índices en campos de búsqueda, caché, paginación |
| Carga inicial de datos falla | Low | Medium | Transacciones, rollback automático, validación previa |
| Conflictos en códigos DANE duplicados | Low | Medium | Constraints de unicidad en BD, validación en app |
| Eliminación accidental de departamentos con municipios | Low | High | Validación de relaciones antes de eliminar, soft delete |

---

## Dependencies & Timeline

**Total Estimated Time**: 2-3 semanas

- Phase 1: 2 días
- Phase 2: 1 día
- Phase 3: 3 días
- Phase 4: 4 días
- Phase 5: 3 días
- Phase 6: 3 días
- Phase 7: 2 días

**No tiene dependencias externas** (módulo independiente)

---

## Success Criteria

✅ Todos los tests pasan (unitarios, integración, E2E)
✅ Cobertura de código > 80%
✅ Carga inicial de 32 departamentos y 1,100+ municipios exitosa
✅ API REST completa con todos los endpoints funcionales
✅ Validaciones de integridad referencial implementadas
✅ Documentación OpenAPI generada
✅ Performance goals cumplidos (autocompletado < 100ms, listados < 200ms)
✅ Sin code smells críticos en SonarQube

---

## Notes

- Este módulo es **independiente** y puede desarrollarse sin esperar otros módulos
- Seguir estrictamente arquitectura hexagonal para facilitar mantenimiento
- Usar UUID como identificador público en API (no exponer IDs numéricos)
- Implementar soft delete con campo `enabled` (no eliminación física)
- Todos los cambios deben auditarse (createdBy, updatedBy, timestamps)
