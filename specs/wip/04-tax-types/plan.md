# Implementation Plan: Módulo de Tipos de Impuestos

**Date**: February 1, 2026  
**Spec**: [functional-spec.md](functional-spec.md) | [technical-spec.md](technical-spec.md)

## Summary

Desarrollo del módulo de catálogo de tipos de impuestos que gestiona los impuestos aplicables en transacciones comerciales (IVA, ReteFuente, ReteIVA, ICA, etc.). Este módulo no tiene dependencias de otros módulos y es requerido por Products, Sales y Purchases para cálculo de impuestos y cumplimiento tributario. Incluye carga masiva inicial de datos de Colombia (7 tipos de impuestos), endpoints REST CRUD completos, búsqueda y filtrado por aplicación, y validaciones de integridad.

## Technical Context

**Languages/Versions**: Java 17+  
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, MySQL Connector, Lombok, MapStruct, Hibernate Validator, Flyway  
**Storage**: MySQL 8.0+  
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers  
**Target Platforms**: RESTful API  
**Performance Goals**: List operations < 100ms p95  
**Constraints**: Soft delete obligatorio, auditoría completa, precisión decimal para porcentajes  
**Scale/Scope**: Single-tenant, catálogo base sin dependencias, ~10-20 tipos de impuestos

## Project Structure

Estructura siguiendo arquitectura hexagonal dentro del módulo taxtype:

```text
src/
├── main/
│   ├── java/
│   │   └── com/jcuadrado/erplitebackend/
│   │       │
│   │       ├── domain/
│   │       │   └── taxtype/                              # DOMAIN LAYER
│   │       │       ├── model/
│   │       │       │   ├── TaxType.java                  # Aggregate Root
│   │       │       │   └── TaxApplicationType.java       # Enum
│   │       │       ├── service/
│   │       │       │   ├── TaxTypeDomainService.java     # Business rules
│   │       │       │   └── TaxTypeValidationService.java # Validations
│   │       │       └── exception/
│   │       │           ├── TaxTypeNotFoundException.java
│   │       │           ├── DuplicateTaxTypeCodeException.java
│   │       │           ├── InvalidTaxTypeCodeException.java
│   │       │           ├── InvalidTaxPercentageException.java
│   │       │           ├── InvalidTaxTypeDataException.java
│   │       │           └── TaxTypeConstraintException.java
│   │       │
│   │       ├── application/
│   │       │   ├── port/
│   │       │   │   ├── in/                               # INPUT PORTS (Use Cases)
│   │       │   │   │   └── taxtype/
│   │       │   │   │       ├── CreateTaxTypeUseCase.java
│   │       │   │   │       ├── GetTaxTypeUseCase.java
│   │       │   │   │       ├── UpdateTaxTypeUseCase.java
│   │       │   │   │       ├── DeactivateTaxTypeUseCase.java
│   │       │   │   │       ├── ActivateTaxTypeUseCase.java
│   │       │   │   │       ├── ListTaxTypesUseCase.java
│   │       │   │   │       └── SearchTaxTypesUseCase.java
│   │       │   │   └── out/                              # OUTPUT PORTS
│   │       │   │       └── TaxTypePort.java              # Repository Interface
│   │       │   └── service/
│   │       │       └── taxtype/                          # USE CASE IMPLEMENTATIONS
│   │       │           ├── CreateTaxTypeService.java
│   │       │           ├── GetTaxTypeService.java
│   │       │           ├── UpdateTaxTypeService.java
│   │       │           ├── DeactivateTaxTypeService.java
│   │       │           ├── ActivateTaxTypeService.java
│   │       │           ├── ListTaxTypesService.java
│   │       │           └── SearchTaxTypesService.java
│   │       │
│   │       └── infrastructure/
│   │           ├── out/                                  # OUTPUT ADAPTERS
│   │           │   └── taxtype/
│   │           │       ├── persistence/
│   │           │       │   ├── entity/
│   │           │       │   │   └── TaxTypeEntity.java
│   │           │       │   ├── repository/
│   │           │       │   │   └── TaxTypeJpaRepository.java
│   │           │       │   └── adapter/
│   │           │       │       └── TaxTypeRepositoryAdapter.java
│   │           │       └── mapper/
│   │           │           └── TaxTypeEntityMapper.java
│   │           │
│   │           └── in/                                   # INPUT ADAPTERS
│   │               └── api/
│   │                   └── taxtype/
│   │                       ├── rest/
│   │                       │   └── TaxTypeController.java
│   │                       ├── dto/
│   │                       │   ├── CreateTaxTypeRequestDto.java
│   │                       │   ├── UpdateTaxTypeRequestDto.java
│   │                       │   └── TaxTypeResponseDto.java
│   │                       ├── mapper/
│   │                       │   └── TaxTypeDtoMapper.java
│   │                       └── constant/
│   │                           └── TaxTypeApiConstants.java
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V1.3__create_tax_types_table.sql
│               └── V1.4__insert_colombia_tax_types.sql
│
└── test/
    └── java/
        └── com/jcuadrado/erplitebackend/
            └── taxtype/
                ├── domain/
                │   ├── model/
                │   │   └── TaxTypeTest.java
                │   └── service/
                │       ├── TaxTypeDomainServiceTest.java
                │       └── TaxTypeValidationServiceTest.java
                ├── application/
                │   └── service/
                │       ├── CreateTaxTypeServiceTest.java
                │       ├── GetTaxTypeServiceTest.java
                │       ├── UpdateTaxTypeServiceTest.java
                │       ├── DeactivateTaxTypeServiceTest.java
                │       ├── ListTaxTypesServiceTest.java
                │       └── SearchTaxTypesServiceTest.java
                └── infrastructure/
                    ├── in/
                    │   └── api/
                    │       └── rest/
                    │           └── TaxTypeControllerTest.java
                    └── out/
                        └── persistence/
                            ├── repository/
                            │   └── TaxTypeJpaRepositoryTest.java
                            └── adapter/
                                └── TaxTypeRepositoryAdapterTest.java
```

