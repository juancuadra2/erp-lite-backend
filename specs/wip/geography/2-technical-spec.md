# Technical Specification: Módulo de Geografía

**Feature**: Geography Module (Departments & Municipalities)  
**Created**: January 10, 2026  
**Related**: [functional-spec.md](1-functional-spec.md)  
**Phase**: PHASE 2 - Draft

---

## 📐 Technical Overview

Implementación del módulo de Geografía siguiendo **Arquitectura Hexagonal** (Ports & Adapters), con separación estricta entre dominio, aplicación e infraestructura. El módulo gestiona la jerarquía de ubicaciones administrativas (Departamento > Municipio) sin dependencias externas.

### Architecture Style
- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Layers**: Domain → Application → Infrastructure
- **Communication**: Inbound/Outbound Ports

---

## 🏗️ Architecture Layers

### Domain Layer (Core Business Logic)

**Purpose**: Lógica de negocio pura, sin dependencias externas

**Components**:

#### Models (`domain/geography/model/`)
```java
// Department.java - Aggregate Root
public class Department {
    private Long id;
    private UUID uuid;
    private String code;          // 2 dígitos
    private String name;
    private Boolean enabled;
    private User createdBy;
    private User updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Municipality> municipalities;
    
    // Business methods
    public void activate() { this.enabled = true; }
    public void deactivate() { this.enabled = false; }
    public boolean canBeDeleted() { 
        return municipalities == null || municipalities.isEmpty(); 
    }
}

// Municipality.java - Entity
public class Municipality {
    private Long id;
    private UUID uuid;
    private String code;          // 5 dígitos
    private String name;
    private Department department;
    private Boolean enabled;
    private User createdBy;
    private User updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business methods
    public void activate() { this.enabled = true; }
    public void deactivate() { this.enabled = false; }
}
```

#### Services (`domain/geography/service/`)
```java
// GeographyDomainService.java
@Service
public class GeographyDomainService {
    public void validateDepartmentCode(String code) {
        if (!code.matches("\\d{2}")) {
            throw new InvalidCodeFormatException("Department code must be 2 digits");
        }
    }
    
    public void validateMunicipalityCode(String code) {
        if (!code.matches("\\d{5}")) {
            throw new InvalidCodeFormatException("Municipality code must be 5 digits");
        }
    }
    
    public boolean canDeleteDepartment(Department department) {
        return department.getMunicipalities() == null || 
               department.getMunicipalities().isEmpty();
    }
}

// GeographyValidationService.java
@Service
public class GeographyValidationService {
    private final DepartmentPort departmentPort;
    private final MunicipalityPort municipalityPort;
    
    public void ensureDepartmentCodeUnique(String code, UUID excludeId) {
        // Check uniqueness excluding current entity
    }
    
    public void ensureMunicipalityCodeUniqueInDepartment(
        UUID departmentId, String code, UUID excludeId) {
        // Check uniqueness within department
    }
}
```

#### Exceptions (`domain/geography/exception/`)
```java
public class DepartmentNotFoundException extends DomainException {}
public class MunicipalityNotFoundException extends DomainException {}
public class DuplicateCodeException extends BusinessRuleException {}
public class GeographyConstraintException extends BusinessRuleException {}
```

---

### Application Layer (Use Cases)

**Purpose**: Orquestación de casos de uso, lógica de aplicación

**Ports** (`application/port/`):

#### Input Ports (Use Cases) - `application/port/in/`

**Department Use Cases** (`application/port/in/department/`)
```java
// CreateDepartmentUseCase.java
public interface CreateDepartmentUseCase {
    Department execute(CreateDepartmentRequestDto request);
}

// GetDepartmentUseCase.java
public interface GetDepartmentUseCase {
    Department execute(UUID uuid);
}

// UpdateDepartmentUseCase.java
public interface UpdateDepartmentUseCase {
    Department execute(UUID uuid, UpdateDepartmentRequestDto request);
}

// ListDepartmentsUseCase.java
public interface ListDepartmentsUseCase {
    Page<Department> execute(Boolean enabled, String name, Pageable pageable);
}

// DeactivateDepartmentUseCase.java
public interface DeactivateDepartmentUseCase {
    void execute(UUID uuid);
}

// ActivateDepartmentUseCase.java
public interface ActivateDepartmentUseCase {
    void execute(UUID uuid);
}

// DeleteDepartmentUseCase.java
public interface DeleteDepartmentUseCase {
    void execute(UUID uuid);
}
```

