# Technical Specification: Módulo de Gestión de Bodegas

**Feature**: 08-warehouses
**Version**: 1.2
**Created**: 2026-02-19
**Last Updated**: 2026-02-20
**Status**: ✅ Approved — Ready for Implementation

---

## 🎯 Architecture Overview

Módulo de catálogo estándar que implementa **arquitectura hexagonal** siguiendo el patrón establecido en el proyecto (idéntico a `05-units-of-measure` y `03-payment-methods`).

### Tech Stack

- **Backend**: Java 17+, Spring Boot 3.x
- **Persistence**: MySQL 8.0+ con Flyway, Spring Data JPA
- **Mapping**: MapStruct 1.5+
- **Testing**: JUnit 5, Mockito, H2 en memoria

---

## 🏗️ Package Structure

```
domain/
  model/warehouse/
    Warehouse.java
    WarehouseType.java          ← enum
  service/warehouse/
    WarehouseDomainService.java
    WarehouseValidator.java
    WarehouseValidationService.java
  port/warehouse/
    WarehouseRepository.java
  exception/warehouse/
    WarehouseNotFoundException.java
    DuplicateWarehouseCodeException.java
    DuplicateWarehouseNameException.java
    WarehouseInUseException.java
    InvalidWarehouseDataException.java
    SinglePrincipalWarehouseException.java

application/
  command/warehouse/
    CreateWarehouseCommand.java
    UpdateWarehouseCommand.java
  port/warehouse/
    ManageWarehouseUseCase.java
    CompareWarehouseUseCase.java
  usecase/warehouse/
    ManageWarehouseUseCaseImpl.java
    CompareWarehouseUseCaseImpl.java

infrastructure/
  config/
    BeanConfiguration.java         ← actualizar
  in/web/
    controller/warehouse/
      WarehouseController.java
    dto/warehouse/
      CreateWarehouseRequestDto.java
      UpdateWarehouseRequestDto.java
      WarehouseResponseDto.java
    mapper/warehouse/
      WarehouseDtoMapper.java
    advice/
      GlobalExceptionHandler.java  ← actualizar
  out/persistence/
    adapter/warehouse/
      WarehouseRepositoryAdapter.java
    entity/warehouse/
      WarehouseEntity.java
    jpa/warehouse/
      WarehouseJpaRepository.java
    mapper/warehouse/
      WarehouseEntityMapper.java
    util/warehouse/
      WarehouseSpecificationUtil.java
```

> **Nota `WarehouseJpaRepository`**: Los métodos `existsByCodeIgnoreCaseAndUuidNot` / `existsByNameIgnoreCaseAndUuidNot` en la JPA usan `String uuid` (el campo `uuid` se almacena como `String` en la entidad). El adapter convierte `UUID → String` antes de invocarlos. Los métodos `existsActivePrincipal*` se implementan con derivación JPA: `existsByTypeAndActiveTrueAndDeletedAtIsNull(WarehouseType type)` y `existsByTypeAndActiveTrueAndDeletedAtIsNullAndUuidNot(WarehouseType type, String uuid)`.

> **Nota `WarehouseSpecificationUtil`**: Siempre añade `deletedAt IS NULL` como predicado base (soft-delete). El filtro `municipalityId` (UUID del controller) se convierte a `String` antes de buscar por `municipalityUuid`.

---

## 🏛️ Domain Layer

### `WarehouseType.java` (enum)

```java
public enum WarehouseType {
    PRINCIPAL,
    SUCURSAL,
    CONSIGNACION,
    TEMPORAL
}
```

### `Warehouse.java` (Domain Model)

```java
@Getter
@Builder
@AllArgsConstructor
public class Warehouse {
    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private String description;
    private WarehouseType type;
    private String address;
    private UUID municipalityId;
    private String responsible;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

### `WarehouseValidator.java`

Validaciones estructurales del modelo (no depende de repositorios):
- Código: requerido, 3-20 chars, formato `[A-Z0-9\-]+`
- Nombre: requerido, 3-100 chars
- Tipo: requerido, valor válido del enum
- Email: formato válido si se provee
- Teléfono: formato válido si se provee

### `WarehouseDomainService.java`

Reglas de negocio que requieren consultas al repositorio. Constructor: `(WarehouseValidator, WarehouseRepository)`.

**Firmas de métodos:**

```java
// Crea, valida y construye la entidad de dominio
public Warehouse prepareForCreate(CreateWarehouseCommand command);

// Valida el comando y aplica los cambios sobre la entidad existente
public void applyUpdate(Warehouse existing, UpdateWarehouseCommand command);