---

## Implementation Phases

### Phase 1: Foundation & Domain Models (3 tasks)

**Purpose**: Establecer domain models, enums y excepciones del módulo.

**Tasks**:

- [ ] **T001**: Crear entidad de dominio `TaxType` con atributos completos en `domain/taxtype/model/`
  - Atributos: id, uuid, code, name, percentage, isIncluded, applicationType, description, enabled, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt
  - Métodos de negocio: `activate()`, `deactivate()`, `isApplicableForSales()`, `isApplicableForPurchases()`, `isValidPercentage()`
  - Sin anotaciones JPA (domain puro)
  - Usar BigDecimal para percentage con precisión de 4 decimales

- [ ] **T002**: Crear enum `TaxApplicationType` en `domain/taxtype/model/`
  - Valores: SALE, PURCHASE, BOTH
  - Sin anotaciones de persistencia

- [ ] **T003**: Crear excepciones específicas del dominio en `domain/taxtype/exception/`
  - `TaxTypeNotFoundException extends DomainException`
  - `DuplicateTaxTypeCodeException extends BusinessRuleException`
  - `InvalidTaxTypeCodeException extends BusinessRuleException`
  - `InvalidTaxPercentageException extends BusinessRuleException`
  - `InvalidTaxTypeDataException extends BusinessRuleException`
  - `TaxTypeConstraintException extends BusinessRuleException`

---

### Phase 2: Domain Services (2 tasks)

**Purpose**: Implementar servicios de dominio con reglas de negocio y validaciones.

**Tasks**:

- [ ] **T004**: Implementar `TaxTypeDomainService` en `domain/taxtype/service/`
  - Método `validateCode(String code)`: Validar formato de código (máx 20 chars, solo uppercase, números, puntos, guiones)
  - Método `validatePercentage(BigDecimal percentage)`: Validar rango 0-100, máx 4 decimales
  - Método `validateName(String name)`: Validar no vacío, máx 100 chars
  - Método `canBeDeleted(TaxType, long productsCount)`: Retornar si puede ser eliminado

