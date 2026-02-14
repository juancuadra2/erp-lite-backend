# Implementation Plan: Document Types Module

**Date**: January 10, 2026
**Updated**: February 6, 2026
**Spec**: [technical-spec.md](2-technical-spec.md)

---

## Summary

Desarrollo del módulo de catálogos de tipos de documentos de identificación que gestiona los diferentes tipos de documentos (NIT, CC, CE, Pasaporte, etc.) utilizados para identificar personas naturales y jurídicas. Este módulo no tiene dependencias de otros módulos y es requerido por Contact, User y Company. Incluye carga masiva inicial de datos de Colombia (6 tipos de documento), endpoints REST CRUD completos, y gestión de auditoría.

**ARQUITECTURA**: Hexagonal siguiendo el nuevo scaffolding con separación clara entre domain, application e infrastructure.

---

## Technical Context

**Languages/Versions**: Java 21
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, MySQL Connector, Lombok, MapStruct, Hibernate Validator, Flyway
**Storage**: MySQL 8.0+
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers
**Target Platforms**: RESTful API
**Performance Goals**: Listados paginados < 100ms p95, operaciones CRUD < 150ms p95
**Constraints**: Soft delete obligatorio, auditoría completa
**Scale/Scope**: Single-tenant, catálogo base sin dependencias

---

## Estructura del Proyecto (Organizada por Features)

**Principio**: Cada feature/módulo tiene su propia carpeta dentro de cada capa (domain, application, infrastructure) para mantener el código organizado y escalable cuando crece el número de módulos.

```text
src/
├── main/
│   ├── java/com/jcuadrado/erplitebackend/
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── document-types/
│   │   │   │       └── DocumentType.java                    # Raíz Agregada
│   │   │   ├── service/
│   │   │   │   └── document-types/
│   │   │   │       ├── DocumentTypeDomainService.java       # Reglas de negocio
│   │   │   │       └── DocumentTypeValidator.java           # Validación de dominio
│   │   │   ├── port/
│   │   │   │   └── out/
│   │   │   │       └── document-types/
│   │   │   │           └── DocumentTypeRepository.java      # Puerto de salida
│   │   │   └── exception/
│   │   │       └── document-types/
│   │   │           ├── DocumentTypeNotFoundException.java
│   │   │           ├── DuplicateCodeException.java
│   │   │           ├── InvalidDocumentTypeException.java
│   │   │           └── DocumentTypeDomainException.java
│   │   │
│   │   ├── application/
│   │   │   ├── port/
│   │   │   │   └── in/
│   │   │   │       └── document-types/
│   │   │   │           ├── CompareDocumentTypesUseCase.java     # Operaciones de consulta
│   │   │   │           └── ManageDocumentTypeUseCase.java       # Operaciones de comando
│   │   │   └── usecase/
│   │   │       └── document-types/
│   │   │           ├── CompareDocumentTypesUseCaseImpl.java
│   │   │           └── ManageDocumentTypeUseCaseImpl.java
│   │   │
│   │   └── infrastructure/
│   │       ├── config/
│   │       │   └── BeanConfiguration.java                   # Configuración compartida
│   │       │
│   │       ├── in/
│   │       │   └── web/
│   │       │       ├── controller/
│   │       │       │   └── document-types/
│   │       │       │       └── DocumentTypeController.java
│   │       │       ├── dto/
│   │       │       │   └── document-types/
│   │       │       │       ├── DocumentTypeDto.java
│   │       │       │       ├── CreateDocumentTypeRequestDto.java
│   │       │       │       ├── UpdateDocumentTypeRequestDto.java
│   │       │       │       ├── DocumentTypeResponseDto.java
│   │       │       │       ├── DocumentTypeComparisonDto.java
│   │       │       │       ├── DocumentTypeFilterDto.java
│   │       │       │       ├── SortDto.java
│   │       │       │       └── PagedResponseDto.java
│   │       │       ├── mapper/
│   │       │       │   └── document-types/
│   │       │       │       ├── DocumentTypeDtoMapper.java
│   │       │       │       └── DocumentTypeComparisonDtoMapper.java
│   │       │       └── advice/
│   │       │           └── GlobalExceptionHandler.java      # Compartido entre features
│   │       │
│   │       └── out/
│   │           └── persistence/
│   │               ├── adapter/
│   │               │   └── document-types/
│   │               │       └── DocumentTypeRepositoryAdapter.java
│   │               ├── entity/
│   │               │   └── document-types/
│   │               │       └── DocumentTypeEntity.java
│   │               ├── mapper/
│   │               │   └── document-types/
│   │               │       └── DocumentTypeEntityMapper.java
│   │               └── util/
│   │                   └── document-types/
│   │                       └── DocumentTypeSpecificationUtil.java
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V1.3__create_document_types_tables.sql
│               └── V1.4__insert_colombia_document_types.sql
│
└── test/
    └── java/com/jcuadrado/erplitebackend/
        ├── application/
        │   └── usecase/
        │       └── document-types/
        │           ├── CompareDocumentTypesUseCaseTest.java
        │           └── ManageDocumentTypeUseCaseTest.java
        ├── domain/
        │   ├── model/
        │   │   └── document-types/
        │   │       └── DocumentTypeTest.java
        │   └── service/
        │       └── document-types/
        │           ├── DocumentTypeDomainServiceTest.java
        │           └── DocumentTypeValidatorTest.java
        └── infrastructure/
            ├── in/web/
            │   ├── controller/
            │   │   └── document-types/
            │   │       └── DocumentTypeControllerTest.java
            │   ├── advice/
            │   │   └── GlobalExceptionHandlerTest.java
            │   └── mapper/
            │       └── document-types/
            │           ├── DocumentTypeDtoMapperTest.java
            │           └── DocumentTypeComparisonDtoMapperTest.java
            └── out/persistence/
                ├── adapter/
                │   └── document-types/
                │       └── DocumentTypeRepositoryAdapterTest.java
                ├── mapper/
                │   └── document-types/
                │       └── DocumentTypeEntityMapperTest.java
                └── util/
                    └── document-types/
                        └── DocumentTypeSpecificationUtilTest.java
```