**Municipality Use Cases** (`application/port/in/municipality/`)
```java
// CreateMunicipalityUseCase.java
public interface CreateMunicipalityUseCase {
    Municipality execute(CreateMunicipalityRequestDto request);
}

// GetMunicipalityUseCase.java
public interface GetMunicipalityUseCase {
    Municipality execute(UUID uuid);
}

// UpdateMunicipalityUseCase.java
public interface UpdateMunicipalityUseCase {
    Municipality execute(UUID uuid, UpdateMunicipalityRequestDto request);
}

// ListMunicipalitiesUseCase.java
public interface ListMunicipalitiesUseCase {
    Page<Municipality> execute(UUID departmentId, Boolean enabled, String name, Pageable pageable);
}

// DeactivateMunicipalityUseCase.java
public interface DeactivateMunicipalityUseCase {
    void execute(UUID uuid);
}

// ActivateMunicipalityUseCase.java
public interface ActivateMunicipalityUseCase {
    void execute(UUID uuid);
}

// DeleteMunicipalityUseCase.java
public interface DeleteMunicipalityUseCase {
    void execute(UUID uuid);
}
```

#### Output Ports (Repository Interfaces) - `application/port/out/`

```java
// DepartmentPort.java
public interface DepartmentPort {
    Department save(Department department);
    Optional<Department> findByUuid(UUID uuid);
    Optional<Department> findByCode(String code);
    Optional<Department> findById(Long id);
    Page<Department> findAll(Pageable pageable);
    Page<Department> findByEnabled(Boolean enabled, Pageable pageable);
    Page<Department> findByNameContaining(String name, Pageable pageable);
    List<Department> findAllEnabled();
    void delete(Department department);
    boolean existsByCode(String code);
    long count();
}

// MunicipalityPort.java
public interface MunicipalityPort {
    Municipality save(Municipality municipality);
    Optional<Municipality> findByUuid(UUID uuid);
    Optional<Municipality> findByCodeAndDepartmentId(String code, Long departmentId);
    Page<Municipality> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Municipality> findByDepartmentIdAndEnabled(Long departmentId, Boolean enabled, Pageable pageable);
    Page<Municipality> findByNameContaining(String name, Pageable pageable);
    Page<Municipality> findAll(Pageable pageable);
    List<Municipality> findAllEnabled();
    void delete(Municipality municipality);
    boolean existsByCodeAndDepartmentId(String code, Long departmentId);
    long countByDepartmentId(Long departmentId);
}
```

#### Service Implementations - `application/service/geography/`

**Department Services** (`application/service/geography/department/`)
```java
// CreateDepartmentService.java
@Service
@RequiredArgsConstructor
public class CreateDepartmentService implements CreateDepartmentUseCase {
    private final DepartmentPort departmentPort;
    private final GeographyDomainService domainService;
    
    @Override
    @Transactional
    public Department execute(CreateDepartmentRequestDto request) {
        // Implementation
    }
}

// GetDepartmentService.java
@Service
@RequiredArgsConstructor
public class GetDepartmentService implements GetDepartmentUseCase {
    private final DepartmentPort departmentPort;
    
    @Override
    @Transactional(readOnly = true)
    public Department execute(UUID uuid) {
        // Implementation
    }
}

// UpdateDepartmentService.java, DeactivateDepartmentService.java, etc.
```

