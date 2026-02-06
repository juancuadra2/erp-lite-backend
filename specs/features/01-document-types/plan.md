# Implementation Plan: Módulo de Tipos de Documento (Catálogos Base)

**Date**: January 10, 2026
**Spec**: [document-types-spec.md](document-types-spec.md)

## Summary

Desarrollo del módulo de catálogos de tipos de documentos de identificación que gestiona los diferentes tipos de documentos (NIT, CC, CE, Pasaporte, etc.) utilizados para identificar personas naturales y jurídicas. Este módulo no tiene dependencias de otros módulos y es requerido por Contact, User y Company. Incluye carga masiva inicial de datos de Colombia (6 tipos de documento), endpoints REST CRUD completos, y gestión de auditoría.

## Technical Context

**Languages/Versions**: Java 17+
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, MySQL Connector, Lombok, MapStruct, Hibernate Validator, Flyway
**Storage**: MySQL 8.0+
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers
**Target Platforms**: RESTful API
**Performance Goals**: Listados paginados < 100ms p95, operaciones CRUD < 150ms p95
**Constraints**: Soft delete obligatorio, auditoría completa
**Scale/Scope**: Single-tenant, catálogo base sin dependencias

## Project Structure

Estructura siguiendo arquitectura hexagonal dentro del módulo document-types:

```text
src/
├── main/
│   ├── java/
│   │   └── com/jcuadrado/erplitebackend/
│   │       │
│   │       ├── domain/
│   │       │   └── documenttype/                        # DOMAIN LAYER
│   │       │       ├── model/
│   │       │       │   └── DocumentType.java            # Aggregate Root
│   │       │       ├── service/
│   │       │       │   └── DocumentTypeDomainService.java  # Business rules
│   │       │       └── exception/
│   │       │           ├── DocumentTypeNotFoundException.java
│   │       │           ├── DuplicateCodeException.java
│   │       │           └── DocumentTypeConstraintException.java
│   │       │
│   │       ├── application/
│   │       │   └── port/
│   │       │       ├── in/                              # INPUT PORTS (Use Cases)
│   │       │       │   ├── documenttype/
│   │       │       │   │   ├── CreateDocumentTypeUseCase.java
│   │       │       │   │   ├── GetDocumentTypeUseCase.java
│   │       │       │   │   ├── UpdateDocumentTypeUseCase.java
│   │       │       │   │   ├── DeactivateDocumentTypeUseCase.java
│   │       │       │   │   └── ListDocumentTypesUseCase.java
│   │       │       └── out/                             # OUTPUT PORTS (Repository Interfaces)
│   │       │           └── DocumentTypePort.java
│   │       │
│   │       └── infrastructure/
│   │           ├── out/                                 # OUTPUT ADAPTERS
│   │           │   └── documenttype/
│   │           │       ├── persistence/
│   │           │       │   ├── entity/
│   │           │       │   │   └── DocumentTypeEntity.java
│   │           │       │   ├── repository/
│   │           │       │   │   └── DocumentTypeJpaRepository.java
│   │           │       │   └── adapter/
│   │           │       │       └── DocumentTypeRepositoryAdapter.java
│   │           │       └── mapper/
│   │           │           └── DocumentTypeEntityMapper.java
│   │           │
│   │           └── in/                                  # INPUT ADAPTERS
│   │               └── api/
│   │                   └── documenttype/
│   │                       ├── rest/
│   │                       │   └── DocumentTypeController.java
│   │                       ├── dto/
│   │                       │   ├── CreateDocumentTypeRequestDto.java
│   │                       │   ├── UpdateDocumentTypeRequestDto.java
│   │                       │   ├── DocumentTypeResponseDto.java
│   │                       │   ├── DocumentTypeFilterDto.java
│   │                       │   ├── SortDto.java
│   │                       │   └── PagedResponseDto.java
│   │                       ├── mapper/
│   │                       │   └── DocumentTypeDtoMapper.java
│   │                       └── constant/
│   │                           └── DocumentTypeApiConstants.java
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V1.3__create_document_types_tables.sql
│               └── V1.4__insert_colombia_document_types.sql
│
└── test/
    └── java/
        └── com/jcuadrado/erplitebackend/
            └── documenttype/
                ├── domain/
                │   ├── DocumentTypeTest.java
│               └── DocumentTypeDomainServiceTest.java
                ├── application/
│               ├── CreateDocumentTypeUseCaseTest.java
│               ├── UpdateDocumentTypeUseCaseTest.java
                │   └── ListDocumentTypesUseCaseTest.java
                └── infrastructure/
                    ├── api/
                    │   └── DocumentTypeControllerTest.java
                    └── persistence/
                        └── DocumentTypeRepositoryTest.java
```