**Ventajas de esta Estructura:**
- ✅ **Escalabilidad**: Fácil agregar nuevos features (contacts, products, etc.) sin mezclar código
- ✅ **Cohesión**: Todo el código de document-types está agrupado en cada capa
- ✅ **Mantenibilidad**: Rápido encontrar y modificar funcionalidad específica del feature
- ✅ **Claridad**: La estructura refleja claramente los módulos de negocio
- ✅ **Aislamiento**: Cada feature evoluciona independientemente

---

## Fases de Implementación

### Fase 1: Fundamentos y Modelos de Dominio (5 tareas)

**Propósito**: Establecer modelos de dominio, servicios, validadores y excepciones del módulo.

**Tareas**:

- [ ] **T001**: Crear entidad de dominio `DocumentType` en `domain/model/document-types/`
  - Atributos según spec (id, uuid, code, name, description, active, audit fields)
  - Métodos de negocio: `activate()`, `deactivate()`, `isActive()`, `isDeleted()`, `markAsDeleted(Long userId)`
  - Sin anotaciones JPA (domain puro)
  - Builder pattern para construcción
  - Validación básica en setters

- [ ] **T002**: Crear excepciones específicas del dominio en `domain/exception/document-types/`
  - `DocumentTypeDomainException` (base exception)
  - `DocumentTypeNotFoundException extends DocumentTypeDomainException`
  - `DuplicateCodeException extends DocumentTypeDomainException`
  - `InvalidDocumentTypeException extends DocumentTypeDomainException`

- [ ] **T003**: Crear `DocumentTypeValidator` en `domain/service/document-types/`
  - `validateCode(String code)`: formato (2-10 caracteres alfanuméricos)
  - `validateName(String name)`: requerido, 1-200 caracteres
  - `validateDescription(String description)`: opcional, max 500 caracteres
  - Métodos que lanzan `InvalidDocumentTypeException` si falla
  - Tests unitarios > 90%