**Municipality Services** (`application/service/geography/municipality/`)
```java
// CreateMunicipalityService.java
@Service
@RequiredArgsConstructor
public class CreateMunicipalityService implements CreateMunicipalityUseCase {
    private final MunicipalityPort municipalityPort;
    private final DepartmentPort departmentPort;
    private final GeographyDomainService domainService;
    
    @Override
    @Transactional
    public Municipality execute(CreateMunicipalityRequestDto request) {
        // Implementation
    }
}

// GetMunicipalityService.java, UpdateMunicipalityService.java, etc.
}
```

---

### Infrastructure Layer (Adapters)

**Purpose**: Implementación de detalles técnicos (DB, API, etc.)

#### Output Adapters (Persistence)

**Entities** (`infrastructure/out/geography/persistence/entity/`)
```java
@Entity
@Table(name = "departments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 36)
    private UUID uuid;
    
    @Column(nullable = false, unique = true, length = 10)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<MunicipalityEntity> municipalities;
    
    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

@Entity
@Table(name = "municipalities", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"code", "department_id"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MunicipalityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 36)
    private UUID uuid;
    
    @Column(nullable = false, length = 10)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity department;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**Repositories** (`infrastructure/out/geography/persistence/repository/`)
```java
@Repository
public interface DepartmentJpaRepository 
    extends JpaRepository<DepartmentEntity, Long> {
    
    Optional<DepartmentEntity> findByUuid(UUID uuid);
    Optional<DepartmentEntity> findByCode(String code);
    Page<DepartmentEntity> findByEnabled(Boolean enabled, Pageable pageable);
    Page<DepartmentEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<DepartmentEntity> findByEnabledTrue();
    boolean existsByCode(String code);
}

@Repository
public interface MunicipalityJpaRepository 
    extends JpaRepository<MunicipalityEntity, Long> {
    
    Optional<MunicipalityEntity> findByUuid(UUID uuid);
    Optional<MunicipalityEntity> findByCodeAndDepartmentId(String code, Long departmentId);
    Page<MunicipalityEntity> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<MunicipalityEntity> findByDepartmentIdAndEnabled(Long departmentId, Boolean enabled, Pageable pageable);
    Page<MunicipalityEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<MunicipalityEntity> findByEnabledTrue();
    boolean existsByCodeAndDepartmentId(String code, Long departmentId);
    long countByDepartmentId(Long departmentId);
}
```

**Entity Mappers** (`infrastructure/out/geography/mapper/`)
```java
@Mapper(componentModel = "spring")
public interface DepartmentEntityMapper {
    Department toDomain(DepartmentEntity entity);
    DepartmentEntity toEntity(Department domain);
    List<Department> toDomainList(List<DepartmentEntity> entities);
}

@Mapper(componentModel = "spring")
public interface MunicipalityEntityMapper {
    @Mapping(target = "department.municipalities", ignore = true)
    Municipality toDomain(MunicipalityEntity entity);
    
    @Mapping(target = "department.municipalities", ignore = true)
    MunicipalityEntity toEntity(Municipality domain);
    
    List<Municipality> toDomainList(List<MunicipalityEntity> entities);
}
```

**Adapters** (`infrastructure/out/geography/persistence/adapter/`)
```java
@Component
@RequiredArgsConstructor
public class DepartmentRepositoryAdapter implements DepartmentPort {
    private final DepartmentJpaRepository jpaRepository;
    private final DepartmentEntityMapper mapper;
    
    @Override
    public Department save(Department department) {
        DepartmentEntity entity = mapper.toEntity(department);
        DepartmentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Department> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid)
                .map(mapper::toDomain);
    }
    
    // Other methods...
}

@Component
@RequiredArgsConstructor
public class MunicipalityRepositoryAdapter implements MunicipalityPort {
    private final MunicipalityJpaRepository jpaRepository;
    private final MunicipalityEntityMapper mapper;
    