// Verifica la regla BR-02.2 antes de activar
public void validateForActivation(Warehouse warehouse);
```

Responsabilidades de **`prepareForCreate`**:
- Delega validación estructural al `WarehouseValidator`
- Normaliza código: `toUpperCase().trim()`
- Verifica unicidad de código (BR-01.1): `existsByCodeIgnoreCase` → `DuplicateWarehouseCodeException`
- Verifica unicidad de nombre (BR-01.2): `existsByNameIgnoreCase` → `DuplicateWarehouseNameException`
- Verifica unicidad de PRINCIPAL activa (BR-02.2) si `type == PRINCIPAL`: `existsActivePrincipalWarehouse` → `SinglePrincipalWarehouseException`
- Construye y retorna `Warehouse` con `uuid=UUID.randomUUID()`, `active=true`, `createdAt=LocalDateTime.now()`

Responsabilidades de **`applyUpdate`** (el código no es modificable):
- Delega validación estructural al `WarehouseValidator`
- Verifica unicidad de nombre excluyendo la bodega actual (BR-01.2): `existsByNameIgnoreCaseAndUuidNot` → `DuplicateWarehouseNameException`
- Verifica unicidad de PRINCIPAL activa excluyendo la bodega actual (BR-02.2) si `type == PRINCIPAL`: `existsActivePrincipalWarehouseAndUuidNot` → `SinglePrincipalWarehouseException`
- Aplica los cambios al objeto `Warehouse` existente y fija `updatedAt=LocalDateTime.now()`

Responsabilidades de **`validateForActivation`**:
- Verifica unicidad de PRINCIPAL activa excluyendo la bodega actual (BR-02.2) si `warehouse.getType() == PRINCIPAL` → `SinglePrincipalWarehouseException`

### `WarehouseValidationService.java`

Validaciones de integridad referencial. Constructor: **sin argumentos** (la implementación actual no requiere repositorios; las verificaciones cross-module se añadirán cuando se implemente 07-inventory mediante puertos adicionales).

```java
public class WarehouseValidationService {

    public void validateDeletable(Warehouse warehouse);

