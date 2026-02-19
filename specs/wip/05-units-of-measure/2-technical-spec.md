# Technical Specification: Módulo de Unidades de Medida

**Feature**: 05-units-of-measure  
**Version**: 1.1  
**Created**: 2026-02-01  
**Last Updated**: 2026-02-13  
**Status**: ⚠️ PHASE 2 - Technical Draft Refinement (v1.1)

---

## 🎯 Architecture Overview

Este módulo implementa un **catálogo base de unidades de medida** siguiendo la **arquitectura hexagonal** (Ports & Adapters).

### Tech Stack

- **Backend**: Java 21, Spring Boot 3.x
- **Persistence**: MySQL 8.0+ con Flyway
- **Mapping**: MapStruct 1.5+
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Documentation**: Swagger/OpenAPI 3.0

---

## ✅ Definición Técnica Complementaria (Iteración v1.1)

### DT-01: Convención de Rutas REST
- Base path canónico: `/api/v1/units-of-measure`
- Los endpoints definidos previamente se mantienen, pero sobre prefijo `v1`.

### DT-02: Organización de Paquetes (Alineación Scaffolding)
- Se adopta estructura feature-based consistente con módulos vigentes:
    - `domain.model.unitofmeasure`
    - `domain.service.unitofmeasure`
    - `domain.exception.unitofmeasure`
    - `domain.port.out.unitofmeasure`
    - `application.port.in.unitofmeasure`
    - `application.usecase.unitofmeasure`
    - `infrastructure.in.web.(controller|dto|mapper).unitofmeasure`
    - `infrastructure.out.persistence.(entity|repository|mapper|adapter).unitofmeasure`

### DT-03: Convención de Casos de Uso
- Se recomienda consolidar puertos de entrada en estilo CQRS para coherencia transversal:
    - `CompareUnitsOfMeasureUseCase` (consultas)
    - `ManageUnitOfMeasureUseCase` (comandos)
- Si se mantiene granularidad por operación, deberá justificarse en revisión técnica.

### DT-04: Convención de Modelo de Persistencia
- Para compatibilidad con módulos existentes, se define convención de tabla:
    - `id BIGINT AUTO_INCREMENT` (PK interna)
    - `uuid BINARY(16) UNIQUE` (identificador externo)
    - `enabled BOOLEAN` (estado lógico)
    - `created_at`, `updated_at`, `deleted_at`
    - `created_by`, `updated_by`, `deleted_by`
- Los snippets previos que usan `id` UUID directo como PK se consideran referencia preliminar y quedan supersedidos por esta convención.

### DT-05: Soft Delete y Reactivación
- Soft delete: desactivación lógica + trazabilidad de auditoría.
- Reactivación: limpia marcadores de eliminación lógica y restituye `enabled=true`.

### DT-06: Definición de Búsqueda/Listado
- Se mantiene búsqueda case-insensitive por `name` y `abbreviation`.
- Se recomienda consolidar en `GET /api/v1/units-of-measure` con filtros opcionales y paginación (`page`, `size`, `sort`, `direction`) para coherencia con módulos implementados.
- Si se conserva endpoint dedicado `/search`, debe documentarse como alias funcional del listado filtrado.

### DT-07: Servicios de Dominio
- Los servicios de dominio se modelan como POJOs de dominio (sin anotaciones de framework en capa domain).
- La inyección se resuelve en configuración de infraestructura (`BeanConfiguration`).

---

## 🏗️ Architecture Layers

### Domain Layer (Core Business Logic)

**Purpose**: Modelos y reglas de negocio independientes de tecnología.

#### Domain Models (`domain/unitofmeasure/model/`)

**UnitOfMeasure.java** (Aggregate Root)
```java
@Getter
@Builder
@AllArgsConstructor
public class UnitOfMeasure {
    private UUID id;
    private String name;           // 2-50 chars, unique
    private String abbreviation;   // 1-10 chars, unique
    private boolean active;
    
    // Audit fields
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
    
    // Factory method
    public static UnitOfMeasure create(String name, String abbreviation, UUID userId) {
        return UnitOfMeasure.builder()
            .id(UUID.randomUUID())
            .name(name)
            .abbreviation(abbreviation.toUpperCase())
            .active(true)
            .createdAt(LocalDateTime.now())
            .createdBy(userId)
            .build();
    }
    
    // Business methods
    public void update(String name, String abbreviation, UUID userId) {
        this.name = name;
        this.abbreviation = abbreviation.toUpperCase();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = userId;
    }
    
    public void deactivate() {
        this.active = false;
    }
    
    public void activate() {
        this.active = true;
    }
    
    public boolean isActive() {
        return this.active;
    }
}
```