---

## Implementation Phases

### Phase 1: Foundation & Domain Models (4 tasks)

**Purpose**: Establecer domain models, value objects y excepciones del módulo.

**Tasks**:

- [ ] **T001**: Crear entidad de dominio `DocumentType` en `domain/documenttype/model/`
  - Atributos según spec (id, uuid, code, name, description, active, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt)
  - Métodos de negocio: `activate()`, `deactivate()`, `isActive()`
  - Sin anotaciones JPA (domain puro)

- [ ] **T002**: Crear excepciones específicas del dominio en `domain/documenttype/exception/`
  - `DocumentTypeNotFoundException extends DomainException`
  - `DuplicateCodeException extends BusinessRuleException`
  - `DocumentTypeConstraintException extends BusinessRuleException`

- [ ] **T003**: Implementar `DocumentTypeDomainService` con reglas de negocio
  - `validateCode(String code)`: validar formato (2-10 caracteres alfanuméricos)
  - `normalizeCode(String code)`: normalizar código a mayúsculas
  - Tests unitarios > 80%

---

### Phase 2: Database Schema & Migrations (3 tasks)

**Purpose**: Crear schema de base de datos, índices y datos iniciales.

**Tasks**:

- [ ] **T004**: Crear migración inicial `V1.3__create_document_types_tables.sql`
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
      INDEX idx_active (active)
  );
  ```
  - Ejecutar y verificar en base de datos local

- [ ] **T005**: Crear migración de datos `V1.4__insert_colombia_document_types.sql`
  - Insertar 6 tipos de documento de Colombia:
    - NIT - Número de Identificación Tributaria
    - CC - Cédula de Ciudadanía
    - CE - Cédula de Extranjería
    - PA - Pasaporte
    - TI - Tarjeta de Identidad
    - RC - Registro Civil
  - Verificar carga exitosa

- [ ] **T006**: Configurar Flyway en `application.properties`
  - `spring.flyway.enabled=true`
  - `spring.flyway.baseline-on-migrate=true`
  - Verificar ejecución automática al iniciar aplicación

---

### Phase 3: Persistence Layer (4 tasks)

**Purpose**: Implementar capa de persistencia con JPA repositories y adapters.

**Tasks**:

- [ ] **T007**: Crear `DocumentTypeEntity` en `infrastructure/out/documenttype/persistence/entity/`
  - Mapear tabla `document_types` con JPA annotations
  - Include soft delete: `@Where(clause = "deleted_at IS NULL")`
  - Relaciones con User (createdBy, updatedBy, deletedBy)

- [ ] **T008**: Crear `DocumentTypeJpaRepository` en `persistence/repository/`
  ```java
  public interface DocumentTypeJpaRepository extends JpaRepository<DocumentTypeEntity, Long>, JpaSpecificationExecutor<DocumentTypeEntity> {
      Optional<DocumentTypeEntity> findByUuid(UUID uuid);
      Optional<DocumentTypeEntity> findByCode(String code);
      Page<DocumentTypeEntity> findByActiveTrue(Pageable pageable);
      Page<DocumentTypeEntity> findAll(Pageable pageable);
      // JpaSpecificationExecutor permite usar Specifications para filtros dinámicos
  }
  ```
  - Agregar `DocumentTypeSpecification` para filtros dinámicos (enabled, search, filters)
  - Implementar lógica de búsqueda global con search en code, name, description

- [ ] **T009**: Crear `DocumentTypeEntityMapper` con MapStruct
  - Mapear `DocumentType` (domain) ↔ `DocumentTypeEntity` (persistence)
  - Tests unitarios de mapeo bidireccional

- [ ] **T010**: Implementar `DocumentTypeRepositoryAdapter`
  - Implementa `DocumentTypePort`
  - Usa `DocumentTypeJpaRepository` y `DocumentTypeEntityMapper`
  - Métodos: save, findByUuid, findByCode, findAll, delete
  - Tests de integración con Testcontainers

---

### Phase 4: Application Layer - Use Cases (5 tasks)

**Purpose**: Implementar casos de uso del módulo.

**Tasks**:

- [ ] **T011**: Implementar `CreateDocumentTypeUseCase`
  - Validar código único con `DocumentTypePort.findByCode()`
  - Normalizar código con `DocumentTypeDomainService`
  - Guardar en repositorio
  - Tests unitarios con Mockito

- [ ] **T012**: Implementar `GetDocumentTypeUseCase`
  - Buscar por UUID
  - Lanzar `DocumentTypeNotFoundException` si no existe
  - Tests unitarios

- [ ] **T013**: Implementar `UpdateDocumentTypeUseCase`
  - Validar código único (excluyendo el actual)
  - Actualizar campos modificables
  - Registrar auditoría
  - Tests unitarios

- [ ] **T014**: Implementar `ListDocumentTypesUseCase`
  - Filtros avanzados:
    - `enabled`: filtro booleano por estado activo
    - `search`: búsqueda global en code, name, description (case-insensitive, LIKE)
    - `page` y `limit`: paginación
    - `sort.field` y `sort.order`: ordenamiento dinámico
    - `filters`: mapa de filtros adicionales (ej: country, type)
  - Selección de campos con `fields` array
  - Populate de relaciones: createdBy, updatedBy, deletedBy
  - Respuesta con metadata de paginación: totalElements, totalPages, currentPage, pageSize
  - Tests unitarios con múltiples combinaciones de filtros

- [ ] **T015**: Implementar `DeactivateDocumentTypeUseCase`
  - Cambiar active a false
  - Registrar auditoría
  - Tests unitarios

---

### Phase 5: Exception Handling & Error Responses (3 tasks)

**Purpose**: Implementar manejo centralizado de excepciones con códigos HTTP apropiados y formato estándar de respuestas de error.

**Tasks**:

- [ ] **T016**: Crear DTOs para respuestas de error
  - `ErrorResponseDto`: message (String), error (String)
  - Seguir formato estándar definido en `framework/STANDARD_ERROR_FORMAT.md`
  - Usar en GlobalExceptionHandler para respuestas consistentes
  - Para múltiples errores de validación, concatenar mensajes con punto y coma (;)
  - Códigos de error estándar: VALIDATION_ERROR, RESOURCE_NOT_FOUND, DUPLICATE_CODE, BUSINESS_RULE_VIOLATION, INTERNAL_SERVER_ERROR
  - Tests unitarios de serialización

- [ ] **T017**: Implementar `GlobalExceptionHandler` con @ControllerAdvice
  - **Excepciones del dominio**:
    - `@ExceptionHandler(DocumentTypeNotFoundException.class)` → ResponseEntity con 404, error="RESOURCE_NOT_FOUND"
    - `@ExceptionHandler(DuplicateCodeException.class)` → ResponseEntity con 409, error="DUPLICATE_CODE"
    - `@ExceptionHandler(DocumentTypeConstraintException.class)` → ResponseEntity con 422, error="BUSINESS_RULE_VIOLATION"
  - **Excepciones de validación**:
    - `@ExceptionHandler(MethodArgumentNotValidException.class)` → ResponseEntity con 400, error="VALIDATION_ERROR"
    - Concatenar múltiples errores de validación con "; " en el campo message
    - `@ExceptionHandler(ConstraintViolationException.class)` → ResponseEntity con 400, error="VALIDATION_ERROR"
    - `@ExceptionHandler(HttpMessageNotReadableException.class)` → ResponseEntity con 400, error="VALIDATION_ERROR"
  - **Excepción genérica**:
    - `@ExceptionHandler(Exception.class)` → ResponseEntity con 500, error="INTERNAL_SERVER_ERROR"
  - Construir ErrorResponseDto con solo dos campos: message y error
  - Logging diferenciado: WARN para 4xx, ERROR para 5xx
  - NO incluir información sensible en respuestas 500
  - Tests unitarios para cada handler

- [ ] **T018**: Crear tests de integración de manejo de errores
  - Test de cada código HTTP con MockMvc
  - Verificar estructura de ErrorResponseDto en respuestas (solo message y error)
  - Verificar que message contiene información concatenada para múltiples errores de validación
  - Verificar que error contiene el código apropiado (VALIDATION_ERROR, RESOURCE_NOT_FOUND, etc.)
  - Verificar logging apropiado
  - Test de múltiples errores de validación simultáneos concatenados
  - Cobertura > 90%

---

### Phase 6: API Layer - Controllers & DTOs (4 tasks)

**Purpose**: Implementar endpoints REST con validaciones y DTOs.

**Tasks**:

- [ ] **T019**: Crear DTOs en `infrastructure/in/api/documenttype/dto/`
  - `CreateDocumentTypeRequestDto`: code, name, description
  - `UpdateDocumentTypeRequestDto`: name, description
  - `DocumentTypeResponseDto`: todos los campos incluyendo uuid, createdBy, updatedBy, deletedBy, timestamps
  - `DocumentTypeFilterDto`: enabled, search, page, limit, fields[], sort, populate[], filters (Map<String, Object>)
  - `SortDto`: field, order (ASC/DESC)
  - `PagedResponseDto<T>`: content[], totalElements, totalPages, currentPage, pageSize
  - Bean validation annotations (@NotBlank, @Size, @Min, @Max)
  - Validación: limit max 100, page >= 0

- [ ] **T020**: Crear `DocumentTypeDtoMapper` con MapStruct
  - Mapear DTOs ↔ domain models
  - Tests unitarios

- [ ] **T021**: Implementar `DocumentTypeController` endpoints CRUD con códigos HTTP apropiados
  - **POST** /api/document-types
    - Success: **201 Created** con header `Location`
    - Errors: 400 (validación), 409 (código duplicado)
  - **GET** /api/document-types/{uuid}
    - Success: **200 OK**
    - Errors: 404 (no encontrado)
  - **PUT** /api/document-types/{uuid}
    - Success: **200 OK**
    - Errors: 400 (validación), 404 (no encontrado), 409 (código duplicado)
  - **DELETE** /api/document-types/{uuid} (soft delete)
    - Success: **204 No Content**
    - Errors: 404 (no encontrado), 422 (en uso)
  - **PATCH** /api/document-types/{uuid}/deactivate
    - Success: **200 OK**
    - Errors: 404 (no encontrado), 422 (restricciones de negocio)
  - **GET** /api/document-types con query params:
    - Success: **200 OK**
    - Errors: 400 (parámetros inválidos)
    - `enabled`: Boolean (opcional)
    - `search`: String (opcional, búsqueda global)
    - `page`: Integer (default: 0, min: 0)
    - `limit`: Integer (default: 10, min: 1, max: 100)
    - `fields`: String[] (opcional, ej: id,uuid,code,name)
    - `sort.field`: String (default: id)
    - `sort.order`: String (ASC/DESC, default: ASC)
    - `populate`: String[] (createdBy, updatedBy, deletedBy)
    - `filters.*`: Map (ej: filters.country=CO, filters.type=admin)
  - Usar @RequestParam con @ModelAttribute para binding de filtros
  - Usar @ResponseStatus annotations apropiadas
  - Respuesta: PagedResponseDto con metadata
  - Incluir header Location en respuestas 201 Created

- [ ] **T022**: Integrar GlobalExceptionHandler con Controller
  - Verificar que todas las excepciones del dominio son capturadas
  - Validar códigos HTTP en cada endpoint
  - Tests de integración Controller + ExceptionHandler
  - Validar formato de ErrorResponseDto en todas las respuestas de error

---

### Phase 7: Testing & Quality (5 tasks)

**Purpose**: Asegurar cobertura de tests y calidad de código.

**Tasks**:

- [ ] **T023**: Tests unitarios de domain layer
  - `DocumentTypeTest`: métodos de negocio
  - `DocumentTypeDomainServiceTest`: validaciones
  - Cobertura > 90%

- [ ] **T024**: Tests unitarios de application layer
  - Todos los use cases con Mockito
  - Casos exitosos y de error
  - Cobertura > 85%

- [ ] **T025**: Tests de integración de persistence layer
  - `DocumentTypeRepositoryTest` con Testcontainers
  - Verificar queries, filtros, soft delete
  - Cobertura > 80%

- [ ] **T026**: Tests de integración de API layer
  - `DocumentTypeControllerTest` con MockMvc
  - Todos los endpoints con casos exitosos y errores
  - Validación de request/response JSON
  - **Validación específica de códigos HTTP**:
    - POST exitoso retorna 201 Created con header Location
    - GET exitoso retorna 200 OK
    - PUT exitoso retorna 200 OK
    - DELETE exitoso retorna 204 No Content
    - PATCH exitoso retorna 200 OK
    - Validación fallida retorna 400 Bad Request
    - Recurso no encontrado retorna 404 Not Found
    - Código duplicado retorna 409 Conflict
    - Restricción de negocio retorna 422 Unprocessable Entity
  - **Validación de formato de error**:
    - Verificar estructura de respuesta de error (timestamp, status, error, message, path)
    - Verificar array de errors en validaciones 400
    - Verificar objeto details en conflictos 409 y restricciones 422
  - Tests específicos de filtros avanzados:
    - Filtro por enabled (true/false)
    - Búsqueda global con search
    - Paginación (page, limit)
    - Validación de límite máximo (limit > 100 retorna 400)
    - Ordenamiento (sort.field, sort.order)
    - Selección de campos (fields)
    - Populate de relaciones (createdBy, updatedBy)
    - Filtros dinámicos (filters.country, filters.type)
    - Combinaciones de múltiples filtros
  - Validación de metadata de paginación
  - Cobertura > 80%

- [ ] **T027**: Tests E2E
  - Flujo completo: crear tipo → consultar → actualizar → desactivar
  - Verificar filtros por active

---

### Phase 8: Documentation & Code Quality (3 tasks)

**Purpose**: Completar documentación y análisis de calidad.

**Tasks**:

- [ ] **T028**: Configurar OpenAPI/Swagger
  - Annotations en controller
  - Ejemplos en DTOs
  - Tags y descripciones
  - **Documentar códigos HTTP para cada endpoint**:
    - @ApiResponse annotations con códigos de éxito y error
    - Ejemplos de respuestas exitosas
    - Ejemplos de respuestas de error (400, 404, 409, 422, 500)
    - Esquema de ErrorResponseDto documentado
  - Documentar query params con validaciones (min, max, default)
  - Incluir ejemplos de uso para cada endpoint

- [ ] **T029**: Documentación técnica
  - README del módulo
  - Ejemplos de uso de API
  - Documentación de códigos HTTP y manejo de errores
  - Ejemplos de respuestas de error para cada escenario

- [ ] **T030**: Code quality & security scan
  - SonarLint analysis sin critical issues
  - Dependency check
  - Security scan
  - Performance profiling

---

## HTTP Status Codes Reference

### Success Responses
- **200 OK**: GET (retrieve), PUT (update), PATCH (partial update)
- **201 Created**: POST (create) - incluye header `Location` con URI del recurso creado
- **204 No Content**: DELETE (soft delete) - sin cuerpo en la respuesta

### Client Error Responses (4xx)
- **400 Bad Request**: 
  - Validaciones de Bean Validation fallidas
  - Formato de datos inválido
  - Parámetros de query inválidos (ej: limit > 100, page < 0)
  - JSON malformado
  
- **404 Not Found**: 
  - GET /api/document-types/{uuid} donde uuid no existe
  - PUT /api/document-types/{uuid} donde uuid no existe
  - DELETE /api/document-types/{uuid} donde uuid no existe
  - PATCH /api/document-types/{uuid}/deactivate donde uuid no existe
  
- **409 Conflict**: 
  - POST con código que ya existe
  - PUT intentando cambiar a un código que ya existe
  
- **422 Unprocessable Entity**: 
  - Intentar desactivar tipo de documento con restricciones de negocio
  - Intentar eliminar tipo de documento en uso por otros módulos

### Server Error Responses (5xx)
- **500 Internal Server Error**: 
  - Excepciones no controladas
  - Errores de base de datos
  - Fallos de infraestructura

### Error Response Format

**NOTA**: Este formato sigue el estándar definido en `framework/STANDARD_ERROR_FORMAT.md`.

Todas las respuestas de error siguen este formato estándar simplificado:

```json
{
  "message": "Descripción clara del error para el usuario",
  "error": "CODIGO_ERROR"
}
```

**Campos:**
- **message**: Mensaje descriptivo del error. Para múltiples errores de validación, se concatenan con "; ".
- **error**: Código de error específico: VALIDATION_ERROR, RESOURCE_NOT_FOUND, DUPLICATE_CODE, BUSINESS_RULE_VIOLATION, INTERNAL_SERVER_ERROR

**Ejemplos:**
```json
// Validación fallida
{
  "message": "Code must be between 2 and 10 characters; Name is required",
  "error": "VALIDATION_ERROR"
}