    public void validateDeactivatable(UUID uuid);
}
```

- `validateDeletable(Warehouse warehouse)`: recibe el objeto dominio ya cargado y verifica:
  1. (**BR-03.3**) Si `warehouse.getType() == PRINCIPAL && warehouse.isActive()` → lanza `WarehouseInUseException("No se puede eliminar la bodega PRINCIPAL activa. Es el punto de venta activo del sistema")`
  2. (BR-03.1) Stub: no hace nada. Se completará en 07-inventory.
- `validateDeactivatable(UUID uuid)`: Stub: no hace nada. Se completará en 07-inventory.

> **Nota**: La verificación BR-03.3 es funcional desde el inicio. Las verificaciones cross-module (stock, transferencias pendientes) son stubs y no requieren repositorios en esta versión.

### `WarehouseRepository.java` (Port)

```java
public interface WarehouseRepository {
    Warehouse save(Warehouse warehouse);
    Optional<Warehouse> findByUuid(UUID uuid);                          // excluye soft-deleted
    Page<Warehouse> findAll(Map<String, Object> filters, Pageable pageable); // excluye soft-deleted vía spec
    List<Warehouse> findAllActive();                                    // active=true AND deletedAt IS NULL; para uso interno / cross-module
    boolean existsByCodeIgnoreCase(String code);                        // incluye soft-deleted (BR-04.3: código bloqueado)
    boolean existsByCodeIgnoreCaseAndUuidNot(String code, UUID excludeUuid);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndUuidNot(String name, UUID excludeUuid);
    boolean existsActivePrincipalWarehouse();                           // active=true AND type=PRINCIPAL AND deletedAt IS NULL
    boolean existsActivePrincipalWarehouseAndUuidNot(UUID excludeUuid); // igual pero excluyendo la bodega actual
}
```

> **Nota sobre `findAllActive()`**: No se expone como endpoint REST. Es un método de solo lectura para uso interno futuro (e.g., selector de bodegas en el módulo 07-inventory).

> **Nota sobre `existsByCode*`**: Incluyen bodegas eliminadas intencionalmente para cumplir BR-04.3 (código bloqueado permanentemente).

---

## 📦 Application Layer

### `CreateWarehouseCommand.java` (record)

```java
public record CreateWarehouseCommand(
    String code,
    String name,
    String description,
    WarehouseType type,
    String address,
    UUID municipalityId,
    String responsible,
    String email,
    String phone
) {}
```

### `UpdateWarehouseCommand.java` (record)

```java
public record UpdateWarehouseCommand(
    String name,
    String description,
    WarehouseType type,
    String address,
    UUID municipalityId,
    String responsible,
    String email,
    String phone
) {}
```

### `ManageWarehouseUseCase.java`

```java
public interface ManageWarehouseUseCase {
    Warehouse create(CreateWarehouseCommand command);
    Warehouse update(UUID uuid, UpdateWarehouseCommand command);
    void delete(UUID uuid);
    Warehouse activate(UUID uuid);
    Warehouse deactivate(UUID uuid);
}
```

### `CompareWarehouseUseCase.java`

```java
public interface CompareWarehouseUseCase {
    Warehouse findByUuid(UUID uuid);
    Page<Warehouse> findAll(Map<String, Object> filters, Pageable pageable);
    List<Warehouse> findAllActive();
}
```

---

## 🌐 Infrastructure Layer

### DTOs

**`CreateWarehouseRequestDto.java`** (record con Jakarta Validation):
```java
public record CreateWarehouseRequestDto(
    @NotBlank @Size(min=3, max=20) @Pattern(regexp="[A-Z0-9\\-]+") String code,
    @NotBlank @Size(min=3, max=100) String name,
    @Size(max=255) String description,
    @NotNull WarehouseType type,
    @Size(max=255) String address,
    UUID municipalityId,
    @Size(max=100) String responsible,
    @Email @Size(max=100) String email,
    @Pattern(regexp="[0-9\\+\\-\\s]{7,20}") String phone
) {}
```

**`UpdateWarehouseRequestDto.java`** (record con Jakarta Validation):
```java
public record UpdateWarehouseRequestDto(
    @NotBlank @Size(min=3, max=100) String name,
    @NotNull WarehouseType type,
    @Size(max=255) String description,
    @Size(max=255) String address,
    UUID municipalityId,
    @Size(max=100) String responsible,
    @Email @Size(max=100) String email,
    @Pattern(regexp="[0-9\\+\\-\\s]{7,20}") String phone
) {}
```
`code` no forma parte del update (no modificable). Campos opcionales enviados como `null` limpian el valor existente (semántica PUT).

**`WarehouseResponseDto.java`** (record):
```java
public record WarehouseResponseDto(
    UUID uuid,
    String code,
    String name,
    String description,
    WarehouseType type,
    String address,
    UUID municipalityId,
    String responsible,
    String email,
    String phone,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

### `WarehouseController.java`

Sigue el mismo patrón que `UnitOfMeasureController`: parámetros de filtro explícitos, `Pageable` construido manualmente, respuesta de lista envuelta en `PagedResponseDto<T>`. La seguridad para los GET está cubierta por `anyRequest().authenticated()` en `SecurityConfig`; no se añade `@PreAuthorize` en métodos de lectura.

```java
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final ManageWarehouseUseCase manageUseCase;
    private final CompareWarehouseUseCase compareUseCase;
    private final WarehouseDtoMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> create(@Valid @RequestBody CreateWarehouseRequestDto request) { ... }

    @GetMapping("/{uuid}")
    public ResponseEntity<WarehouseResponseDto> findByUuid(@PathVariable UUID uuid) { ... }

    @GetMapping
    public ResponseEntity<PagedResponseDto<WarehouseResponseDto>> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) UUID municipalityId,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String code,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(defaultValue = "name") String sort,
        @RequestParam(defaultValue = "asc") String direction) { ... }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> update(
        @PathVariable UUID uuid,
        @Valid @RequestBody UpdateWarehouseRequestDto request) { ... }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) { ... }

    @PatchMapping("/{uuid}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> activate(@PathVariable UUID uuid) { ... }

    @PatchMapping("/{uuid}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> deactivate(@PathVariable UUID uuid) { ... }
}
```

### `WarehouseEntity.java` (JPA)

```java
@Entity
@Table(name = "warehouses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WarehouseType type;

    @Column(length = 255)
    private String address;

    @Column(name = "municipality_uuid", length = 36)
    private String municipalityUuid;

    @Column(length = 100)
    private String responsible;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

---

## 🗄️ Database Schema

### Flyway: `V13__create_warehouses_table.sql`

```sql
CREATE TABLE warehouses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid            VARCHAR(36)  NOT NULL UNIQUE,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    description       VARCHAR(255),
    type              VARCHAR(30)  NOT NULL,
    address           VARCHAR(255),
    municipality_uuid VARCHAR(36),
    responsible       VARCHAR(100),
    email             VARCHAR(100),
    phone             VARCHAR(20),
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        DATETIME     NOT NULL,
    updated_at        DATETIME,
    deleted_at        DATETIME,
    INDEX idx_warehouse_uuid         (uuid),
    INDEX idx_warehouse_code         (code),
    INDEX idx_warehouse_type         (type),
    INDEX idx_warehouse_active       (active),
    INDEX idx_warehouse_municipality (municipality_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Flyway: `V14__insert_warehouses_seed_data.sql`

```sql
INSERT IGNORE INTO warehouses (uuid, code, name, description, type, active, created_at)
VALUES
    (UUID(), 'BOD-001', 'Bodega Principal', 'Bodega central de operaciones', 'PRINCIPAL', TRUE, NOW()),
    (UUID(), 'BOD-002', 'Sucursal Norte',   'Bodega de distribución norte',  'SUCURSAL',  TRUE, NOW());
```

---

## 🔧 BeanConfiguration — Beans a agregar

```java
@Bean
public WarehouseValidator warehouseValidator() {
    return new WarehouseValidator();
}

@Bean
public WarehouseValidationService warehouseValidationService() {
    return new WarehouseValidationService();
}

@Bean
public WarehouseDomainService warehouseDomainService(
        WarehouseValidator validator,
        WarehouseRepository repository) {
    return new WarehouseDomainService(validator, repository);
}

@Bean
public ManageWarehouseUseCase manageWarehouseUseCase(
        WarehouseRepository repository,
        WarehouseDomainService domainService,
        WarehouseValidationService validationService) {
    return new ManageWarehouseUseCaseImpl(repository, domainService, validationService);
}

@Bean
public CompareWarehouseUseCase compareWarehouseUseCase(WarehouseRepository repository) {
    return new CompareWarehouseUseCaseImpl(repository);
}
```

---

## 🔐 Security Integration

Los endpoints de escritura (POST, PUT, DELETE, PATCH) usan `@PreAuthorize("hasRole('ADMIN')")`.
Los endpoints de lectura (GET) no llevan anotación: `anyRequest().authenticated()` en `SecurityConfig` ya garantiza que solo usuarios autenticados puedan acceder.

Los permisos granulares (`WAREHOUSE:CREATE`, etc.) están **fuera del alcance** de este módulo. La autorización se gestiona exclusivamente por rol.

---

## 🧪 Testing Strategy

### Unit Tests (obligatorios, 100% cobertura en clases modificadas)

| Clase | Tests |
|-------|-------|
| `Warehouse.java` | activate, deactivate, softDelete, isDeleted |
| `WarehouseValidator.java` | validaciones estructurales por campo |
| `WarehouseDomainService.java` | unicidad código/nombre, bodega PRINCIPAL, normalización, prepareForCreate/Update, validateForActivation |
| `WarehouseValidationService.java` | validateDeletable (BR-03.3 funcional + BR-03.1 stub), validateDeactivatable stub |
| `ManageWarehouseUseCaseImpl.java` | create, update, delete (BR-03.3 + BR-03.1), activate (BR-02.2), deactivate |
| `CompareWarehouseUseCaseImpl.java` | findByUuid (found/not found), findAll con filtros, findAllActive |
| `WarehouseEntityMapper.java` | domain ↔ entity (incluyendo municipalityId UUID ↔ municipalityUuid String) |
| `WarehouseDtoMapper.java` | domain ↔ dto |
| `WarehouseRepositoryAdapter.java` | todos los métodos del port, incluyendo SpecificationUtil integration |
| `WarehouseSpecificationUtil.java` | filtros por active, type, municipalityUuid, name (like), code (like); exclusión de soft-deleted |
| `WarehouseController.java` | todos los endpoints: status HTTP, body, errores (400, 404, 409, 204) |

### H2 Test Profile
- `ddl-auto=create-drop`
- Flyway deshabilitado
- Schema compatible con MySQL MODE=MySQL

---

## 📊 Decisiones Técnicas

| ID | Decisión | Justificación |
|----|----------|---------------|
| DT-01 | UUID como identificador público de bodega | Consistente con todos los módulos del proyecto |
| DT-02 | `municipalityId` almacenado como `VARCHAR(36)` (UUID), sin FK de BD | Consistente con el patrón de identificadores públicos del proyecto. Sin FK de BD para mantener el módulo desacoplado de geography. |
| DT-03 | WarehouseValidationService sin verificación cross-módulo inicial | Inventario no está implementado; se añadirá el check de stock cuando exista el módulo 07-inventory |
| DT-04 | Seed data idempotente con `INSERT IGNORE` | Permite re-ejecutar migrations sin error |
| DT-05 | `WarehouseDomainService` recibe `(WarehouseValidator, WarehouseRepository)` | Necesita el repositorio para verificaciones de unicidad y BR-02.2; sin acoplar al `WarehouseValidationService` |
| DT-06 | `ManageWarehouseUseCaseImpl` recibe `(WarehouseRepository, WarehouseDomainService, WarehouseValidationService)` | Separa la lógica de negocio (DomainService) de la integridad referencial (ValidationService) |

---

**Status**: ✅ Approved v1.2 — Ready for Implementation
**Next Step**: Implementation