#### Domain Exceptions (`domain/unitofmeasure/exception/`)

```java
// Base exception
public class UnitOfMeasureException extends RuntimeException

// Specific exceptions
public class UnitOfMeasureNotFoundException extends UnitOfMeasureException
public class DuplicateUnitOfMeasureNameException extends UnitOfMeasureException
public class DuplicateUnitOfMeasureAbbreviationException extends UnitOfMeasureException
public class UnitOfMeasureInUseException extends UnitOfMeasureException
public class InvalidUnitOfMeasureDataException extends UnitOfMeasureException
```

#### Domain Services (`domain/unitofmeasure/service/`)

**UnitOfMeasureDomainService.java**
```java
@Service
@RequiredArgsConstructor
public class UnitOfMeasureDomainService {
    
    // Validates name format
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidUnitOfMeasureDataException("El nombre no puede estar vacío");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new InvalidUnitOfMeasureDataException("El nombre debe tener entre 2 y 50 caracteres");
        }
        if (!name.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            throw new InvalidUnitOfMeasureDataException("El nombre solo puede contener letras y espacios");
        }
    }
    
    // Validates abbreviation format
    public void validateAbbreviation(String abbreviation) {
        if (abbreviation == null || abbreviation.isBlank()) {
            throw new InvalidUnitOfMeasureDataException("La abreviatura no puede estar vacía");
        }
        if (abbreviation.length() < 1 || abbreviation.length() > 10) {
            throw new InvalidUnitOfMeasureDataException("La abreviatura debe tener entre 1 y 10 caracteres");
        }
        if (!abbreviation.matches("^[a-zA-Z0-9²³]+$")) {
            throw new InvalidUnitOfMeasureDataException("La abreviatura solo puede contener letras, números y símbolos matemáticos");
        }
    }
    
    // Checks if unit can be deleted (not in use)
    public void ensureCanBeDeleted(UUID unitId, long usageCount) {
        if (usageCount > 0) {
            throw new UnitOfMeasureInUseException(
                String.format("No se puede desactivar esta unidad porque está en uso por %d productos", usageCount)
            );
        }
    }
}
```

**UnitOfMeasureValidationService.java**
```java
@Service
@RequiredArgsConstructor
public class UnitOfMeasureValidationService {
    private final UnitOfMeasurePort unitOfMeasurePort;
    
    // Ensures name uniqueness
    public void ensureNameIsUnique(String name, UUID excludeId) {
        boolean exists = unitOfMeasurePort.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        if (exists) {
            throw new DuplicateUnitOfMeasureNameException(
                String.format("Ya existe una unidad de medida con el nombre '%s'", name)
            );
        }
    }
    
    // Ensures abbreviation uniqueness
    public void ensureAbbreviationIsUnique(String abbreviation, UUID excludeId) {
        boolean exists = unitOfMeasurePort.existsByAbbreviationIgnoreCaseAndIdNot(abbreviation, excludeId);
        if (exists) {
            throw new DuplicateUnitOfMeasureAbbreviationException(
                String.format("Ya existe una unidad de medida con la abreviatura '%s'", abbreviation)
            );
        }
    }
}
```

---

### Application Layer (Use Cases)

**Purpose**: Orquestación de casos de uso, lógica de aplicación

**Ports** (`application/port/`):

#### Input Ports (Use Cases)