    // Similar implementation...
}
```

#### Input Adapters (REST API)

**Controllers** (`infrastructure/in/api/geography/rest/`)
```java
@RestController
@RequestMapping("/api/geography/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final CreateDepartmentUseCase createUseCase;
    private final GetDepartmentUseCase getUseCase;
    private final UpdateDepartmentUseCase updateUseCase;
    private final ListDepartmentsUseCase listUseCase;
    private final DeactivateDepartmentUseCase deactivateUseCase;
    private final ActivateDepartmentUseCase activateUseCase;
    private final DeleteDepartmentUseCase deleteUseCase;
    private final DepartmentDtoMapper dtoMapper;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponseDto create(
        @Valid @RequestBody CreateDepartmentRequestDto request) {
        Department department = createUseCase.execute(request);
        return dtoMapper.toResponseDto(department);
    }
    
    @GetMapping("/{uuid}")
    public DepartmentResponseDto getByUuid(@PathVariable UUID uuid) {
        Department department = getUseCase.execute(uuid);
        return dtoMapper.toResponseDto(department);
    }
    
    @GetMapping
    public Page<DepartmentResponseDto> list(
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(required = false) String name,
        Pageable pageable) {
        Page<Department> departments = listUseCase.execute(enabled, name, pageable);
        return departments.map(dtoMapper::toResponseDto);
    }
    
    // Other endpoints...
}

@RestController
@RequestMapping("/api/geography/municipalities")
@RequiredArgsConstructor
public class MunicipalityController {
    private final CreateMunicipalityUseCase createUseCase;
    private final GetMunicipalityUseCase getUseCase;
    private final UpdateMunicipalityUseCase updateUseCase;
    private final ListMunicipalitiesUseCase listUseCase;
    private final DeactivateMunicipalityUseCase deactivateUseCase;
    private final ActivateMunicipalityUseCase activateUseCase;
    private final DeleteMunicipalityUseCase deleteUseCase;
    private final MunicipalityDtoMapper dtoMapper;
    
    // Similar structure...
}
```

**DTOs** (`infrastructure/in/api/geography/dto/`)

Department DTOs (`infrastructure/in/api/geography/dto/department/`)
```java
// CreateDepartmentRequestDto.java
public record CreateDepartmentRequestDto(
    @NotBlank @Pattern(regexp = "\\d{2}") String code,
    @NotBlank @Size(max = 100) String name,
    @NotNull Boolean enabled
) {}

// UpdateDepartmentRequestDto.java
public record UpdateDepartmentRequestDto(
    @NotBlank @Size(max = 100) String name,
    @NotNull Boolean enabled
) {}

