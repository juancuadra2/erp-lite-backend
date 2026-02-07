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
│   │       │           ├── GeographyDomainException.java
│   │       │           ├── DepartmentNotFoundException.java
│   │       │           ├── MunicipalityNotFoundException.java
│   │       │           ├── DuplicateCodeException.java
│   │       │           └── GeographyConstraintException.java
│   │       │
│   │       ├── application/                             # APPLICATION LAYER
│   │       │   ├── port/
│   │       │   │   ├── in/                              # INPUT PORTS (Use Cases)
│   │       │   │   │   ├── department/
│   │       │   │   │   │   ├── CreateDepartmentUseCase.java
│   │       │   │   │   │   ├── GetDepartmentUseCase.java
│   │       │   │   │   │   ├── UpdateDepartmentUseCase.java
│   │       │   │   │   │   ├── DeactivateDepartmentUseCase.java
│   │       │   │   │   │   ├── ActivateDepartmentUseCase.java
│   │       │   │   │   │   ├── DeleteDepartmentUseCase.java
│   │       │   │   │   │   └── ListDepartmentsUseCase.java
│   │       │   │   │   └── municipality/
│   │       │   │   │       ├── CreateMunicipalityUseCase.java
│   │       │   │   │       ├── GetMunicipalityUseCase.java
│   │       │   │   │       ├── UpdateMunicipalityUseCase.java
│   │       │   │   │       ├── DeactivateMunicipalityUseCase.java
│   │       │   │   │       ├── ActivateMunicipalityUseCase.java
│   │       │   │   │       ├── DeleteMunicipalityUseCase.java
│   │       │   │   │       └── ListMunicipalitiesUseCase.java
│   │       │   │   └── out/                             # OUTPUT PORTS (Repository Interfaces)
│   │       │   │       ├── DepartmentPort.java
│   │       │   │       └── MunicipalityPort.java
│   │       │   │
│   │       │   └── service/                             # USE CASE IMPLEMENTATIONS
│   │       │       └── geography/
│   │       │           ├── department/
│   │       │           │   ├── CreateDepartmentService.java
│   │       │           │   ├── GetDepartmentService.java
│   │       │           │   ├── UpdateDepartmentService.java
│   │       │           │   ├── DeactivateDepartmentService.java
│   │       │           │   ├── ActivateDepartmentService.java
│   │       │           │   ├── DeleteDepartmentService.java
│   │       │           │   └── ListDepartmentsService.java
│   │       │           └── municipality/
│   │       │               ├── CreateMunicipalityService.java
│   │       │               ├── GetMunicipalityService.java
│   │       │               ├── UpdateMunicipalityService.java
│   │       │               ├── DeactivateMunicipalityService.java
│   │       │               ├── ActivateMunicipalityService.java
│   │       │               ├── DeleteMunicipalityService.java
│   │       │               └── ListMunicipalitiesService.java
│   │       │
│   │       └── infrastructure/                          # INFRASTRUCTURE LAYER
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
│   │                       │       └── MunicipalityResponseDto.java
│   │                       ├── mapper/
│   │                       │   ├── DepartmentDtoMapper.java
│   │                       │   └── MunicipalityDtoMapper.java
│   │                       └── exception/
│   │                           └── GeographyExceptionHandler.java
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V1_5__create_geography_tables.sql
│               └── V1_6__insert_colombia_geography.sql
│
└── test/
    └── java/
        └── com/jcuadrado/erplitebackend/
            └── geography/
                ├── domain/
                │   ├── DepartmentTest.java
                │   └── MunicipalityTest.java
                ├── application/
                │   ├── CreateDepartmentServiceTest.java
                │   ├── CreateMunicipalityServiceTest.java
                │   ├── ListDepartmentsServiceTest.java
                │   └── ListMunicipalitiesServiceTest.java
                └── infrastructure/
                    ├── rest/
                    │   ├── DepartmentControllerTest.java
                    │   └── MunicipalityControllerTest.java
                    └── persistence/
                        ├── DepartmentRepositoryAdapterTest.java
                        └── MunicipalityRepositoryAdapterTest.java
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