- [ ] **T005**: Implementar `TaxTypeValidationService` en `domain/taxtype/service/`
  - Método `ensureCodeIsUnique(String code, UUID excludeUuid)`: Validar código único
  - Inyectar `TaxTypePort` para consultar existencia
  - Lanzar `DuplicateTaxTypeCodeException` si existe

---

### Phase 3: Application Ports (2 tasks)

**Purpose**: Definir contratos de puertos de entrada y salida.

**Tasks**:

- [ ] **T006**: Crear Input Ports (Use Cases) en `application/port/in/taxtype/`
  - `CreateTaxTypeUseCase.java`
  - `GetTaxTypeUseCase.java`
  - `UpdateTaxTypeUseCase.java`
  - `DeactivateTaxTypeUseCase.java`
  - `ActivateTaxTypeUseCase.java`
  - `ListTaxTypesUseCase.java`
  - `SearchTaxTypesUseCase.java`
  - Cada uno con método `execute()` y parámetros según spec

- [ ] **T007**: Crear Output Port (Repository Interface) en `application/port/out/`
  - `TaxTypePort.java` con métodos:
    - save, findByUuid, findByCode, findAll, findByEnabled
    - findByEnabledAndApplicationType, searchByNameContainingIgnoreCase
    - existsByCode, existsByCodeAndUuidNot, countProductsWithTaxType, deleteByUuid

---

### Phase 4: Application Services (Use Case Implementations) (7 tasks)

**Purpose**: Implementar casos de uso coordinando domain services y ports.

**Tasks**:

- [ ] **T008**: Implementar `CreateTaxTypeService` en `application/service/taxtype/`
  - Validar código, nombre y porcentaje usando domain services
  - Validar unicidad de código
  - Crear TaxType con UUID auto-generado
  - Persistir usando TaxTypePort
  - Mapear a DTO de respuesta
  - TODO: Registrar en AuditLog

- [ ] **T009**: Implementar `GetTaxTypeService`
  - Buscar por UUID usando TaxTypePort
  - Lanzar TaxTypeNotFoundException si no existe
  - Mapear a DTO de respuesta

- [ ] **T010**: Implementar `UpdateTaxTypeService`
  - Buscar existente por UUID
  - Validar datos usando domain services
  - Validar unicidad de código (excluyendo UUID actual)
  - Actualizar campos
  - Persistir cambios
  - TODO: Registrar en AuditLog

- [ ] **T011**: Implementar `DeactivateTaxTypeService`
  - Buscar por UUID
  - Validar que no haya productos asociados usando `countProductsWithTaxType()`
  - Llamar `deactivate()` del domain model
  - Persistir cambios
  - TODO: Registrar en AuditLog

- [ ] **T012**: Implementar `ActivateTaxTypeService`
  - Buscar por UUID
  - Llamar `activate()` del domain model
  - Persistir cambios
  - TODO: Registrar en AuditLog

- [ ] **T013**: Implementar `ListTaxTypesService`
  - Aplicar filtros: enabled, applicationType
  - Si applicationType es SALE o PURCHASE, incluir también los que son BOTH
  - Usar paginación
  - Ordenar por código
  - Mapear resultados a DTOs

- [ ] **T014**: Implementar `SearchTaxTypesService`
  - Buscar por nombre usando like case-insensitive
  - Solo activos (enabled=true)
  - Ordenar alfabéticamente
  - Mapear resultados a DTOs

---

### Phase 5: Infrastructure - Persistence (4 tasks)

**Purpose**: Implementar adaptadores de persistencia.

**Tasks**:

- [ ] **T015**: Crear `TaxTypeEntity` en `infrastructure/out/taxtype/persistence/entity/`
  - Anotaciones JPA: @Entity, @Table con índices
  - Todos los campos según spec técnico
  - Índices: uuid (unique), code (unique), enabled, application_type
  - @PrePersist: auto-generar UUID, timestamps
  - @PreUpdate: actualizar updatedAt