// DepartmentResponseDto.java
public record DepartmentResponseDto(
    UUID uuid,
    String code,
    String name,
    Boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

Municipality DTOs (`infrastructure/in/api/geography/dto/municipality/`)
```java
// CreateMunicipalityRequestDto.java
public record CreateMunicipalityRequestDto(
    @NotNull Long departmentId,
    @NotBlank @Pattern(regexp = "\\d{5}") String code,
    @NotBlank @Size(max = 100) String name,
    @NotNull Boolean enabled
) {}

// UpdateMunicipalityRequestDto.java
public record UpdateMunicipalityRequestDto(
    @NotBlank @Size(max = 100) String name,
    @NotNull Boolean enabled
) {}

// MunicipalityResponseDto.java
public record MunicipalityResponseDto(
    UUID uuid,
    String code,
    String name,
    DepartmentResponseDto department,
    Boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**DTO Mappers** (`infrastructure/in/api/geography/mapper/`)
```java
// DepartmentDtoMapper.java
@Mapper(componentModel = "spring")
public interface DepartmentDtoMapper {
    DepartmentResponseDto toResponseDto(Department department);
    List<DepartmentResponseDto> toResponseDtoList(List<Department> departments);
}

// MunicipalityDtoMapper.java
@Mapper(componentModel = "spring", uses = {DepartmentDtoMapper.class})
public interface MunicipalityDtoMapper {
    MunicipalityResponseDto toResponseDto(Municipality municipality);
    List<MunicipalityResponseDto> toResponseDtoList(List<Municipality> municipalities);
}
public record DepartmentResponseDto(
    Long id,
    UUID uuid,
    String code,
    String name,
    Boolean enabled,
    UserSummaryDto createdBy,
    UserSummaryDto updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

// CreateMunicipalityRequestDto.java
public record CreateMunicipalityRequestDto(
    @NotNull UUID departmentId,
    @NotBlank @Pattern(regexp = "\\d{5}") String code,
    @NotBlank @Size(max = 100) String name,
    @NotNull Boolean enabled
) {}

// MunicipalityResponseDto.java
public record MunicipalityResponseDto(
    Long id,
    UUID uuid,
    String code,
    String name,
    DepartmentSummaryDto department,
    Boolean enabled,
    UserSummaryDto createdBy,
    UserSummaryDto updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

// MunicipalityAutocompleteDto.java
public record MunicipalityAutocompleteDto(
    UUID uuid,
    String name,
    String departmentName
) {}
```

---

## 🌐 API Endpoints

### Department Endpoints

**Base Path**: `/api/geography/departments`

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/` | Crear nuevo departamento | 201, 400, 409 |
| GET | `/{uuid}` | Obtener departamento por UUID | 200, 404 |
| PUT | `/{uuid}` | Actualizar departamento | 200, 400, 404, 409 |
| DELETE | `/{uuid}` | Eliminar departamento | 204, 404, 409 |
| PATCH | `/{uuid}/activate` | Activar departamento | 200, 404 |
| PATCH | `/{uuid}/deactivate` | Desactivar departamento | 200, 404 |
| GET | `/` | Listar departamentos (paginado) | 200 |

#### Ejemplos de Requests/Responses

**POST /api/geography/departments**
```json
Request:
{
  "code": "05",
  "name": "Antioquia",
  "enabled": true
}

Response (201 Created):
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "05",
  "name": "Antioquia",
  "enabled": true,
  "createdAt": "2026-02-06T10:30:00",
  "updatedAt": "2026-02-06T10:30:00"
}
```

**GET /api/geography/departments?enabled=true&page=0&size=20**
```json
Response (200 OK):
{
  "content": [
    {
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "code": "05",
      "name": "Antioquia",
      "enabled": true,
      "createdAt": "2026-02-06T10:30:00",
      "updatedAt": "2026-02-06T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 32,
  "totalPages": 2
}
```

### Municipality Endpoints

**Base Path**: `/api/geography/municipalities`

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/` | Crear nuevo municipio | 201, 400, 404, 409 |
| GET | `/{uuid}` | Obtener municipio por UUID | 200, 404 |
| PUT | `/{uuid}` | Actualizar municipio | 200, 400, 404, 409 |
| DELETE | `/{uuid}` | Eliminar municipio | 204, 404, 409 |
| PATCH | `/{uuid}/activate` | Activar municipio | 200, 404 |
| PATCH | `/{uuid}/deactivate` | Desactivar municipio | 200, 404 |
| GET | `/` | Listar municipios (paginado) | 200 |

#### Ejemplos de Requests/Responses

**POST /api/geography/municipalities**
```json
Request:
{
  "departmentId": 1,
  "code": "05001",
  "name": "Medellín",
  "enabled": true
}

Response (201 Created):
{
  "uuid": "660e8400-e29b-41d4-a716-446655440001",
  "code": "05001",
  "name": "Medellín",
  "department": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "code": "05",
    "name": "Antioquia",
    "enabled": true,
    "createdAt": "2026-02-06T10:30:00",
    "updatedAt": "2026-02-06T10:30:00"
  },
  "enabled": true,
  "createdAt": "2026-02-06T11:00:00",
  "updatedAt": "2026-02-06T11:00:00"
}
```

**GET /api/geography/municipalities?departmentId=1&enabled=true&page=0&size=50**
```json
Response (200 OK):
{
  "content": [
    {
      "uuid": "660e8400-e29b-41d4-a716-446655440001",
      "code": "05001",
      "name": "Medellín",
      "department": {
        "uuid": "550e8400-e29b-41d4-a716-446655440000",
        "code": "05",
        "name": "Antioquia",
        "enabled": true
      },
      "enabled": true,
      "createdAt": "2026-02-06T11:00:00",
      "updatedAt": "2026-02-06T11:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50
  },
  "totalElements": 125,
  "totalPages": 3
}
```

### Error Responses

**400 Bad Request** - Validation Error
```json
{
  "timestamp": "2026-02-06T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "code",
      "message": "must match \"\\d{2}\""
    }
  ],
  "path": "/api/geography/departments"
}
```

**404 Not Found**
```json
{
  "timestamp": "2026-02-06T11:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Department not found with uuid: 550e8400-e29b-41d4-a716-446655440000",
  "path": "/api/geography/departments/550e8400-e29b-41d4-a716-446655440000"
}
```

**409 Conflict**
```json
{
  "timestamp": "2026-02-06T11:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Department code already exists: 05",
  "path": "/api/geography/departments"
}
```

---

## 🗄️ Database Schema

### Tables

```sql
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_by_id BIGINT,
    updated_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    
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
    enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_by_id BIGINT,
    updated_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    
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

### Indexes Strategy

| Index | Purpose | Impact |
|-------|---------|--------|
| `idx_departments_code` | Búsqueda por código DANE | High |
| `idx_departments_name` | Búsqueda por nombre | Medium |
| `idx_departments_uuid` | Lookup por UUID en API | High |
| `idx_municipalities_name` | Autocompletado | High |
| `idx_municipalities_department_id` | Filtro por departamento | High |
| `uk_municipality_code_department` | Unicidad compuesta | High |

### Constraints

1. **Uniqueness**:
   - `departments.code` → global unique
   - `municipalities.(code, department_id)` → unique per department

2. **Foreign Keys**:
   - `municipalities.department_id → departments.id` con `ON DELETE RESTRICT`
   - Previene eliminación de departamentos con municipios

3. **Not Null**:
   - `code`, `name`, `enabled` obligatorios
   - `department_id` obligatorio en municipalities

---

## 🔄 Data Flow

### Example: Create Municipality

```
1. REST API Request
   POST /api/municipalities
   Body: {departmentId, code, name, enabled}
   ↓
2. Controller
   DepartmentController.create()
   Validate DTO
   ↓
3. Use Case (Application Layer)
   CreateMunicipalityUseCase.execute()
   - Validate department exists
   - Validate code unique in department
   - Create domain object
   ↓
4. Domain Service
   GeographyDomainService.validateMunicipalityCode()
   GeographyValidationService.ensureCodeUnique()
   ↓
5. Port (Repository Interface)
   MunicipalityPort.save()
   ↓
6. Adapter (Infrastructure)
   MunicipalityRepositoryAdapter.save()
   - Map domain → entity
   - Save via JPA
   - Map entity → domain
   ↓
7. Response
   MunicipalityResponseDto
```

---

## 🔧 Technical Decisions

### TD-01: MapStruct for Mapping
**Decision**: Usar MapStruct para todos los mappers  
**Rationale**: Type-safe, compile-time, mejor performance que reflection  
**Alternatives**: ModelMapper (descartado: runtime overhead)

### TD-02: UUID for Public IDs
**Decision**: Exponer UUID en API, no IDs numéricos  
**Rationale**: Seguridad, evita enumeration attacks  
**Implementation**: UUID v4 generado automáticamente

### TD-03: Soft Delete Strategy
**Decision**: Campo `enabled` en lugar de eliminación física  
**Rationale**: Auditoría, recuperación de datos, integridad histórica  
**Implementation**: Filtro por defecto en queries

### TD-04: Flyway Migrations
**Decision**: Flyway para versionado de schema  
**Rationale**: Reproducible, auditable, rollback capability  
**Naming**: `V{major}.{minor}__{description}.sql`

### TD-05: Testcontainers for Integration Tests
**Decision**: MySQL real via Testcontainers  
**Rationale**: Tests más confiables que H2 in-memory  
**Trade-off**: Tiempo de ejecución mayor (acceptable)

---

## 📊 Performance Considerations

### Optimization Strategies

1. **Indexes**:
   - Índices en campos de búsqueda frecuente (code, name, uuid)
   - Índice compuesto en (department_id, enabled) para municipios

2. **Lazy Loading**:
   - Relaciones `@OneToMany` con `FetchType.LAZY`
   - Cargar solo cuando necesario

3. **Query Optimization**:
   - Paginación obligatoria en listados
   - Limit en autocompletado (default: 10)

4. **Caching** (Future):
   - Cache de departamentos activos (raramente cambian)
   - TTL: 1 hora

### Performance Goals

| Operation | Target | Measurement |
|-----------|--------|-------------|
| Autocompletado | < 100ms p95 | JMeter |
| Listado paginado | < 200ms p95 | JMeter |
| Get by UUID | < 50ms p95 | Application logs |
| Importación masiva | < 30s total | Manual timing |

---

## 🔐 Security Considerations

### Authentication
- JWT bearer token requerido en todos los endpoints
- Header: `Authorization: Bearer <token>`

### Authorization
- **SUPERADMIN, ADMIN**: CRUD completo
- **USER**: Solo READ operations (GET)

### Input Validation
- Bean Validation en DTOs (`@NotBlank`, `@Size`, `@Pattern`)
- Validación de códigos DANE (formato correcto)
- Prevención de SQL Injection (uso de JPA/Hibernate)

### Audit Trail
- Registrar `createdBy`, `updatedBy` en todas las operaciones
- Timestamps automáticos (`createdAt`, `updatedAt`)

---

## 🧪 Testing Strategy

### Unit Tests
- **Target**: Domain models, services, validators
- **Coverage**: > 80%
- **Tools**: JUnit 5, Mockito
- **Focus**: Business logic, validations

### Integration Tests
- **Target**: Repositories, adapters
- **Tools**: Testcontainers (MySQL), Spring Boot Test
- **Focus**: Database interactions, constraints

### API Tests
- **Target**: Controllers
- **Tools**: MockMvc, RestAssured
- **Focus**: HTTP status, headers, body, error handling

### E2E Tests
- **Target**: Complete flows
- **Scenarios**:
  - Create department → Create municipality → List → Search
  - Try delete department with municipalities → Verify 409

### Performance Tests
- **Tool**: JMeter
- **Scenarios**:
  - Autocompletado con 1,100 municipios
  - Listado paginado con filtros

---

## 📦 Dependencies

### Core Dependencies
```xml
<dependencies>
    <!-- Spring Boot -->
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
    
    <!-- Mapping -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- Utilities -->
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
</dependencies>
```

### No External Module Dependencies
Este módulo **NO** depende de otros módulos del sistema (catálogo base independiente).

---

##  API Documentation

### OpenAPI/Swagger
- URL: `/swagger-ui.html`
- Spec: `/v3/api-docs`

### Example Request/Response

**Create Department**:
```bash
POST /api/geography/departments
Content-Type: application/json
Authorization: Bearer <token>

{
  "code": "05",
  "name": "Antioquia",
  "enabled": true
}

Response 201:
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "05",
  "name": "Antioquia",
  "enabled": true,
  "createdBy": {"id": 1, "username": "admin"},
  "updatedBy": {"id": 1, "username": "admin"},
  "createdAt": "2026-01-10T10:00:00.000Z",
  "updatedAt": "2026-01-10T10:00:00.000Z"
}
```

---

## 🔄 Integration Points

### Modules that will consume Geography:
- **Company**: Select department/municipality for company address
- **Contact**: Select department/municipality for contact address
- **Warehouse**: Select department/municipality for warehouse location
- **Sales**: Filter sales by geographic region
- **Purchases**: Filter suppliers by location

### Integration Contract:
```java
// Other modules will use:
GET /api/geography/departments/{uuid}
GET /api/geography/municipalities/{uuid}
GET /api/geography/municipalities/autocomplete?query=...
```

---

**Status**: ⚠️ PHASE 2 - Draft  
**Next Step**: Review → Clarify → Approve → Move to PHASE 3