- [ ] **T005**: Crear migración inicial `V1_5__create_geography_tables.sql`
  ```sql
  CREATE TABLE departments (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      uuid CHAR(36) NOT NULL UNIQUE,
      code VARCHAR(10) NOT NULL UNIQUE,
      name VARCHAR(100) NOT NULL,
      enabled BOOLEAN DEFAULT TRUE,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      INDEX idx_departments_code (code),
      INDEX idx_departments_name (name),
      INDEX idx_departments_enabled (enabled),
      INDEX idx_departments_uuid (uuid)
  );

  CREATE TABLE municipalities (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      uuid CHAR(36) NOT NULL UNIQUE,
      code VARCHAR(10) NOT NULL,
      name VARCHAR(100) NOT NULL,
      department_id BIGINT NOT NULL,
      enabled BOOLEAN DEFAULT TRUE,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      CONSTRAINT fk_municipality_department 
        FOREIGN KEY (department_id) REFERENCES departments(id) 
        ON DELETE RESTRICT,
      CONSTRAINT uk_municipality_code_department 
        UNIQUE (code, department_id),
      INDEX idx_municipalities_code (code),
      INDEX idx_municipalities_name (name),
      INDEX idx_municipalities_enabled (enabled),
      INDEX idx_municipalities_uuid (uuid),
      INDEX idx_municipalities_department_id (department_id)
  );
  ```

- [ ] **T006**: Crear script de carga inicial `V1_6__insert_colombia_geography.sql`
  - 32 departamentos de Colombia con códigos DANE
  - 1,100+ municipios con sus códigos DANE
  - Datos reales actualizados de Colombia

- [ ] **T007**: Documentar modelo de base de datos
  - Diagrama ER (Department 1:N Municipality)
  - Índices y constraints
  - Plan de mantenimiento

---

### Phase 3: Persistence Layer (Output Adapters) (5 tasks)

**Purpose**: Implementar capa de persistencia con JPA entities, repositories y adapters.

**Tasks**:

- [ ] **T008**: Crear JPA entities en `infrastructure/out/geography/persistence/entity/`
  - `DepartmentEntity` con anotaciones JPA
  - `MunicipalityEntity` con relación @ManyToOne a DepartmentEntity
  - Mapeo de campos de auditoría (createdAt, updatedAt)
  - Usar Lombok (@Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor)

- [ ] **T009**: Crear Spring Data JPA repositories en `infrastructure/out/geography/persistence/repository/`
  - `DepartmentJpaRepository extends JpaRepository<DepartmentEntity, Long>`
    - `Optional<DepartmentEntity> findByCode(String code)`
    - `Optional<DepartmentEntity> findByUuid(UUID uuid)`
    - `Page<DepartmentEntity> findByEnabled(Boolean enabled, Pageable pageable)`
    - `Page<DepartmentEntity> findByNameContainingIgnoreCase(String name, Pageable pageable)`
    - `List<DepartmentEntity> findByEnabledTrue()`
    - `boolean existsByCode(String code)`
  - `MunicipalityJpaRepository extends JpaRepository<MunicipalityEntity, Long>`
    - `Optional<MunicipalityEntity> findByUuid(UUID uuid)`
    - `Page<MunicipalityEntity> findByDepartmentId(Long departmentId, Pageable pageable)`
    - `Page<MunicipalityEntity> findByDepartmentIdAndEnabled(Long departmentId, Boolean enabled, Pageable pageable)`
    - `Optional<MunicipalityEntity> findByCodeAndDepartmentId(String code, Long departmentId)`
    - `Page<MunicipalityEntity> findByNameContainingIgnoreCase(String name, Pageable pageable)`
    - `List<MunicipalityEntity> findByEnabledTrue()`
    - `boolean existsByCodeAndDepartmentId(String code, Long departmentId)`
    - `long countByDepartmentId(Long departmentId)`