- [ ] **T016**: Crear `TaxTypeJpaRepository` en `infrastructure/out/taxtype/persistence/repository/`
  - Extender JpaRepository<TaxTypeEntity, Long>
  - Métodos custom: findByUuid, findByCode, findByEnabled
  - Query custom: findByEnabledAndApplicationType (incluir BOTH)
  - findByNameContainingIgnoreCaseAndEnabled
  - existsByCode, existsByCodeAndUuidNot
  - Query: countProductsWithTaxType (join con products)

- [ ] **T017**: Crear `TaxTypeEntityMapper` usando MapStruct en `infrastructure/out/taxtype/mapper/`
  - Mapeo bidireccional: TaxType <-> TaxTypeEntity
  - Manejar relaciones con User (createdBy, updatedBy, deletedBy)

- [ ] **T018**: Implementar `TaxTypeRepositoryAdapter` en `infrastructure/out/taxtype/persistence/adapter/`
  - Implementar interfaz `TaxTypePort`
  - Inyectar TaxTypeJpaRepository y TaxTypeEntityMapper
  - Delegar todas las operaciones al repository JPA
  - Mapear entidades a domain models

---

### Phase 6: Infrastructure - API REST (4 tasks)

**Purpose**: Implementar controlador REST y DTOs.

**Tasks**:

- [ ] **T019**: Crear DTOs en `infrastructure/in/api/taxtype/dto/`
  - `CreateTaxTypeRequestDto` con validaciones Jakarta
  - `UpdateTaxTypeRequestDto` con validaciones Jakarta
  - `TaxTypeResponseDto` sin validaciones
  - Validaciones: @NotBlank, @NotNull, @Size, @DecimalMin, @DecimalMax, @Digits, @Pattern

- [ ] **T020**: Crear `TaxTypeDtoMapper` usando MapStruct en `infrastructure/in/api/taxtype/mapper/`
  - Mapeo: DTOs <-> Domain Models
  - toRequestDto, toResponseDto, toResponseDtoList

- [ ] **T021**: Crear constantes en `TaxTypeApiConstants` en `infrastructure/in/api/taxtype/constant/`
  - Paths: BASE_PATH = "/api/tax-types"
  - Mensajes de error estándar
  - Configuraciones por defecto (page size, etc.)

- [ ] **T022**: Implementar `TaxTypeController` en `infrastructure/in/api/taxtype/rest/`
  - Anotaciones: @RestController, @RequestMapping, @RequiredArgsConstructor, @Tag
  - Endpoints: POST /, GET /{uuid}, PUT /{uuid}, PATCH /{uuid}/deactivate, PATCH /{uuid}/activate, GET /, GET /search
  - Inyectar todos los use cases
  - Documentación Swagger: @Operation
  - Manejo de errores delegado a GlobalExceptionHandler

---

### Phase 7: Database Migrations (2 tasks)

**Purpose**: Crear scripts de migración Flyway.

**Tasks**:

- [ ] **T023**: Crear migración `V1.3__create_tax_types_table.sql` en `resources/db/migration/`
  - Tabla tax_types con todos los campos según spec
  - Índices: uuid, code, enabled, application_type
  - Columna percentage con DECIMAL(7,4)
  - Valores por defecto: enabled=TRUE, is_included=FALSE
  - Engine InnoDB, charset utf8mb4

- [ ] **T024**: Crear migración `V1.4__insert_colombia_tax_types.sql` en `resources/db/migration/`
  - INSERT de 7 tipos de impuestos de Colombia
  - IVA19, IVA5, IVA0, RETE2.5, RETE10, RETEIVA15, ICA
  - Usar ON DUPLICATE KEY UPDATE para idempotencia
  - UUIDs auto-generados con UUID()

---

### Phase 8: Testing - Unit Tests (9 tasks)

**Purpose**: Implementar tests unitarios con alta cobertura.

**Tasks**:

- [ ] **T025**: Test de `TaxType` domain model en `domain/model/TaxTypeTest.java`
  - Test métodos de negocio: activate(), deactivate(), isApplicableForSales(), isApplicableForPurchases()
  - Test isValidPercentage() con diferentes valores

- [ ] **T026**: Test de `TaxTypeDomainService` en `domain/service/TaxTypeDomainServiceTest.java`
  - Test validateCode: válido, inválido (vacío, > 20 chars, caracteres inválidos)
  - Test validatePercentage: válido, negativo, > 100, más de 4 decimales
  - Test validateName: válido, vacío, > 100 chars
  - Test canBeDeleted: con/sin productos asociados

- [ ] **T027**: Test de `TaxTypeValidationService` en `domain/service/TaxTypeValidationServiceTest.java`
  - Test ensureCodeIsUnique: código único, código duplicado
  - Mock de TaxTypePort

- [ ] **T028**: Test de `CreateTaxTypeService` en `application/service/CreateTaxTypeServiceTest.java`
  - Test creación exitosa
  - Test con código duplicado (lanzar excepción)
  - Test con datos inválidos (código, nombre, porcentaje)
  - Mock de todos los dependencies

- [ ] **T029**: Test de `GetTaxTypeService` en `application/service/GetTaxTypeServiceTest.java`
  - Test obtener por UUID existente
  - Test con UUID inexistente (lanzar TaxTypeNotFoundException)

- [ ] **T030**: Test de `UpdateTaxTypeService` en `application/service/UpdateTaxTypeServiceTest.java`
  - Test actualización exitosa
  - Test con UUID inexistente
  - Test con código duplicado (diferente al actual)
  - Test con datos inválidos

- [ ] **T031**: Test de `DeactivateTaxTypeService` en `application/service/DeactivateTaxTypeServiceTest.java`
  - Test desactivación exitosa sin productos asociados
  - Test con productos asociados (lanzar TaxTypeConstraintException)
  - Test con UUID inexistente

- [ ] **T032**: Test de `ListTaxTypesService` en `application/service/ListTaxTypesServiceTest.java`
  - Test listar todos sin filtros
  - Test filtrar por enabled=true
  - Test filtrar por applicationType=SALE (incluir BOTH)
  - Test filtrar por applicationType=PURCHASE (incluir BOTH)
  - Test paginación

- [ ] **T033**: Test de `SearchTaxTypesService` en `application/service/SearchTaxTypesServiceTest.java`
  - Test búsqueda exitosa con resultados
  - Test búsqueda sin resultados
  - Test búsqueda case-insensitive

---

### Phase 9: Testing - Integration Tests (3 tasks)

**Purpose**: Implementar tests de integración.

**Tasks**:

- [ ] **T034**: Test de `TaxTypeJpaRepository` en `infrastructure/out/persistence/repository/TaxTypeJpaRepositoryTest.java`
  - Test de todos los métodos custom con Testcontainers
  - findByUuid, findByCode, findByEnabled, findByEnabledAndApplicationType
  - searchByNameContainingIgnoreCase, existsByCode, existsByCodeAndUuidNot

- [ ] **T035**: Test de `TaxTypeRepositoryAdapter` en `infrastructure/out/persistence/adapter/TaxTypeRepositoryAdapterTest.java`
  - Test del adapter con mocks de JpaRepository y mapper
  - Validar que delega correctamente las operaciones

- [ ] **T036**: Test de `TaxTypeController` en `infrastructure/in/api/rest/TaxTypeControllerTest.java`
  - Test de todos los endpoints con MockMvc
  - Test validaciones de entrada (Jakarta Validator)
  - Test respuestas exitosas y de error
  - Test status codes correctos (201, 200, 400, 404, 409)

---

### Phase 10: Documentation & Review (2 tasks)

**Purpose**: Documentar y revisar antes de merge.

**Tasks**:

- [ ] **T037**: Actualizar documentación Swagger
  - Verificar anotaciones @Operation en controller
  - Agregar ejemplos en DTOs con @Schema
  - Agregar descripciones a parámetros
  - Documentar códigos de error posibles

