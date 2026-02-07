# Technical Specification: Módulo de Geografía

**Feature**: Geography Module (Departments & Municipalities)  
**Created**: January 10, 2026  
**Updated**: February 7, 2026  
**Related**: [functional-spec.md](1-functional-spec.md)  
**Phase**: PHASE 2 - Draft  
**Arquitectura:** Hexagonal (Puertos y Adaptadores)

---

## 📐 Technical Overview

Implementación del módulo de Geografía siguiendo **Arquitectura Hexagonal** (Ports & Adapters) alineada con el nuevo scaffolding, con separación estricta entre dominio, aplicación e infraestructura. Cada feature tiene su propia carpeta dentro de cada capa para mantener el código organizado y escalable.

### Architecture Style
- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Layers**: Domain → Application → Infrastructure
- **Communication**: Inbound/Outbound Ports
- **Organization**: Feature-based folders within each layer

### Stack Tecnológico
- **Lenguaje**: Java 21
- **Framework**: Spring Boot 3.x
- **ORM**: Spring Data JPA con Hibernate
- **Base de Datos**: MySQL 8.0+
- **Migraciones**: Flyway
- **Mapeo**: MapStruct 1.5.5
- **Validación**: Hibernate Validator (Bean Validation)
- **Pruebas**: JUnit 5, Mockito
- **Documentación de API**: SpringDoc OpenAPI 3

---

## Estructura de Paquetes (Organizada por Features)

**NOTA**: Cada feature tiene su propia carpeta dentro de cada capa para mantener el código organizado y escalable.

```
com.jcuadrado.erplitebackend/
│
├── domain/
│   ├── model/
│   │   └── geography/
│   │       ├── Department.java                      # Raíz Agregada
│   │       └── Municipality.java                    # Entidad
│   ├── service/
│   │   └── geography/
│   │       ├── GeographyDomainService.java          # Reglas de negocio
│   │       └── GeographyValidationService.java      # Validación de dominio
│   ├── port/
│   │   └── out/
│   │       └── geography/
│   │           ├── DepartmentRepository.java        # Puerto de salida
│   │           └── MunicipalityRepository.java      # Puerto de salida
│   └── exception/
│       └── geography/
│           ├── DepartmentNotFoundException.java
│           ├── MunicipalityNotFoundException.java
│           ├── DuplicateCodeException.java
│           ├── InvalidCodeFormatException.java
│           └── GeographyConstraintException.java
│
├── application/
│   ├── port/
│   │   └── in/
│   │       └── geography/
│   │           ├── CompareDepartmentsUseCase.java        # Operaciones de consulta
│   │           ├── ManageDepartmentUseCase.java          # Operaciones de comando
│   │           ├── CompareMunicipalitiesUseCase.java     # Operaciones de consulta
│   │           └── ManageMunicipalityUseCase.java        # Operaciones de comando
│   └── usecase/
│       └── geography/
│           ├── CompareDepartmentsUseCaseImpl.java
│           ├── ManageDepartmentUseCaseImpl.java
│           ├── CompareMunicipalitiesUseCaseImpl.java
│           └── ManageMunicipalityUseCaseImpl.java
│
└── infrastructure/
    ├── config/
    │   └── BeanConfiguration.java                   # Configuración compartida
    │
    ├── in/
    │   └── web/
    │       ├── controller/
    │       │   └── geography/
    │       │       ├── DepartmentController.java
    │       │       └── MunicipalityController.java
    │       ├── dto/
    │       │   └── geography/
    │       │       ├── CreateDepartmentRequestDto.java
    │       │       ├── UpdateDepartmentRequestDto.java
    │       │       ├── DepartmentResponseDto.java
    │       │       ├── CreateMunicipalityRequestDto.java
    │       │       ├── UpdateMunicipalityRequestDto.java
    │       │       └── MunicipalityResponseDto.java
    │       ├── mapper/
    │       │   └── geography/
    │       │       ├── DepartmentDtoMapper.java
    │       │       └── MunicipalityDtoMapper.java
    │       └── advice/
    │           └── GlobalExceptionHandler.java       # Compartido entre features
    │
    └── out/
        └── persistence/
            ├── adapter/
            │   └── geography/
            │       ├── DepartmentRepositoryAdapter.java
            │       └── MunicipalityRepositoryAdapter.java
            ├── entity/
            │   └── geography/
            │       ├── DepartmentEntity.java
            │       └── MunicipalityEntity.java
            ├── mapper/
            │   └── geography/
            │       ├── DepartmentEntityMapper.java
            │       └── MunicipalityEntityMapper.java
            └── jpa/
                └── geography/
                    ├── DepartmentJpaRepository.java
                    └── MunicipalityJpaRepository.java
```