- [ ] **T004**: Implementar `DocumentTypeDomainService` con reglas de negocio en `domain/service/document-types/`
  - `normalizeCode(String code)`: normalizar código a mayúsculas
  - `validateUniqueCode(String code, DocumentTypeRepository repository)`: validar unicidad
  - `canDeactivate(DocumentType documentType)`: verificar si se puede desactivar
  - `canDelete(DocumentType documentType)`: verificar si se puede eliminar
  - Tests unitarios > 85%

- [ ] **T005**: Crear output port `DocumentTypeRepository` en `domain/port/out/document-types/`
  - Interfaz con métodos:
    - `Optional<DocumentType> findById(Long id)`
    - `Optional<DocumentType> findByUuid(UUID uuid)`
    - `Optional<DocumentType> findByCode(String code)`
    - `Page<DocumentType> findAll(DocumentTypeFilterDto filter, Pageable pageable)`
    - `DocumentType save(DocumentType documentType)`
    - `void delete(UUID uuid)`
    - `boolean existsByCode(String code)`
  - `DuplicateCodeException extends DocumentTypeDomainException`
  - `InvalidDocumentTypeException extends DocumentTypeDomainException`

- [ ] **T003**: Crear `DocumentTypeValidator` en `domain/service/`
  - `validateCode(String code)`: formato (2-10 caracteres alfanuméricos)
  - `validateName(String name)`: requerido, 1-200 caracteres
  - `validateDescription(String description)`: opcional, max 500 caracteres
  - Métodos que lanzan `InvalidDocumentTypeException` si falla
  - Tests unitarios > 90%

- [ ] **T004**: Implementar `DocumentTypeDomainService` con reglas de negocio
  - `normalizeCode(String code)`: normalizar código a mayúsculas
  - `validateUniqueCode(String code, DocumentTypeRepository repository)`: validar unicidad
  - `canDeactivate(DocumentType documentType)`: verificar si se puede desactivar
  - `canDelete(DocumentType documentType)`: verificar si se puede eliminar
  - Tests unitarios > 85%

- [ ] **T005**: Crear output port `DocumentTypeRepository` en `domain/port/out/`
  - Interfaz con métodos:
    - `Optional<DocumentType> findById(Long id)`
    - `Optional<DocumentType> findByUuid(UUID uuid)`
    - `Optional<DocumentType> findByCode(String code)`
    - `Page<DocumentType> findAll(DocumentTypeFilterDto filter, Pageable pageable)`
    - `DocumentType save(DocumentType documentType)`
    - `void delete(UUID uuid)`
    - `boolean existsByCode(String code)`

---

### Fase 2: Esquema de Base de Datos y Migraciones (3 tareas)

**Propósito**: Crear schema de base de datos, índices y datos iniciales.

**Tareas**:

- [ ] **T006**: Crear migración inicial `V1.3__create_document_types_tables.sql`
  ```sql
  CREATE TABLE document_types (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      uuid CHAR(36) NOT NULL UNIQUE,
      code VARCHAR(10) NOT NULL UNIQUE,
      name VARCHAR(200) NOT NULL,
      description VARCHAR(500),
      active BOOLEAN DEFAULT TRUE,
      created_by BIGINT,
      updated_by BIGINT,
      deleted_by BIGINT,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      deleted_at TIMESTAMP NULL,
      INDEX idx_code (code),
      INDEX idx_uuid (uuid),
      INDEX idx_active (active),
      INDEX idx_deleted_at (deleted_at)
  );
  ```
  - Ejecutar y verificar en base de datos local

- [ ] **T007**: Crear migración de datos `V1.4__insert_colombia_document_types.sql`
  - Insertar 6 tipos de documento de Colombia:
    - NIT - Número de Identificación Tributaria
    - CC - Cédula de Ciudadanía
    - CE - Cédula de Extranjería
    - PA - Pasaporte
    - TI - Tarjeta de Identidad
    - RC - Registro Civil
  - Cada uno con UUID generado, active=true, timestamps
  - Verificar carga exitosa