- [ ] **T010**: Crear mappers con MapStruct en `infrastructure/out/geography/mapper/`
  - `DepartmentEntityMapper`: Department ↔ DepartmentEntity
  - `MunicipalityEntityMapper`: Municipality ↔ MunicipalityEntity
  - Usar @Mapper(componentModel = "spring")
  - Configurar mappings para evitar ciclos: @Mapping(target = "department.municipalities", ignore = true)

- [ ] **T011**: Implementar adapters en `infrastructure/out/geography/persistence/adapter/`
  - `DepartmentRepositoryAdapter implements DepartmentPort`
  - `MunicipalityRepositoryAdapter implements MunicipalityPort`
  - Usar @Component y @RequiredArgsConstructor

- [ ] **T012**: Tests de integración de persistencia
  - Tests con @DataJpaTest para repositories
  - Tests de adapters con Testcontainers (opcional)
  - Verificar queries y relaciones

---

### Phase 4: Application Layer (Use Cases) (7 tasks)

**Purpose**: Implementar casos de uso (puertos de entrada) con lógica de negocio.

**Tasks**:

- [ ] **T013**: Definir puertos de salida (repository interfaces) en `application/port/out/`
  - `DepartmentPort` con métodos: save, findByUuid, findByCode, findById, findAll, findByEnabled, findByNameContaining, findAllEnabled, delete, existsByCode, count
  - `MunicipalityPort` con métodos: save, findByUuid, findByCodeAndDepartmentId, findByDepartmentId, findByDepartmentIdAndEnabled, findByNameContaining, findAll, findAllEnabled, delete, existsByCodeAndDepartmentId, countByDepartmentId
  - Todos los métodos devuelven objetos de dominio, no entidades

- [ ] **T014**: Definir puertos de entrada (use case interfaces) en `application/port/in/`
  - **Department Use Cases** en `application/port/in/department/`:
    - `CreateDepartmentUseCase`: Department execute(CreateDepartmentRequestDto)
    - `GetDepartmentUseCase`: Department execute(UUID uuid)
    - `UpdateDepartmentUseCase`: Department execute(UUID uuid, UpdateDepartmentRequestDto)
    - `ListDepartmentsUseCase`: Page<Department> execute(Boolean enabled, String name, Pageable)
    - `DeactivateDepartmentUseCase`: void execute(UUID uuid)
    - `ActivateDepartmentUseCase`: void execute(UUID uuid)
    - `DeleteDepartmentUseCase`: void execute(UUID uuid)
  - **Municipality Use Cases** en `application/port/in/municipality/`:
    - `CreateMunicipalityUseCase`: Municipality execute(CreateMunicipalityRequestDto)
    - `GetMunicipalityUseCase`: Municipality execute(UUID uuid)
    - `UpdateMunicipalityUseCase`: Municipality execute(UUID uuid, UpdateMunicipalityRequestDto)
    - `ListMunicipalitiesUseCase`: Page<Municipality> execute(UUID departmentId, Boolean enabled, String name, Pageable)
    - `DeactivateMunicipalityUseCase`: void execute(UUID uuid)
    - `ActivateMunicipalityUseCase`: void execute(UUID uuid)
    - `DeleteMunicipalityUseCase`: void execute(UUID uuid)

- [ ] **T015**: Implementar servicios de Department en `application/service/geography/department/`
  - `CreateDepartmentService implements CreateDepartmentUseCase`
    - Validar código único
    - Validar formato (GeographyDomainService)
    - Crear departamento
    - Usar @Service, @RequiredArgsConstructor, @Transactional
  - `GetDepartmentService implements GetDepartmentUseCase`
    - Buscar por UUID
    - Lanzar DepartmentNotFoundException si no existe
  - `UpdateDepartmentService implements UpdateDepartmentUseCase`
  - `ListDepartmentsService implements ListDepartmentsUseCase`
  - `DeactivateDepartmentService implements DeactivateDepartmentUseCase`
  - `ActivateDepartmentService implements ActivateDepartmentUseCase`
  - `DeleteDepartmentService implements DeleteDepartmentUseCase`