- [ ] **T038**: Code Review y Cleanup
  - Eliminar TODOs y logs innecesarios
  - Verificar cumplimiento de estándares de código
  - Verificar cobertura de tests >= 85%
  - Actualizar CHANGELOG
  - Actualizar STATUS.md

---

## Dependencies

### Internal Dependencies
- **None**: Este es un módulo independiente sin dependencias de otros módulos de negocio

### External Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- MySQL Connector
- Lombok
- MapStruct
- Hibernate Validator
- Flyway Core
- Spring Boot Starter Test
- JUnit 5
- Mockito
- Testcontainers (MySQL)

---

## Risks & Mitigations

### Risk 1: Cambios en Tasas Impositivas
**Impact**: Alto  
**Probability**: Media  
**Mitigation**:
- Mantener histórico de cambios en auditoría
- Permitir actualización de porcentajes sin afectar transacciones históricas
- Documentar proceso de actualización de tasas

### Risk 2: Eliminación de Impuestos con Referencias
**Impact**: Medio  
**Probability**: Alta  
**Mitigation**:
- Validación estricta antes de eliminación
- Mensajes de error claros
- Soft delete por defecto
- Permitir desactivación en lugar de eliminación

### Risk 3: Precisión Decimal en Porcentajes
**Impact**: Alto  
**Probability**: Baja  
**Mitigation**:
- Usar BigDecimal con 4 decimales
- Validar precisión en capa de dominio
- Tests exhaustivos de cálculos

---

## Estimation

### Time Estimates (Story Points)
- Phase 1: 2 SP (2 hours)
- Phase 2: 2 SP (2 hours)
- Phase 3: 1 SP (1 hour)
- Phase 4: 5 SP (5 hours)
- Phase 5: 3 SP (3 hours)
- Phase 6: 3 SP (3 hours)
- Phase 7: 1 SP (1 hour)
- Phase 8: 5 SP (5 hours)
- Phase 9: 3 SP (3 hours)
- Phase 10: 2 SP (2 hours)

**Total**: 27 SP (~27 hours / ~3.5 days)

### Timeline
- Start: Pending approval
- End: ~1 week after start (considering team capacity)

---

## Success Criteria

- [ ] Todas las 38 tareas completadas
- [ ] Cobertura de tests >= 85%
- [ ] Todos los tests pasando (unit + integration)
- [ ] API documentada en Swagger
- [ ] Seed data de Colombia cargado correctamente
- [ ] Performance: list operations < 100ms p95
- [ ] Code review aprobado
- [ ] Sin warnings de SonarQube críticos

---

## Definition of Done

- [ ] Código implementado y commiteado
- [ ] Tests unitarios >= 85% cobertura
- [ ] Tests de integración pasando
- [ ] Migraciones Flyway ejecutadas sin errores
- [ ] Documentación Swagger completa
- [ ] Code review aprobado por al menos 2 reviewers
- [ ] Sin errores en análisis estático (SonarQube)
- [ ] README actualizado si aplica
- [ ] STATUS.md actualizado

---

## Next Steps After Completion

1. Mover documentación de `wip/tax-types/` a `features/03-tax-types/`
2. Crear `IMPLEMENTED.md` con resumen de implementación
3. Actualizar `STATUS.md` global del proyecto
4. Tag en Git: `v0.3.0-tax-types`
5. Notificar a equipos dependientes (Products, Sales, Purchases)

---

## Notes

- Este módulo es independiente y puede implementarse en paralelo con Geography
- Se recomienda implementar después de Geography para mantener secuencia lógica de features
- Los tipos de impuestos son relativamente estables, cambios poco frecuentes
- Considerar implementar cache para mejorar performance si se consulta frecuentemente

---

## References

- [Functional Spec](functional-spec.md)
- [Technical Spec](technical-spec.md)
- [Framework SDD](../../framework/proyecto-framework-sdd.md)
- [Document Types Implementation](../../features/01-document-types/) (reference implementation)