- [ ] **T008**: Configurar Flyway en `application.properties`
  - `spring.flyway.enabled=true`
  - `spring.flyway.baseline-on-migrate=true`
  - `spring.flyway.locations=classpath:db/migration`
  - Verificar ejecución automática al iniciar aplicación

---

### Fase 3: Capa de Persistencia (Adaptadores de Salida) (4 tareas)

**Propósito**: Implementar capa de persistencia con JPA repositories y adapters.

**Tareas**:

- [ ] **T009**: Crear `DocumentTypeEntity` en `infrastructure/out/persistence/entity/document-types/`
  - Mapear tabla `document_types` con JPA annotations
  - Include soft delete: `@Where(clause = "deleted_at IS NULL")`
  - Implementar `@PreUpdate` para actualizar `updated_at`
  - Relaciones con User (createdBy, updatedBy, deletedBy) - opcional por ahora

- [ ] **T010**: Crear JPA Repository en `infrastructure/out/persistence/` (sin carpeta feature, es interfaz JPA)
  ```java
  public interface DocumentTypeJpaRepository extends 
      JpaRepository<DocumentTypeEntity, Long>, 
      JpaSpecificationExecutor<DocumentTypeEntity> {
      
      Optional<DocumentTypeEntity> findByUuid(UUID uuid);
      Optional<DocumentTypeEntity> findByCode(String code);
      boolean existsByCode(String code);
  }
  ```

- [ ] **T011**: Crear `DocumentTypeEntityMapper` con MapStruct en `infrastructure/out/persistence/mapper/document-types/`
  - Mapear `DocumentType` (domain) ↔ `DocumentTypeEntity` (persistence)
  - Manejar conversiones de tipos (UUID, LocalDateTime)
  - Tests unitarios de mapeo bidireccional

- [ ] **T012**: Implementar `DocumentTypeRepositoryAdapter` en `infrastructure/out/persistence/adapter/document-types/`
  - Implementa `DocumentTypeRepository` (output port)
  - Usa `DocumentTypeJpaRepository` y `DocumentTypeEntityMapper`
  - Implementar filtrado dinámico con `DocumentTypeSpecificationUtil`
  - Métodos: save, findByUuid, findByCode, findAll, delete, existsByCode
  - Tests de integración con Testcontainers > 80%

- [ ] **T013**: Crear `DocumentTypeSpecificationUtil` en `infrastructure/out/persistence/util/document-types/`
  - Métodos estáticos para crear Specifications:
    - `byActive(Boolean active)`
    - `bySearchText(String search)` - buscar en code, name, description
    - `combine(List<Specification> specs)` - combinar múltiples specs
  - Tests unitarios

---

### Fase 4: Capa de Aplicación - Casos de Uso (3 tareas)

**Propósito**: Implementar casos de uso del módulo siguiendo patrón CQRS.

**Tareas**:

- [ ] **T014**: Crear input ports en `application/port/in/document-types/`
  
  **CompareDocumentTypesUseCase.java** (Queries):
  ```java
  public interface CompareDocumentTypesUseCase {
      DocumentType getById(UUID uuid);
      DocumentType getByCode(String code);
      Page<DocumentType> listAll(DocumentTypeFilterDto filter, Pageable pageable);
      List<DocumentType> findActiveTypes();
  }
  ```
  
  **ManageDocumentTypeUseCase.java** (Commands):
  ```java
  public interface ManageDocumentTypeUseCase {
      DocumentType create(DocumentType documentType, Long userId);
      DocumentType update(UUID uuid, DocumentType updates, Long userId);
      void deactivate(UUID uuid, Long userId);
      void delete(UUID uuid, Long userId);
      void activate(UUID uuid, Long userId);
  }
  ```

- [ ] **T015**: Implementar `CompareDocumentTypesUseCaseImpl` en `application/usecase/document-types/`
  - Inyectar `DocumentTypeRepository` (output port)
  - Implementar todos los métodos de query
  - `getById()`: lanzar `DocumentTypeNotFoundException` si no existe
  - `listAll()`: aplicar filtros con paginación
  - Tests unitarios con Mockito > 85%