- [ ] **T016**: Implementar servicios de Municipality en `application/service/geography/municipality/`
  - `CreateMunicipalityService implements CreateMunicipalityUseCase`
    - Validar que departamento exista
    - Validar código único por departamento
    - Validar formato (GeographyDomainService)
    - Crear municipio
  - `GetMunicipalityService implements GetMunicipalityUseCase`
  - `UpdateMunicipalityService implements UpdateMunicipalityUseCase`
  - `ListMunicipalitiesService implements ListMunicipalitiesUseCase`
  - `DeactivateMunicipalityService implements DeactivateMunicipalityUseCase`
  - `ActivateMunicipalityService implements ActivateMunicipalityUseCase`
  - `DeleteMunicipalityService implements DeleteMunicipalityUseCase`

- [ ] **T017**: Implementar validaciones transversales
  - Validar que departamento exista antes de crear municipio
  - Validar códigos únicos
  - Validar que departamento no tenga municipios antes de eliminar

- [ ] **T018**: Tests unitarios de servicios
  - Mockear puertos de salida (DepartmentPort, MunicipalityPort)
  - Verificar lógica de negocio
  - Verificar excepciones
  - Cobertura > 80%
  - Usar @ExtendWith(MockitoExtension.class)

- [ ] **T019**: Documentar casos de uso
  - Diagramas de secuencia para flujos principales
  - Reglas de negocio por caso de uso
  - Javadoc en interfaces de use cases

---

### Phase 5: REST API Layer (Input Adapters) (6 tasks)

**Purpose**: Exponer endpoints REST con validación, manejo de errores y documentación.

**Tasks**:

- [ ] **T020**: Crear DTOs en `infrastructure/in/api/geography/dto/`
  - **Department DTOs** en `infrastructure/in/api/geography/dto/department/`:
    - `CreateDepartmentRequestDto` (code, name, enabled)
    - `UpdateDepartmentRequestDto` (name, enabled)
    - `DepartmentResponseDto` (uuid, code, name, enabled, createdAt, updatedAt)
  - **Municipality DTOs** en `infrastructure/in/api/geography/dto/municipality/`:
    - `CreateMunicipalityRequestDto` (departmentId, code, name, enabled)
    - `UpdateMunicipalityRequestDto` (name, enabled)
    - `MunicipalityResponseDto` (uuid, code, name, department, enabled, createdAt, updatedAt)
  - Usar Java Records
  - Anotaciones de validación: @NotBlank, @NotNull, @Pattern, @Size

- [ ] **T021**: Crear mappers DTO con MapStruct en `infrastructure/in/api/geography/mapper/`
  - `DepartmentDtoMapper`: Department ↔ DepartmentResponseDto
    - toResponseDto(Department): DepartmentResponseDto
    - toResponseDtoList(List<Department>): List<DepartmentResponseDto>
  - `MunicipalityDtoMapper`: Municipality ↔ MunicipalityResponseDto
    - toResponseDto(Municipality): MunicipalityResponseDto
    - toResponseDtoList(List<Municipality>): List<MunicipalityResponseDto>
    - Usar uses = {DepartmentDtoMapper.class} para mapear el departamento anidado
  - Usar @Mapper(componentModel = "spring")

- [ ] **T022**: Implementar `DepartmentController` en `infrastructure/in/api/geography/rest/`
  - `POST /api/geography/departments` - crear departamento
  - `GET /api/geography/departments/{uuid}` - obtener por UUID
  - `PUT /api/geography/departments/{uuid}` - actualizar
  - `PATCH /api/geography/departments/{uuid}/deactivate` - desactivar
  - `PATCH /api/geography/departments/{uuid}/activate` - activar
  - `DELETE /api/geography/departments/{uuid}` - eliminar
  - `GET /api/geography/departments` - listar con filtros (enabled, name, paginación)
  - Usar @RestController, @RequestMapping, @RequiredArgsConstructor
  - Validar con @Valid
  - Mapear resultados con DepartmentDtoMapper