**Ventajas de la Estructura por Features:**
1. **Escalabilidad**: Cada nuevo feature se agrega en su propia carpeta sin afectar otros
2. **Cohesión**: Todo el código relacionado con geography está agrupado en cada capa
3. **Mantenibilidad**: Fácil encontrar y modificar código específico del feature
4. **Claridad**: La estructura refleja los módulos de negocio del sistema
5. **Independencia**: Cada feature puede evolucionar independientemente

---

## 🏗️ Architecture Layers

### Domain Layer (Core Business Logic)

**Purpose**: Lógica de negocio pura, sin dependencias externas

**Components**:

#### Models (`domain/model/geography/`)
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

#### Services (`domain/service/geography/`)
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

#### Exceptions (`domain/exception/geography/`)
```java
public class DepartmentNotFoundException extends DomainException {}
public class MunicipalityNotFoundException extends DomainException {}
public class DuplicateCodeException extends BusinessRuleException {}
public class GeographyConstraintException extends BusinessRuleException {}
```

---

### Application Layer (Use Cases)

**Purpose**: Orquestación de casos de uso, lógica de aplicación

#### Input Ports (Use Cases) - `application/port/in/geography/`

```java
/**
 * Use case for querying departments (Queries: get, list, search, compare)
 */
public interface CompareDepartmentsUseCase {
    /**
     * Get department by UUID
     */
    Department getByUuid(UUID uuid);
    
    /**
     * Get department by code
     */
    Department getByCode(String code);
    
    /**
     * Get all active departments
     */
    List<Department> getAllActive();
    
    /**
     * Find all departments with filters and pagination
     * @param filters Map with filter criteria (enabled, search, etc.)
     * @param pageable Pagination and sorting information
     */
    Page<Department> findAll(Map<String, Object> filters, Pageable pageable);
}

/**
 * Use case for managing departments (Commands: create, update, delete, activate/deactivate)
 */
public interface ManageDepartmentUseCase {
    /**
     * Create a new department
     */
    Department create(Department department);
    
    /**
     * Update an existing department
     */
    Department update(UUID uuid, Department department);
    
    /**
     * Delete a department (soft delete)
     */
    void delete(UUID uuid);
    
    /**
     * Activate a department
     */
    void activate(UUID uuid);
    
    /**
     * Deactivate a department
     */
    void deactivate(UUID uuid);
}

/**
 * Use case for querying municipalities (Queries: get, list, search, compare)
 */
public interface CompareMunicipalitiesUseCase {
    /**
     * Get municipality by UUID
     */
    Municipality getByUuid(UUID uuid);
    
    /**
     * Get municipality by code and department
     */
    Municipality getByCodeAndDepartment(String code, Long departmentId);
    
    /**
     * Get all active municipalities
     */
    List<Municipality> getAllActive();
    
    /**
     * Find municipalities by department
     */
    Page<Municipality> findByDepartment(UUID departmentUuid, Pageable pageable);
    
    /**
     * Find all municipalities with filters and pagination
     * @param filters Map with filter criteria (departmentId, enabled, search, etc.)
     * @param pageable Pagination and sorting information
     */
    Page<Municipality> findAll(Map<String, Object> filters, Pageable pageable);
}

/**
 * Use case for managing municipalities (Commands: create, update, delete, activate/deactivate)
 */
public interface ManageMunicipalityUseCase {
    /**
     * Create a new municipality
     */
    Municipality create(Municipality municipality);
    
    /**
     * Update an existing municipality
     */
    Municipality update(UUID uuid, Municipality municipality);
    
    /**
     * Delete a municipality (soft delete)
     */
    void delete(UUID uuid);
    
    /**
     * Activate a municipality
     */
    void activate(UUID uuid);
    
    /**
     * Deactivate a municipality
     */
    void deactivate(UUID uuid);
}
```

#### Output Ports (Repository Interfaces) - `domain/port/out/geography/`