- [ ] **T016**: Implementar `ManageDocumentTypeUseCaseImpl` en `application/usecase/document-types/`
  - Inyectar `DocumentTypeRepository`, `DocumentTypeDomainService`, `DocumentTypeValidator`
  - **create()**:
    - Validar con `DocumentTypeValidator`
    - Normalizar código con `DocumentTypeDomainService`
    - Validar unicidad de código
    - Generar UUID
    - Establecer createdBy y createdAt
    - Guardar en repositorio
  - **update()**:
    - Buscar existente por UUID
    - Validar cambios
    - Si cambia código, validar unicidad
    - Establecer updatedBy y updatedAt
    - Guardar
  - **deactivate()**:
    - Buscar por UUID
    - Verificar con `canDeactivate()`
    - Llamar `documentType.deactivate()`
    - Guardar
  - **delete()** (soft delete):
    - Buscar por UUID
    - Verificar con `canDelete()`
    - Llamar `documentType.markAsDeleted(userId)`
    - Guardar
  - Tests unitarios con Mockito > 85%

---

### Phase 5: Exception Handling (3 tasks)

**Purpose**: Implementar manejo centralizado de excepciones con formato estándar.

**Tasks**:

- [ ] **T017**: Crear DTOs para respuestas de error en `infrastructure/in/web/dto/`
  - `ErrorResponseDto`: message (String), error (String)
  - Seguir formato estándar: solo 2 campos
  - Para múltiples errores de validación, concatenar mensajes con "; "
  - Códigos de error: VALIDATION_ERROR, RESOURCE_NOT_FOUND, DUPLICATE_CODE, BUSINESS_RULE_VIOLATION, INTERNAL_SERVER_ERROR
  - Tests unitarios de serialización

- [ ] **T018**: Implementar `GlobalExceptionHandler` en `infrastructure/in/web/advice/`
  - `@ControllerAdvice` para manejo global
  - **Excepciones del dominio**:
    - `@ExceptionHandler(DocumentTypeNotFoundException.class)` → 404, RESOURCE_NOT_FOUND
    - `@ExceptionHandler(DuplicateCodeException.class)` → 409, DUPLICATE_CODE
    - `@ExceptionHandler(InvalidDocumentTypeException.class)` → 400, VALIDATION_ERROR
  - **Excepciones de validación**:
    - `@ExceptionHandler(MethodArgumentNotValidException.class)` → 400, VALIDATION_ERROR
    - Concatenar múltiples errores con "; "
    - `@ExceptionHandler(ConstraintViolationException.class)` → 400, VALIDATION_ERROR
  - **Excepción genérica**:
    - `@ExceptionHandler(Exception.class)` → 500, INTERNAL_SERVER_ERROR
  - Logging: WARN para 4xx, ERROR para 5xx
  - Tests unitarios > 90%

- [ ] **T019**: Crear tests de integración de manejo de errores
  - Test de cada código HTTP con MockMvc
  - Verificar estructura de ErrorResponseDto (message y error)
  - Verificar concatenación de errores múltiples
  - Verificar logging apropiado
  - Cobertura > 90%

---

### Fase 6: Capa Web - Controladores y DTOs (Adaptadores de Entrada) (4 tareas)

**Propósito**: Implementar endpoints REST con validaciones y DTOs.

**Tareas**:

- [ ] **T020**: Crear DTOs en `infrastructure/in/web/dto/document-types/`
  - `DocumentTypeDto`: DTO base con todos los campos
  - `CreateDocumentTypeRequestDto`: code, name, description con validaciones
  - `UpdateDocumentTypeRequestDto`: name, description (code no se puede cambiar)
  - `DocumentTypeResponseDto`: todos los campos incluyendo audit info
  - `DocumentTypeComparisonDto`: para comparaciones entre tipos
  - `DocumentTypeFilterDto`: enabled, search, page, limit, fields[], sort, populate[], filters
  - `SortDto`: field, order (ASC/DESC)
  - `PagedResponseDto<T>`: content[], totalElements, totalPages, currentPage, pageSize
  - Bean validation annotations (@NotBlank, @Size, @Min, @Max)
  - Validación: limit max 100, page >= 0