- [ ] **T023**: Implementar `MunicipalityController` en `infrastructure/in/api/geography/rest/`
  - `POST /api/geography/municipalities` - crear municipio
  - `GET /api/geography/municipalities/{uuid}` - obtener por UUID
  - `PUT /api/geography/municipalities/{uuid}` - actualizar
  - `PATCH /api/geography/municipalities/{uuid}/deactivate` - desactivar
  - `PATCH /api/geography/municipalities/{uuid}/activate` - activar
  - `DELETE /api/geography/municipalities/{uuid}` - eliminar
  - `GET /api/geography/municipalities` - listar con filtros (departmentId, enabled, name, paginación)
  - Mapear resultados con MunicipalityDtoMapper

- [ ] **T024**: Implementar `GeographyExceptionHandler` en `infrastructure/in/api/geography/exception/`
  - Capturar DepartmentNotFoundException → 404
  - Capturar MunicipalityNotFoundException → 404
  - Capturar DuplicateCodeException → 409
  - Capturar GeographyConstraintException → 409
  - Capturar MethodArgumentNotValidException → 400
  - Usar @RestControllerAdvice
  - Devolver ErrorResponse consistente

- [ ] **T025**: Tests de integración de controllers
  - `DepartmentControllerTest` con @WebMvcTest
  - `MunicipalityControllerTest` con @WebMvcTest
  - Mockear use cases
  - Verificar status codes, headers, body
  - Tests de validaciones (400)
  - Tests de excepciones de negocio (404, 409)
  - Mapear GeographyConstraintException → 409
  - Formato de error estándar: `{timestamp, status, error, message, path}`

---

### Phase 6: Testing & Quality (4 tasks)

**Purpose**: Tests completos (unitarios, integración, API) y análisis de cobertura.

**Tasks**:

- [ ] **T026**: Tests de integración de repositories con @DataJpaTest
  - `DepartmentJpaRepositoryTest`: CRUD completo con queries personalizados
  - `MunicipalityJpaRepositoryTest`: CRUD + relaciones
  - Verificar constraints de unicidad
  - Verificar integridad referencial
  - Usar base de datos en memoria (H2) o Testcontainers

- [ ] **T027**: Tests E2E (End-to-End) con @SpringBootTest
  - Flujo: crear departamento → crear municipio → listar → buscar → actualizar → desactivar
  - Flujo: intentar eliminar departamento con municipios → verificar error 409
  - Flujo: validar códigos duplicados → verificar error 409
  - Tests con base de datos real (Testcontainers)

- [ ] **T028**: Análisis de cobertura y calidad
  - JaCoCo: cobertura > 80% en domain y application
  - Verificar cobertura de branches críticos
  - SonarQube: sin code smells críticos

- [ ] **T029**: Tests de performance
  - JMeter o Gatling para endpoints de listado
  - Objetivo: listados paginados < 200ms p95
  - Verificar N+1 queries con Hibernate Statistics

---

### Phase 7: Documentation (2 tasks)

**Purpose**: Documentar API y crear guía de usuario.

**Tasks**:

- [ ] **T030**: Documentar API con OpenAPI (Springdoc)
  - Anotaciones @Operation, @ApiResponse en controllers
  - Ejemplos de request/response en DTOs
  - Generar Swagger UI en /swagger-ui.html
  - Agrupar endpoints por tags (departments, municipalities)

- [ ] **T031**: Crear guía de usuario y documentación técnica
  - README del módulo con ejemplos de uso
  - Colección de Postman/Insomnia con ejemplos completos
  - Documentar scripts de migraciones (Flyway)
  - Diagramas de arquitectura (C4 Model)


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

**Total Tasks**: 31 tareas
**Total Estimated Time**: 3-4 semanas

- **Phase 1** (Foundation & Domain): 4 tareas - 2 días
- **Phase 2** (Database Schema): 3 tareas - 1 día
- **Phase 3** (Persistence Layer): 5 tareas - 3 días
- **Phase 4** (Application Layer): 7 tareas - 4 días
- **Phase 5** (REST API Layer): 6 tareas - 3 días
- **Phase 6** (Testing & Quality): 4 tareas - 3 días
- **Phase 7** (Documentation): 2 tareas - 2 días
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