```java
// CreateUnitOfMeasureUseCase.java
public interface CreateUnitOfMeasureUseCase {
    UnitOfMeasure execute(CreateUnitOfMeasureCommand command);
}

// GetUnitOfMeasureUseCase.java
public interface GetUnitOfMeasureUseCase {
    UnitOfMeasure execute(UUID id);
}

// UpdateUnitOfMeasureUseCase.java
public interface UpdateUnitOfMeasureUseCase {
    UnitOfMeasure execute(UUID id, UpdateUnitOfMeasureCommand command);
}

// DeactivateUnitOfMeasureUseCase.java
public interface DeactivateUnitOfMeasureUseCase {
    void execute(UUID id);
}

// ActivateUnitOfMeasureUseCase.java
public interface ActivateUnitOfMeasureUseCase {
    void execute(UUID id);
}

// ListUnitsOfMeasureUseCase.java
public interface ListUnitsOfMeasureUseCase {
    List<UnitOfMeasure> execute(Boolean enabled);
}

// SearchUnitsOfMeasureUseCase.java
public interface SearchUnitsOfMeasureUseCase {
    List<UnitOfMeasure> execute(String name, String abbreviation);
}
```

#### Output Ports (Repository Interfaces)

```java
// UnitOfMeasurePort.java
public interface UnitOfMeasurePort {
    UnitOfMeasure save(UnitOfMeasure unitOfMeasure);
    Optional<UnitOfMeasure> findById(UUID id);
    List<UnitOfMeasure> findAll();
    List<UnitOfMeasure> findByActive(boolean active);
    List<UnitOfMeasure> searchByName(String name);
    List<UnitOfMeasure> searchByAbbreviation(String abbreviation);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID excludeId);
    boolean existsByAbbreviationIgnoreCaseAndIdNot(String abbreviation, UUID excludeId);
    long countProductsByUnitOfMeasureId(UUID unitId);
}
```

#### Services (`application/service/`)

**CreateUnitOfMeasureService.java**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class CreateUnitOfMeasureService implements CreateUnitOfMeasureUseCase {
    private final UnitOfMeasurePort unitOfMeasurePort;
    private final UnitOfMeasureDomainService domainService;
    private final UnitOfMeasureValidationService validationService;
    
    @Override
    public UnitOfMeasure execute(CreateUnitOfMeasureCommand command) {
        // 1. Validate format
        domainService.validateName(command.name());
        domainService.validateAbbreviation(command.abbreviation());
        
        // 2. Validate uniqueness
        validationService.ensureNameIsUnique(command.name(), null);
        validationService.ensureAbbreviationIsUnique(command.abbreviation(), null);
        
        // 3. Create domain object
        UnitOfMeasure unitOfMeasure = UnitOfMeasure.create(
            command.name(),
            command.abbreviation(),
            command.userId()
        );
        
        // 4. Persist
        return unitOfMeasurePort.save(unitOfMeasure);
    }
}
```

**UpdateUnitOfMeasureService.java**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateUnitOfMeasureService implements UpdateUnitOfMeasureUseCase {
    private final UnitOfMeasurePort unitOfMeasurePort;
    private final UnitOfMeasureDomainService domainService;
    private final UnitOfMeasureValidationService validationService;
    
    @Override
    public UnitOfMeasure execute(UUID id, UpdateUnitOfMeasureCommand command) {
        // 1. Find existing
        UnitOfMeasure unitOfMeasure = unitOfMeasurePort.findById(id)
            .orElseThrow(() -> new UnitOfMeasureNotFoundException(id));
        
        // 2. Validate format
        domainService.validateName(command.name());
        domainService.validateAbbreviation(command.abbreviation());
        
        // 3. Validate uniqueness
        validationService.ensureNameIsUnique(command.name(), id);
        validationService.ensureAbbreviationIsUnique(command.abbreviation(), id);
        
        // 4. Update
        unitOfMeasure.update(command.name(), command.abbreviation(), command.userId());
        
        // 5. Persist
        return unitOfMeasurePort.save(unitOfMeasure);
    }
}
```

**DeactivateUnitOfMeasureService.java**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class DeactivateUnitOfMeasureService implements DeactivateUnitOfMeasureUseCase {
    private final UnitOfMeasurePort unitOfMeasurePort;
    private final UnitOfMeasureDomainService domainService;
    
    @Override
    public void execute(UUID id) {
        // 1. Find existing
        UnitOfMeasure unitOfMeasure = unitOfMeasurePort.findById(id)
            .orElseThrow(() -> new UnitOfMeasureNotFoundException(id));
        
        // 2. Check if in use
        long usageCount = unitOfMeasurePort.countProductsByUnitOfMeasureId(id);
        domainService.ensureCanBeDeleted(id, usageCount);
        
        // 3. Deactivate
        unitOfMeasure.deactivate();
        
        // 4. Persist
        unitOfMeasurePort.save(unitOfMeasure);
    }
}
```

**ListUnitsOfMeasureService.java**
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUnitsOfMeasureService implements ListUnitsOfMeasureUseCase {
    private final UnitOfMeasurePort unitOfMeasurePort;
    
    @Override
    public List<UnitOfMeasure> execute(Boolean enabled) {
        if (enabled != null) {
            return unitOfMeasurePort.findByActive(enabled);
        }
        return unitOfMeasurePort.findAll();
    }
}
```