- [ ] **T021**: Crear mappers con MapStruct en `infrastructure/in/web/mapper/document-types/`
  - `DocumentTypeDtoMapper`: DTOs ↔ domain models
    - `toDto(DocumentType domain)`
    - `toDomain(CreateDocumentTypeRequestDto dto)`
    - `updateDomainFromDto(UpdateDocumentTypeRequestDto dto, DocumentType domain)`
    - `toResponseDto(DocumentType domain)`
  - `DocumentTypeComparisonDtoMapper`: para comparaciones
  - Tests unitarios de mapeo

- [ ] **T022**: Implementar `DocumentTypeController` en `infrastructure/in/web/controller/document-types/`
  - **POST /api/document-types**
    - Inyectar `ManageDocumentTypeUseCase`, mappers
    - Validar request con @Valid
    - Llamar `manageUseCase.create()`
    - Retornar 201 Created con header Location
  - **GET /api/document-types/{uuid}**
    - Inyectar `CompareDocumentTypesUseCase`
    - Llamar `compareUseCase.getById()`
    - Retornar 200 OK
  - **GET /api/document-types** con query params
    - Recibir `DocumentTypeFilterDto` con @ModelAttribute
    - Validar parámetros (limit <= 100)
    - Llamar `compareUseCase.listAll()`
    - Retornar PagedResponseDto
  - **PUT /api/document-types/{uuid}**
    - Validar request con @Valid
    - Llamar `manageUseCase.update()`
    - Retornar 200 OK
  - **PATCH /api/document-types/{uuid}/deactivate**
    - Llamar `manageUseCase.deactivate()`
    - Retornar 200 OK
  - **DELETE /api/document-types/{uuid}**
    - Llamar `manageUseCase.delete()` (soft delete)
    - Retornar 204 No Content
  - Usar @ResponseStatus annotations apropiadas

- [ ] **T023**: Configurar dependency injection en `infrastructure/config/BeanConfiguration.java`
  - `@Configuration` class
  - Beans para use cases:
    ```java
    @Bean
    public CompareDocumentTypesUseCase compareDocumentTypesUseCase(
        DocumentTypeRepository repository) {
        return new CompareDocumentTypesUseCaseImpl(repository);
    }
    
    @Bean
    public ManageDocumentTypeUseCase manageDocumentTypeUseCase(
        DocumentTypeRepository repository,
        DocumentTypeDomainService domainService,
        DocumentTypeValidator validator) {
        return new ManageDocumentTypeUseCaseImpl(repository, domainService, validator);
    }
    ```
  - Bean para domain service y validator
    - `toDto(DocumentType domain)`
    - `toDomain(CreateDocumentTypeRequestDto dto)`
    - `updateDomainFromDto(UpdateDocumentTypeRequestDto dto, DocumentType domain)`
    - `toResponseDto(DocumentType domain)`
  - `DocumentTypeComparisonDtoMapper`: para comparaciones
  - Tests unitarios de mapeo

- [ ] **T022**: Implementar `DocumentTypeController` en `infrastructure/in/web/controller/`
  - **POST /api/document-types**
    - Inyectar `ManageDocumentTypeUseCase`, mappers
    - Validar request con @Valid
    - Llamar `manageUseCase.create()`
    - Retornar 201 Created con header Location
  - **GET /api/document-types/{uuid}**
    - Inyectar `CompareDocumentTypesUseCase`
    - Llamar `compareUseCase.getById()`
    - Retornar 200 OK
  - **GET /api/document-types** con query params
    - Recibir `DocumentTypeFilterDto` con @ModelAttribute
    - Validar parámetros (limit <= 100)
    - Llamar `compareUseCase.listAll()`
    - Retornar PagedResponseDto
  - **PUT /api/document-types/{uuid}**
    - Validar request con @Valid
    - Llamar `manageUseCase.update()`
    - Retornar 200 OK
  - **PATCH /api/document-types/{uuid}/deactivate**
    - Llamar `manageUseCase.deactivate()`
    - Retornar 200 OK
  - **DELETE /api/document-types/{uuid}**
    - Llamar `manageUseCase.delete()` (soft delete)
    - Retornar 204 No Content
  - Usar @ResponseStatus annotations apropiadas