// Recurso no encontrado
{
  "message": "Document type not found with uuid: 123e4567-e89b-12d3-a456-426614174000",
  "error": "RESOURCE_NOT_FOUND"
}

// Código duplicado
{
  "message": "Document type with code 'NIT' already exists",
  "error": "DUPLICATE_CODE"
}
```

---

## Testing Strategy

### Unit Tests
- **Domain**: tests puros sin mocks para lógica de negocio
- **Application**: mocks de ports, verificar orquestación
- **Mappers**: bidireccionalidad

### Integration Tests
- **Persistence**: Testcontainers MySQL
- **API**: MockMvc, verificar serialización JSON

### E2E Tests
- **Flujos completos**: crear → consultar → validar documento → calcular check digit

### Test Data
- 6 tipos de documento de Colombia precargados
- Casos de prueba:
  - Códigos válidos: "NIT", "CC", "CE"
  - Códigos inválidos: "X", "ABCDEFGHIJK" (muy largo)
  - Nombres válidos: "Cédula de Ciudadanía"
  - Soft delete funcional

---

## Dependencies & Integration

### Module Dependencies
- **FROM**: Ninguno (módulo base sin dependencias)
- **TO**: 
  - Contact (usa tipos de documento para contactos)
  - User (usa tipos de documento para usuarios)
  - Company (usa tipos de documento para empresas)

### External Dependencies
```xml
<!-- pom.xml additions -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
```

---

## Risk Assessment

### Technical Risks
1. **RISK**: Códigos duplicados por diferencias en mayúsculas/minúsculas
   - **Mitigation**: Normalizar siempre a mayúsculas antes de validar y guardar
   - **Impact**: MEDIUM
   - **Probability**: MEDIUM

2. **RISK**: Soft delete no respetado en queries
   - **Mitigation**: Usar @Where clause en entity, tests de integración
   - **Impact**: MEDIUM
   - **Probability**: LOW

### Business Risks
1. **RISK**: Tipos de documento de otros países no contemplados
   - **Mitigation**: Arquitectura extensible, documentación de cómo agregar nuevos tipos
   - **Impact**: LOW
   - **Probability**: HIGH

2. **RISK**: Validaciones de formato necesarias en el futuro
   - **Mitigation**: Modelo preparado para extensión, agregar campos cuando sea necesario
   - **Impact**: MEDIUM
   - **Probability**: MEDIUM

---

## Performance Considerations

### Optimizations
- Índices en code (unique), uuid (unique)
- Paginación por defecto en listados
- Soft delete con @Where clause para evitar filtrar en queries

### Benchmarks
- GET /api/document-types: < 100ms p95
- POST /api/document-types: < 150ms p95
- PUT /api/document-types: < 150ms p95

---

## Security Considerations

### Authentication & Authorization
- JWT requerido para todos los endpoints
- Solo ADMIN puede: CREATE, UPDATE, DELETE
- Lectura permitida para usuarios autenticados

### Input Validation
- Bean validation en DTOs
- Normalización de códigos a mayúsculas
- Límites de longitud en inputs

---

## Open Questions

1. **Q**: ¿Se requieren otros algoritmos de check digit para otros países?
   - **A**: Por ahora solo NIT Colombia, agregar bajo demanda

2. **Q**: ¿Se permite eliminar tipos de documento con documentos asociados?
   - **A**: Soft delete siempre, validación de referencias en módulos consumidores

3. **Q**: ¿Se requiere versionado de tipos de documento?
   - **A**: No por ahora, updatedAt suficiente para auditoría

---

## Acceptance Criteria Checklist

- [ ] Migrations ejecutan sin errores
- [ ] 6 tipos de documento de Colombia cargados
- [ ] CRUD completo funcional con códigos HTTP apropiados:
  - [ ] POST retorna 201 Created con header Location
  - [ ] GET retorna 200 OK o 404 Not Found
  - [ ] PUT retorna 200 OK o 404 Not Found
  - [ ] DELETE retorna 204 No Content o 404 Not Found
  - [ ] PATCH retorna 200 OK o 404 Not Found
- [ ] Manejo de errores implementado:
  - [ ] 400 Bad Request para validaciones fallidas
  - [ ] 404 Not Found para recursos inexistentes
  - [ ] 409 Conflict para códigos duplicados
  - [ ] 422 Unprocessable Entity para restricciones de negocio
  - [ ] 500 Internal Server Error para errores inesperados
- [ ] Formato estándar de ErrorResponseDto en todas las respuestas de error
- [ ] Filtros avanzados funcionan:
  - [ ] enabled (Boolean)
  - [ ] search (búsqueda global)
  - [ ] page y limit (paginación)
  - [ ] sort.field y sort.order (ordenamiento)
  - [ ] fields (selección de campos)
  - [ ] populate (relaciones)
  - [ ] filters (filtros dinámicos)
- [ ] Metadata de paginación correcta
- [ ] Tests unitarios > 80% cobertura
- [ ] Tests de integración pasan (incluyendo códigos HTTP)
- [ ] Swagger documentation completa (incluyendo respuestas de error)
- [ ] No hay issues críticos en SonarLint
- [ ] Performance benchmarks cumplidos