**SearchUnitsOfMeasureService.java**
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUnitsOfMeasureService implements SearchUnitsOfMeasureUseCase {
    private final UnitOfMeasurePort unitOfMeasurePort;
    
    @Override
    public List<UnitOfMeasure> execute(String name, String abbreviation) {
        if (name != null && !name.isBlank()) {
            return unitOfMeasurePort.searchByName(name);
        }
        if (abbreviation != null && !abbreviation.isBlank()) {
            return unitOfMeasurePort.searchByAbbreviation(abbreviation);
        }
        return List.of();
    }
}
```

---

### Infrastructure Layer (Adapters)

**Purpose**: Implementación de detalles técnicos (DB, API, etc.)

#### Output Adapters (Persistence)

**UnitOfMeasureEntity.java** (`infrastructure/persistence/entity/`)
```java
@Entity
@Table(name = "units_of_measure",
       indexes = {
           @Index(name = "idx_uom_name", columnList = "name"),
           @Index(name = "idx_uom_abbreviation", columnList = "abbreviation"),
           @Index(name = "idx_uom_active", columnList = "active")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_uom_name", columnNames = "name"),
           @UniqueConstraint(name = "uk_uom_abbreviation", columnNames = "abbreviation")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UnitOfMeasureEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "abbreviation", nullable = false, length = 10)
    private String abbreviation;
    
    @Column(name = "active", nullable = false)
    private Boolean active;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", columnDefinition = "BINARY(16)", updatable = false)
    private UUID createdBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", columnDefinition = "BINARY(16)")
    private UUID updatedBy;
}
```

**UnitOfMeasureJpaRepository.java**
```java
@Repository
public interface UnitOfMeasureJpaRepository extends JpaRepository<UnitOfMeasureEntity, UUID> {
    
    List<UnitOfMeasureEntity> findByActive(Boolean active);
    
    @Query("SELECT u FROM UnitOfMeasureEntity u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UnitOfMeasureEntity> searchByName(@Param("name") String name);
    
    @Query("SELECT u FROM UnitOfMeasureEntity u WHERE LOWER(u.abbreviation) LIKE LOWER(CONCAT('%', :abbreviation, '%'))")
    List<UnitOfMeasureEntity> searchByAbbreviation(@Param("abbreviation") String abbreviation);
    
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    
    boolean existsByAbbreviationIgnoreCaseAndIdNot(String abbreviation, UUID id);
    
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.baseUnitId = :unitId AND p.active = true")
    long countActiveProductsByUnitId(@Param("unitId") UUID unitId);
}
```

**UnitOfMeasureEntityMapper.java**
```java
@Mapper(componentModel = "spring")
public interface UnitOfMeasureEntityMapper {
    
    UnitOfMeasure toDomain(UnitOfMeasureEntity entity);
    
    UnitOfMeasureEntity toEntity(UnitOfMeasure domain);
    
    List<UnitOfMeasure> toDomainList(List<UnitOfMeasureEntity> entities);
}
```

**UnitOfMeasureRepositoryAdapter.java**
```java
@Repository
@RequiredArgsConstructor
public class UnitOfMeasureRepositoryAdapter implements UnitOfMeasurePort {
    private final UnitOfMeasureJpaRepository jpaRepository;
    private final UnitOfMeasureEntityMapper mapper;
    
    @Override
    public UnitOfMeasure save(UnitOfMeasure unitOfMeasure) {
        UnitOfMeasureEntity entity = mapper.toEntity(unitOfMeasure);
        UnitOfMeasureEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<UnitOfMeasure> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public List<UnitOfMeasure> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }
    
    @Override
    public List<UnitOfMeasure> findByActive(boolean active) {
        return mapper.toDomainList(jpaRepository.findByActive(active));
    }
    