- [ ] **T023**: Configurar dependency injection en `infrastructure/config/BeanConfiguration.java`
  - `@Configuration` class
  - Beans para use cases:
    ```java
    @Bean
    public CompareDocumentTypesUseCase compareDocumentTypesUseCase(
        DocumentTypeRepository repository) {
        return new CompareDocumentTypesUseCaseImpl(repository);
    }
    
    @Bean
    public ManageDocumentTypeUseCase manageDocumentTypeUseCase(
        DocumentTypeRepository repository,
        DocumentTypeDomainService domainService,
        DocumentTypeValidator validator) {
        return new ManageDocumentTypeUseCaseImpl(repository, domainService, validator);
    }
    ```
  - Bean para domain service y validator

---

### Phase 7: Testing & Quality (6 tasks)

**Purpose**: Asegurar cobertura de tests y calidad de código.

**Tasks**:

- [ ] **T024**: Tests unitarios de domain layer
  - `DocumentTypeTest`: métodos de negocio (activate, deactivate, markAsDeleted)
  - `DocumentTypeValidatorTest`: todas las validaciones
  - `DocumentTypeDomainServiceTest`: reglas de negocio
  - Cobertura > 90%

- [ ] **T025**: Tests unitarios de application layer
  - `CompareDocumentTypesUseCaseTest`: todos los métodos query con mocks
  - `ManageDocumentTypeUseCaseTest`: todos los comandos con mocks
  - Casos exitosos y de error
  - Verificar interacciones con repository
  - Cobertura > 85%

- [ ] **T026**: Tests de integración de persistence layer
  - `DocumentTypeRepositoryAdapterTest` con Testcontainers
  - Verificar queries, filtros, soft delete
  - Test de especificaciones dinámicas
  - Cobertura > 80%

- [ ] **T027**: Tests de mappers
  - `DocumentTypeDtoMapperTest`: bidireccionalidad, null handling
  - `DocumentTypeEntityMapperTest`: conversiones de tipos
  - `DocumentTypeComparisonDtoMapperTest`: comparaciones
  - Cobertura > 85%

- [ ] **T028**: Tests de integración de API layer
  - `DocumentTypeControllerTest` con MockMvc
  - **Validación de códigos HTTP**:
    - POST exitoso → 201 Created con header Location
    - GET exitoso → 200 OK
    - PUT exitoso → 200 OK
    - DELETE exitoso → 204 No Content
    - PATCH exitoso → 200 OK
    - Validación fallida → 400 Bad Request
    - Recurso no encontrado → 404 Not Found
    - Código duplicado → 409 Conflict
    - Restricción de negocio → 422 Unprocessable Entity
  - **Validación de formato de error**: ErrorResponseDto con message y error
  - **Tests de filtros avanzados**:
    - enabled, search, page, limit, sort, fields, populate
    - Validación de límite máximo (limit > 100 → 400)
    - Combinaciones múltiples
  - Validación de metadata de paginación
  - Cobertura > 80%

- [ ] **T029**: Tests E2E
  - Flujo completo: crear → consultar → actualizar → desactivar → eliminar
  - Verificar filtros y búsqueda
  - Verificar soft delete
  - Tests con datos precargados de Colombia

---

### Phase 8: Documentation & Deployment (3 tasks)

**Purpose**: Completar documentación y análisis de calidad.

**Tasks**:

- [ ] **T030**: Configurar OpenAPI/Swagger
  - Annotations en controller (@Operation, @ApiResponse)
  - Documentar códigos HTTP para cada endpoint
  - Ejemplos de request/response
  - Ejemplos de ErrorResponseDto
  - Documentar query params con validaciones
  - Tags y descripciones

- [ ] **T031**: Documentación técnica
  - README del módulo con:
    - Arquitectura hexagonal explicada
    - Estructura de carpetas
    - Ejemplos de uso de API
    - Documentación de códigos HTTP y errores
    - Guía de testing
  - Actualizar STATUS.md con progreso