```java
// DepartmentRepository.java
public interface DepartmentRepository {
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

// MunicipalityRepository.java
public interface MunicipalityRepository {
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

#### Use Case Implementations - `application/usecase/geography/`

```java
// CompareDepartmentsUseCaseImpl.java
@Service
@RequiredArgsConstructor
public class CompareDepartmentsUseCaseImpl implements CompareDepartmentsUseCase {
    private final DepartmentRepository departmentRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Department getByUuid(UUID uuid) {
        return departmentRepository.findByUuid(uuid)
            .orElseThrow(() -> new DepartmentNotFoundException(uuid));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Department getByCode(String code) {
        return departmentRepository.findByCode(code)
            .orElseThrow(() -> new DepartmentNotFoundException(code));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllActive() {
        return departmentRepository.findAllEnabled();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Department> findAll(Map<String, Object> filters, Pageable pageable) {
        // Apply dynamic filters based on filter map
        Boolean enabled = (Boolean) filters.get("enabled");
        String search = (String) filters.get("search");
        
        if (search != null && !search.isBlank()) {
            return departmentRepository.findByNameContaining(search, pageable);
        } else if (enabled != null) {
            return departmentRepository.findByEnabled(enabled, pageable);
        } else {
            return departmentRepository.findAll(pageable);
        }
    }
}

// ManageDepartmentUseCaseImpl.java
@Service
@RequiredArgsConstructor
public class ManageDepartmentUseCaseImpl implements ManageDepartmentUseCase {
    private final DepartmentRepository departmentRepository;
    private final GeographyDomainService domainService;
    private final GeographyValidationService validationService;
    
    @Override
    @Transactional
    public Department create(Department department) {
        // Validate domain rules
        domainService.validateDepartmentCode(department.getCode());
        validationService.ensureDepartmentCodeUnique(department.getCode(), null);
        
        // Set UUID and timestamps
        department.setUuid(UUID.randomUUID());
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        
        return departmentRepository.save(department);
    }
    
    @Override
    @Transactional
    public Department update(UUID uuid, Department updates) {
        Department existing = departmentRepository.findByUuid(uuid)
            .orElseThrow(() -> new DepartmentNotFoundException(uuid));
        
        // Validate code uniqueness if changed
        if (!existing.getCode().equals(updates.getCode())) {
            validationService.ensureDepartmentCodeUnique(updates.getCode(), uuid);
        }
        
        // Update fields
        existing.setName(updates.getName());
        existing.setEnabled(updates.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return departmentRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void delete(UUID uuid) {
        Department department = departmentRepository.findByUuid(uuid)
            .orElseThrow(() -> new DepartmentNotFoundException(uuid));
        
        // Check business rules
        if (!domainService.canDeleteDepartment(department)) {
            throw new GeographyConstraintException(
                "Cannot delete department with associated municipalities");
        }
        
        departmentRepository.delete(department);
    }
    
    @Override
    @Transactional
    public void activate(UUID uuid) {
        Department department = departmentRepository.findByUuid(uuid)
            .orElseThrow(() -> new DepartmentNotFoundException(uuid));
        department.activate();
        departmentRepository.save(department);
    }
    
    @Override
    @Transactional
    public void deactivate(UUID uuid) {
        Department department = departmentRepository.findByUuid(uuid)
            .orElseThrow(() -> new DepartmentNotFoundException(uuid));
        department.deactivate();
        departmentRepository.save(department);
    }
}

// CompareMunicipalitiesUseCaseImpl.java
@Service
@RequiredArgsConstructor
public class CompareMunicipalitiesUseCaseImpl implements CompareMunicipalitiesUseCase {
    private final MunicipalityRepository municipalityRepository;
    private final DepartmentRepository departmentRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Municipality getByUuid(UUID uuid) {
        return municipalityRepository.findByUuid(uuid)
            .orElseThrow(() -> new MunicipalityNotFoundException(uuid));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Municipality getByCodeAndDepartment(String code, Long departmentId) {
        return municipalityRepository.findByCodeAndDepartmentId(code, departmentId)
            .orElseThrow(() -> new MunicipalityNotFoundException(code));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Municipality> getAllActive() {
        return municipalityRepository.findAllEnabled();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Municipality> findByDepartment(UUID departmentUuid, Pageable pageable) {
        Department department = departmentRepository.findByUuid(departmentUuid)
            .orElseThrow(() -> new DepartmentNotFoundException(departmentUuid));
        return municipalityRepository.findByDepartmentId(department.getId(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Municipality> findAll(Map<String, Object> filters, Pageable pageable) {
        // Apply dynamic filters
        Long departmentId = (Long) filters.get("departmentId");
        Boolean enabled = (Boolean) filters.get("enabled");
        String search = (String) filters.get("search");
        
        if (search != null && !search.isBlank()) {
            return municipalityRepository.findByNameContaining(search, pageable);
        } else if (departmentId != null && enabled != null) {
            return municipalityRepository.findByDepartmentIdAndEnabled(departmentId, enabled, pageable);
        } else if (departmentId != null) {
            return municipalityRepository.findByDepartmentId(departmentId, pageable);
        } else {
            return municipalityRepository.findAll(pageable);
        }
    }
}

// ManageMunicipalityUseCaseImpl.java
@Service
@RequiredArgsConstructor
public class ManageMunicipalityUseCaseImpl implements ManageMunicipalityUseCase {
    private final MunicipalityRepository municipalityRepository;
    private final DepartmentRepository departmentRepository;
    private final GeographyDomainService domainService;
    private final GeographyValidationService validationService;
    
    @Override
    @Transactional
    public Municipality create(Municipality municipality) {
        // Validate domain rules
        domainService.validateMunicipalityCode(municipality.getCode());
        
        // Verify department exists
        Department department = departmentRepository.findById(municipality.getDepartment().getId())
            .orElseThrow(() -> new DepartmentNotFoundException(municipality.getDepartment().getId()));
        
        // Validate code uniqueness within department
        validationService.ensureMunicipalityCodeUniqueInDepartment(
            department.getUuid(), municipality.getCode(), null);
        
        // Set UUID and timestamps
        municipality.setUuid(UUID.randomUUID());
        municipality.setCreatedAt(LocalDateTime.now());
        municipality.setUpdatedAt(LocalDateTime.now());
        
        return municipalityRepository.save(municipality);
    }
    
    @Override
    @Transactional
    public Municipality update(UUID uuid, Municipality updates) {
        Municipality existing = municipalityRepository.findByUuid(uuid)
            .orElseThrow(() -> new MunicipalityNotFoundException(uuid));
        
        // Validate code uniqueness if changed
        if (!existing.getCode().equals(updates.getCode())) {
            validationService.ensureMunicipalityCodeUniqueInDepartment(
                existing.getDepartment().getUuid(), updates.getCode(), uuid);
        }
        
        // Update fields
        existing.setName(updates.getName());
        existing.setEnabled(updates.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return municipalityRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void delete(UUID uuid) {
        Municipality municipality = municipalityRepository.findByUuid(uuid)
            .orElseThrow(() -> new MunicipalityNotFoundException(uuid));
        municipalityRepository.delete(municipality);
    }
    
    @Override
    @Transactional
    public void activate(UUID uuid) {
        Municipality municipality = municipalityRepository.findByUuid(uuid)
            .orElseThrow(() -> new MunicipalityNotFoundException(uuid));
        municipality.activate();
        municipalityRepository.save(municipality);
    }
    
    @Override
    @Transactional
    public void deactivate(UUID uuid) {
        Municipality municipality = municipalityRepository.findByUuid(uuid)
            .orElseThrow(() -> new MunicipalityNotFoundException(uuid));
        municipality.deactivate();
        municipalityRepository.save(municipality);
    }
}
```

---

### Infrastructure Layer (Adapters)

**Purpose**: Implementación de detalles técnicos (DB, API, etc.)

#### Output Adapters (Persistence)

**Entities** (`infrastructure/out/persistence/entity/geography/`)
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

**Entity Mappers** (`infrastructure/out/persistence/mapper/geography/`)
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

**Adapters** (`infrastructure/out/persistence/adapter/geography/`)
```java
@Component
@RequiredArgsConstructor
public class DepartmentRepositoryAdapter implements DepartmentRepository {
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
public class MunicipalityRepositoryAdapter implements MunicipalityRepository {
    private final MunicipalityJpaRepository jpaRepository;
    private final MunicipalityEntityMapper mapper;
    
    // Similar implementation...
}
```

#### Input Adapters (REST API)

**Controllers** (`infrastructure/in/web/controller/geography/`)
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

**Municipality DTOs** (`infrastructure/in/web/dto/geography/`)
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

**DTO Mappers** (`infrastructure/in/web/mapper/geography/`)
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
- **Coverage**: > 90%
- **Tools**: JUnit 5, Mockito
- **Focus**: Business logic, validations

### Controller Tests
- **Target**: Controllers
- **Tools**: JUnit 5, Mockito
- **Focus**: Status codes y body (sin MockMvc)

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