    @Override
    public List<UnitOfMeasure> searchByName(String name) {
        return mapper.toDomainList(jpaRepository.searchByName(name));
    }
    
    @Override
    public List<UnitOfMeasure> searchByAbbreviation(String abbreviation) {
        return mapper.toDomainList(jpaRepository.searchByAbbreviation(abbreviation));
    }
    
    @Override
    public boolean existsByNameIgnoreCaseAndIdNot(String name, UUID excludeId) {
        UUID safeId = excludeId != null ? excludeId : UUID.randomUUID();
        return jpaRepository.existsByNameIgnoreCaseAndIdNot(name, safeId);
    }
    
    @Override
    public boolean existsByAbbreviationIgnoreCaseAndIdNot(String abbreviation, UUID excludeId) {
        UUID safeId = excludeId != null ? excludeId : UUID.randomUUID();
        return jpaRepository.existsByAbbreviationIgnoreCaseAndIdNot(abbreviation, safeId);
    }
    
    @Override
    public long countProductsByUnitOfMeasureId(UUID unitId) {
        return jpaRepository.countActiveProductsByUnitId(unitId);
    }
}
```

#### Input Adapters (REST API)

**DTOs** (`infrastructure/api/dto/`)

```java
// CreateUnitOfMeasureDto.java
public record CreateUnitOfMeasureDto(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras y espacios")
    String name,
    
    @NotBlank(message = "La abreviatura es obligatoria")
    @Size(min = 1, max = 10, message = "La abreviatura debe tener entre 1 y 10 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9²³]+$", message = "La abreviatura solo puede contener letras, números y símbolos matemáticos")
    String abbreviation
) {}

// UpdateUnitOfMeasureDto.java
public record UpdateUnitOfMeasureDto(
    @NotBlank @Size(min = 2, max = 50) String name,
    @NotBlank @Size(min = 1, max = 10) String abbreviation
) {}

// UnitOfMeasureResponseDto.java
public record UnitOfMeasureResponseDto(
    UUID id,
    String name,
    String abbreviation,
    Boolean active,
    LocalDateTime createdAt,
    UUID createdBy,
    LocalDateTime updatedAt,
    UUID updatedBy
) {}
```

**UnitOfMeasureDtoMapper.java**
```java
@Mapper(componentModel = "spring")
public interface UnitOfMeasureDtoMapper {
    
    UnitOfMeasureResponseDto toResponseDto(UnitOfMeasure domain);
    
    List<UnitOfMeasureResponseDto> toResponseDtoList(List<UnitOfMeasure> domains);
    
    default CreateUnitOfMeasureCommand toCreateCommand(CreateUnitOfMeasureDto dto, UUID userId) {
        return new CreateUnitOfMeasureCommand(dto.name(), dto.abbreviation(), userId);
    }
    
    default UpdateUnitOfMeasureCommand toUpdateCommand(UpdateUnitOfMeasureDto dto, UUID userId) {
        return new UpdateUnitOfMeasureCommand(dto.name(), dto.abbreviation(), userId);
    }
}
```

**UnitOfMeasureController.java**
```java
@RestController
@RequestMapping("/api/units-of-measure")
@RequiredArgsConstructor
@Tag(name = "Units of Measure", description = "Endpoints para gestión de unidades de medida")
public class UnitOfMeasureController {
    