- [ ] **T032**: Code quality & security scan
  - SonarLint analysis sin critical issues
  - Verificar cobertura de tests > 80%
  - Dependency check
  - Security scan
  - Performance profiling

---

## HTTP Status Codes Reference

### Success Responses
- **200 OK**: GET (retrieve), PUT (update), PATCH (partial update)
- **201 Created**: POST (create) - incluye header `Location`
- **204 No Content**: DELETE (soft delete)

### Client Error Responses (4xx)
- **400 Bad Request**: Validaciones fallidas, parámetros inválidos
- **404 Not Found**: Recurso no existe por UUID
- **409 Conflict**: Código duplicado
- **422 Unprocessable Entity**: Restricciones de negocio

### Server Error Responses (5xx)
- **500 Internal Server Error**: Errores inesperados

### Error Response Format

```json
{
  "message": "Descripción clara del error",
  "error": "CODIGO_ERROR"
}
```

---

## Testing Strategy Summary

### Unit Tests
- **Domain**: tests puros sin mocks (> 90% coverage)
- **Application**: mocks de repositories y services (> 85% coverage)
- **Mappers**: bidireccionalidad y edge cases (> 85% coverage)

### Integration Tests
- **Persistence**: Testcontainers MySQL (> 80% coverage)
- **API**: MockMvc con verificación de JSON (> 80% coverage)
- **Exception Handling**: todos los códigos HTTP (> 90% coverage)

### E2E Tests
- Flujos completos de negocio
- Verificación de soft delete
- Datos precargados de Colombia

---

## Dependencies & Integration

### Module Dependencies
- **FROM**: Ninguno (módulo base independiente)
- **TO**: Contact, User, Company (futuros consumidores)

### External Dependencies
```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- MapStruct -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Risk Assessment

### Technical Risks
1. **RISK**: Códigos duplicados por case sensitivity
   - **Mitigation**: Normalizar a mayúsculas, validación case-insensitive
   - **Impact**: MEDIUM, **Probability**: MEDIUM

2. **RISK**: Soft delete no respetado en queries
   - **Mitigation**: `@Where` clause en entity, tests exhaustivos
   - **Impact**: MEDIUM, **Probability**: LOW

3. **RISK**: Complejidad de filtros dinámicos con Specifications
   - **Mitigation**: Utility class bien testeada, documentación clara
   - **Impact**: LOW, **Probability**: MEDIUM

### Business Risks
1. **RISK**: Necesidad de tipos de documento de otros países
   - **Mitigation**: Arquitectura extensible, campo country futuro
   - **Impact**: LOW, **Probability**: HIGH

---

## Performance Considerations

### Optimizations
- Índices en code (unique), uuid (unique), active, deleted_at
- Paginación obligatoria en listados
- Soft delete con `@Where` clause
- Lazy loading de relaciones audit

### Benchmarks
- GET /api/document-types: < 100ms p95
- POST /api/document-types: < 150ms p95
- PUT /api/document-types: < 150ms p95

---

## Acceptance Criteria Checklist

- [ ] Migrations ejecutan sin errores
- [ ] 6 tipos de documento de Colombia cargados
- [ ] CRUD completo funcional con códigos HTTP apropiados
- [ ] Formato ErrorResponseDto estándar en todas las respuestas de error
- [ ] Filtros avanzados funcionan (enabled, search, page, limit, sort, fields, populate)
- [ ] Metadata de paginación correcta
- [ ] Tests unitarios > 80% cobertura (domain y application)
- [ ] Tests de integración pasan
- [ ] Swagger documentation completa
- [ ] No issues críticos en SonarLint
- [ ] Performance benchmarks cumplidos
- [ ] Arquitectura hexagonal correctamente implementada según scaffolding

---

## Documentación Relacionada

- Especificación Funcional: [functional-spec.md](1-functional-spec.md)
- Especificación Técnica: [technical-spec.md](2-technical-spec.md)
- Seguimiento de Tareas: [tasks.json](4-tasks.json)
- Estado: [STATUS.md](status.md)
- Referencia de Scaffolding: [../../scaffolding.md](../../scaffolding.md)