    private final CreateUnitOfMeasureUseCase createUseCase;
    private final GetUnitOfMeasureUseCase getUseCase;
    private final UpdateUnitOfMeasureUseCase updateUseCase;
    private final DeactivateUnitOfMeasureUseCase deactivateUseCase;
    private final ActivateUnitOfMeasureUseCase activateUseCase;
    private final ListUnitsOfMeasureUseCase listUseCase;
    private final SearchUnitsOfMeasureUseCase searchUseCase;
    private final UnitOfMeasureDtoMapper mapper;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nueva unidad de medida")
    public UnitOfMeasureResponseDto create(
            @Valid @RequestBody CreateUnitOfMeasureDto dto,
            @AuthenticationPrincipal UserPrincipal user) {
        
        CreateUnitOfMeasureCommand command = mapper.toCreateCommand(dto, user.getId());
        UnitOfMeasure created = createUseCase.execute(command);
        return mapper.toResponseDto(created);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener unidad por UUID")
    public UnitOfMeasureResponseDto getById(@PathVariable UUID id) {
        UnitOfMeasure unitOfMeasure = getUseCase.execute(id);
        return mapper.toResponseDto(unitOfMeasure);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar unidad de medida")
    public UnitOfMeasureResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUnitOfMeasureDto dto,
            @AuthenticationPrincipal UserPrincipal user) {
        
        UpdateUnitOfMeasureCommand command = mapper.toUpdateCommand(dto, user.getId());
        UnitOfMeasure updated = updateUseCase.execute(id, command);
        return mapper.toResponseDto(updated);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar unidad (soft delete)")
    public void deactivate(@PathVariable UUID id) {
        deactivateUseCase.execute(id);
    }
    
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar unidad")
    public UnitOfMeasureResponseDto activate(@PathVariable UUID id) {
        activateUseCase.execute(id);
        UnitOfMeasure unitOfMeasure = getUseCase.execute(id);
        return mapper.toResponseDto(unitOfMeasure);
    }
    
    @GetMapping
    @Operation(summary = "Listar unidades con filtros")
    public List<UnitOfMeasureResponseDto> list(
            @RequestParam(required = false) Boolean enabled) {
        
        List<UnitOfMeasure> units = listUseCase.execute(enabled);
        return mapper.toResponseDtoList(units);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Buscar unidades por nombre o abreviatura")
    public List<UnitOfMeasureResponseDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abbreviation) {
        
        List<UnitOfMeasure> units = searchUseCase.execute(name, abbreviation);
        return mapper.toResponseDtoList(units);
    }
}
```

**GlobalExceptionHandler.java** (Partial)
```java
@ExceptionHandler(UnitOfMeasureNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(UnitOfMeasureNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder()
        .code("UNIT_OF_MEASURE_NOT_FOUND")
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler({
    DuplicateUnitOfMeasureNameException.class,
    DuplicateUnitOfMeasureAbbreviationException.class,
    UnitOfMeasureInUseException.class
})
public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
    ErrorResponse error = ErrorResponse.builder()
        .code("UNIT_OF_MEASURE_CONFLICT")
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}

@ExceptionHandler(InvalidUnitOfMeasureDataException.class)
public ResponseEntity<ErrorResponse> handleInvalidData(InvalidUnitOfMeasureDataException ex) {
    ErrorResponse error = ErrorResponse.builder()
        .code("INVALID_UNIT_OF_MEASURE_DATA")
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

---

## 💾 Database Design

> **Nota v1.1**: Este diseño debe interpretarse bajo la convención DT-04. En caso de discrepancia entre snippets previos y DT-04, prevalece DT-04 por alineación transversal con el repositorio.

### Entity Relationship Diagram

```
┌─────────────────────────────┐
│   units_of_measure          │
├─────────────────────────────┤
│ id             BINARY(16) PK│
│ name           VARCHAR(50)  │◄──── UNIQUE
│ abbreviation   VARCHAR(10)  │◄──── UNIQUE
│ active         BOOLEAN      │◄──── INDEX
│ created_at     DATETIME     │
│ created_by     BINARY(16)   │
│ updated_at     DATETIME     │
│ updated_by     BINARY(16)   │
└─────────────────────────────┘
         △
         │ baseUnitId (FK)
         │
┌────────┴────────┐
│   products      │ (future module)
└─────────────────┘
```

### Flyway Migrations

**V1.7__create_units_of_measure_table.sql**
```sql
CREATE TABLE units_of_measure (
    id             BINARY(16)   NOT NULL,
    name           VARCHAR(50)  NOT NULL,
    abbreviation   VARCHAR(10)  NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME     NOT NULL,
    created_by     BINARY(16)   NOT NULL,
    updated_at     DATETIME,
    updated_by     BINARY(16),
    PRIMARY KEY (id),
    CONSTRAINT uk_uom_name UNIQUE (name),
    CONSTRAINT uk_uom_abbreviation UNIQUE (abbreviation),
    INDEX idx_uom_name (name),
    INDEX idx_uom_abbreviation (abbreviation),
    INDEX idx_uom_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**V1.8__insert_colombia_units_of_measure.sql**
```sql
INSERT INTO units_of_measure (id, name, abbreviation, active, created_at, created_by) VALUES
-- Cantidad
(UNHEX(REPLACE(UUID(), '-', '')), 'Unidad', 'UN', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Docena', 'DOC', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Par', 'PAR', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),

-- Empaque
(UNHEX(REPLACE(UUID(), '-', '')), 'Caja', 'CJ', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Paquete', 'PQ', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Bulto', 'BL', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),

-- Peso
(UNHEX(REPLACE(UUID(), '-', '')), 'Gramo', 'GR', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Kilogramo', 'KG', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Tonelada', 'TON', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),

-- Volumen
(UNHEX(REPLACE(UUID(), '-', '')), 'Mililitro', 'ML', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Litro', 'L', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Galón', 'GAL', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),

-- Longitud
(UNHEX(REPLACE(UUID(), '-', '')), 'Centímetro', 'CM', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),
(UNHEX(REPLACE(UUID(), '-', '')), 'Metro', 'M', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))),

-- Área
(UNHEX(REPLACE(UUID(), '-', '')), 'Metro Cuadrado', 'M²', TRUE, NOW(), UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', '')));
```

---

## 📊 Performance Considerations

### Database Optimization

1. **Indexes**: name, abbreviation, active
2. **Query optimization**: Use covering indexes
3. **Connection pooling**: HikariCP con 10-20 connections

### Caching Strategy

- **Cache Level**: Application-level (Spring Cache)
- **Cache Provider**: Caffeine
- **TTL**: 1 hora para listados
- **Invalidation**: On create/update/delete

### Performance Goals

| Operation | Target | Measurement |
|-----------|--------|-------------|
| Create | < 100ms p95 | JMeter |
| Get by UUID | < 50ms p95 | APM logs |
| List (20 items) | < 100ms p95 | APM logs |
| Search | < 150ms p95 | JMeter |

---

## ⚙️ Configuration

### Application Properties

```properties
# Units of Measure Module Configuration
erplite.unitofmeasure.default-page-size=20
erplite.unitofmeasure.max-page-size=100

# Caching
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=1h
```

---

## 🧪 Testing Strategy

### Unit Tests

- **Domain model tests**: Business logic methods
- **Domain service tests**: Validation logic
- **Use case tests**: Mocked ports
- **Mapper tests**: Bidirectionality

### Integration Tests

- **Repository tests**: Testcontainers MySQL
- **Controller tests**: MockMvc
- **End-to-end API tests**: RestAssured

### Test Coverage Target

- **Overall**: >= 85%
- **Domain layer**: >= 95%
- **Application layer**: >= 90%
- **Infrastructure layer**: >= 75%

---

## 🔒 Security Considerations

### Authentication & Authorization

| Endpoint | Required Role |
|----------|--------------|
| POST /api/units-of-measure | ADMIN |
| PUT /api/units-of-measure/{id} | ADMIN |
| DELETE /api/units-of-measure/{id} | ADMIN |
| PATCH /api/units-of-measure/{id}/activate | ADMIN |
| GET /api/units-of-measure | USER |
| GET /api/units-of-measure/{id} | USER |
| GET /api/units-of-measure/search | USER |

### Input Validation

- Jakarta Bean Validation on DTOs
- Domain-level validation in services
- SQL injection prevention via JPA

---

## 📝 API Documentation

### Swagger/OpenAPI 3.0

- **Title**: ERP Lite - Units of Measure API
- **Version**: 1.0
- **Base URL**: `/api/units-of-measure`
- **Authentication**: Bearer JWT

**Example Request**:
```bash
curl -X POST http://localhost:8080/api/units-of-measure \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name": "Caja", "abbreviation": "CJ"}'
```

**Example Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Caja",
  "abbreviation": "CJ",
  "active": true,
  "createdAt": "2026-02-01T10:30:00Z",
  "createdBy": "123e4567-e89b-12d3-a456-426614174000",
  "updatedAt": null,
  "updatedBy": null
}
```

---

## 🔗 External Dependencies

### Maven Dependencies

```xml
<!-- Domain & Application (No external dependencies) -->

<!-- Infrastructure -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
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

**Status**: ⚠️ PHASE 2 - Technical Draft Refinement (v1.1)  
**Next Step**: Technical Review (Tech Lead) → Resolve design decisions → Approve → Move to PHASE 3
