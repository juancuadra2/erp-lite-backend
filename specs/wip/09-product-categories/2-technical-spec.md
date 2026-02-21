# Especificación Técnica: Módulo de Categorías de Productos

**Creado:** 20 de febrero de 2026
**Número de Funcionalidad:** 09
**Arquitectura:** Hexagonal (Puertos y Adaptadores)

---

## Modelo de Dominio

```java
// domain/model/productcategory/ProductCategory.java
// Lombok: @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductCategory {
    private Long id;
    private UUID uuid;
    private String code;          // Siempre mayúsculas. Inmutable tras creación.
    private String name;
    private String description;   // Nullable.
    private Boolean active;
    private Long createdBy;       // ID del usuario del JWT en el momento de la operación.
    private Long updatedBy;       // ID del usuario del JWT en el momento de la operación.
    private Long deletedBy;       // ID del usuario del JWT en el momento de la operación.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public boolean isDeleted()             { return deletedAt != null; }
    public void activate()                 { this.active = true; }
    public void deactivate()               { this.active = false; }
    public void markAsDeleted(Long userId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }
}
```

### Excepciones de dominio

```
domain/exception/productcategory/
  ProductCategoryDomainException.java     ← base (extends RuntimeException)
  ProductCategoryNotFoundException.java   ← lanzada cuando uuid no existe o está soft-deleted
  DuplicateCategoryCodeException.java     ← lanzada cuando code (normalizado) ya existe
  CategoryInUseException.java             ← stub; no se lanza en esta fase
```

---

## Migración de Base de Datos

### V16 — `create_product_categories_table`

```sql
CREATE TABLE product_categories (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid        CHAR(36)        NOT NULL,
    code        VARCHAR(20)     NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(500)    NULL,
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by  BIGINT          NULL,
    updated_by  BIGINT          NULL,
    deleted_by  BIGINT          NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP       NULL,

    CONSTRAINT uk_product_categories_uuid UNIQUE (uuid),
    CONSTRAINT uk_product_categories_code UNIQUE (code),
    INDEX idx_product_categories_active     (active),
    INDEX idx_product_categories_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Estructura de Paquetes

```
domain/model/productcategory/
  ProductCategory.java
domain/exception/productcategory/
  ProductCategoryDomainException.java
  ProductCategoryNotFoundException.java
  DuplicateCategoryCodeException.java
  CategoryInUseException.java
domain/service/productcategory/
  ProductCategoryValidator.java
  ProductCategoryDomainService.java
domain/port/productcategory/
  ProductCategoryRepository.java

application/port/productcategory/
  ManageProductCategoryUseCase.java
  CompareProductCategoryUseCase.java
application/usecase/productcategory/
  ManageProductCategoryUseCaseImpl.java
  CompareProductCategoryUseCaseImpl.java

infrastructure/in/web/
  controller/productcategory/
    ProductCategoryController.java
  dto/productcategory/
    CreateProductCategoryRequestDto.java
    UpdateProductCategoryRequestDto.java
    ProductCategoryResponseDto.java
    ProductCategoryFilterDto.java
  mapper/productcategory/
    ProductCategoryDtoMapper.java

infrastructure/out/persistence/
  entity/productcategory/
    ProductCategoryEntity.java
  mapper/productcategory/
    ProductCategoryEntityMapper.java
  adapter/productcategory/
    ProductCategoryJpaRepository.java
    ProductCategoryRepositoryAdapter.java
  util/productcategory/
    ProductCategorySpecificationUtil.java
```

---

## Contratos de DTOs

### `CreateProductCategoryRequestDto`

| Campo | Tipo | Validación Jakarta |
|-------|------|--------------------|
| `code` | `String` | `@NotBlank` `@Pattern(regexp = "^[A-Za-z0-9-]{2,20}$")` |
| `name` | `String` | `@NotBlank` `@Size(max = 100)` |
| `description` | `String` | `@Size(max = 500)` (nullable) |

### `UpdateProductCategoryRequestDto`

| Campo | Tipo | Validación Jakarta |
|-------|------|--------------------|
| `name` | `String` | `@NotBlank` `@Size(max = 100)` |
| `description` | `String` | `@Size(max = 500)` (nullable) |

> `code` **no existe** en este DTO.
> Si `description` llega como `null` o ausente del JSON, se almacena `null` (borra el valor previo).

### `ProductCategoryResponseDto` (record)

| Campo | Tipo | Notas |
|-------|------|-------|
| `uuid` | `UUID` | — |
| `code` | `String` | Siempre mayúsculas. |
| `name` | `String` | — |
| `description` | `String` | Nullable. |
| `active` | `Boolean` | — |
| `createdAt` | `LocalDateTime` | — |
| `updatedAt` | `LocalDateTime` | — |
| `deletedAt` | `LocalDateTime` | Nullable. |
| `createdBy` | `Long` | Nullable. |
| `updatedBy` | `Long` | Nullable. |
| `deletedBy` | `Long` | Nullable. |

### `ProductCategoryFilterDto`

| Parámetro | Tipo | Default | Restricción | Comportamiento |
|-----------|------|---------|-------------|----------------|
| `search` | `String` | `null` | — | `null` = sin filtro. Si presente: OR entre `code LIKE %v%`, `name LIKE %v%`, `description LIKE %v%`. Insensible a mayúsculas. |
| `active` | `Boolean` | `null` | — | `null` = devuelve activos e inactivos. `true`/`false` filtra por ese valor. |
| `page` | `int` | `0` | `@Min(0)` → 400 si negativo. | Basado en 0. |
| `limit` | `int` | `10` | `@Min(1) @Max(100)` → 400 si fuera de rango. | Registros por página. |
| `sort.field` | `String` | `"name"` | Valores permitidos: `code`, `name`, `createdAt`, `updatedAt`. Cualquier otro → 400. | Campo de ordenamiento. |
| `sort.order` | `String` | `"ASC"` | `ASC` o `DESC`. Cualquier otro → 400. | Dirección de orden. |

---

## Lógica de Dominio

### `ProductCategoryValidator`

| Método | Regla | Excepción |
|--------|-------|-----------|
| `validateCode(String)` | Patrón `^[A-Za-z0-9-]{2,20}$` sobre el input original (antes de normalizar). | `ProductCategoryDomainException` |
| `validateName(String)` | No blank, max 100 chars. | `ProductCategoryDomainException` |
| `validateDescription(String)` | Si no null: max 500 chars. | `ProductCategoryDomainException` |

> La validación Jakarta en el DTO es la primera barrera. El Validator de dominio es la segunda barrera para llamadas internas.

### `ProductCategoryDomainService`

| Método | Comportamiento |
|--------|----------------|
| `normalizeCode(String)` | Retorna `input.trim().toUpperCase()`. |
| `validateUniqueCode(String normalizedCode, ProductCategoryRepository repo)` | Llama `repo.existsByCode(normalizedCode)`. Si true → lanza `DuplicateCategoryCodeException`. |
| `canDelete(UUID uuid)` | **Stub.** Retorna `true` siempre. No lanza excepción. |

### `ManageProductCategoryUseCaseImpl` — flujos

**create:**
1. Normalizar `code`.
2. Validar patrón con `validator.validateCode()`.
3. Validar unicidad con `domainService.validateUniqueCode()`.
4. Construir `ProductCategory` con UUID aleatorio, `active=true`, `createdBy=userId`.
5. `repo.save()`.
6. Retornar entidad guardada.

**update:**
1. `repo.findByUuid()` → 404 si no existe.
2. Actualizar `name` y `description` (null en description = borrar).
3. Setear `updatedBy=userId`, `updatedAt=now()`.
4. `repo.save()`.

**activate / deactivate:**
1. `repo.findByUuid()` → 404 si no existe.
2. Llamar `entity.activate()` o `entity.deactivate()`.
3. Setear `updatedBy=userId`.
4. `repo.save()`.
5. Si ya estaba en el estado solicitado: ejecutar igual (no-op idempotente).

**delete:**
1. `repo.findByUuid()` → 404 si no existe (o está soft-deleted).
2. `domainService.canDelete()` → siempre true en esta fase.
3. `entity.markAsDeleted(userId)`.
4. `repo.save()`.

---

## Puerto de Salida: `ProductCategoryRepository`

```java
public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory category);
    Optional<ProductCategory> findByUuid(UUID uuid);
    Optional<ProductCategory> findByCode(String normalizedCode);
    boolean existsByCode(String normalizedCode);
    Page<ProductCategory> findAll(Specification<ProductCategoryEntity> spec, Pageable pageable);
}
```

> `findByUuid` y `findByCode` NO retornan registros con `deleted_at IS NOT NULL`.

---

## Mapeo de Excepciones → HTTP

| Excepción | HTTP | `error` en body |
|-----------|------|-----------------|
| `ProductCategoryNotFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| `DuplicateCategoryCodeException` | 409 | `DUPLICATE_CODE` |
| `CategoryInUseException` | 422 | `BUSINESS_RULE_VIOLATION` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |
| `ConstraintViolationException` | 400 | `VALIDATION_ERROR` |

---

## Wiring en `BeanConfiguration`

```java
@Bean
public ProductCategoryValidator productCategoryValidator() {
    return new ProductCategoryValidator();
}

@Bean
public ProductCategoryDomainService productCategoryDomainService(
        ProductCategoryRepository productCategoryRepository,
        ProductCategoryValidator productCategoryValidator) {
    return new ProductCategoryDomainService(productCategoryRepository, productCategoryValidator);
}

@Bean
public ManageProductCategoryUseCase manageProductCategoryUseCase(
        ProductCategoryRepository productCategoryRepository,
        ProductCategoryDomainService productCategoryDomainService) {
    return new ManageProductCategoryUseCaseImpl(productCategoryRepository, productCategoryDomainService);
}

@Bean
public CompareProductCategoryUseCase compareProductCategoryUseCase(
        ProductCategoryRepository productCategoryRepository) {
    return new CompareProductCategoryUseCaseImpl(productCategoryRepository);
}
```

---

## Seguridad

### Contexto global

`SecurityConfig` aplica `anyRequest().authenticated()`: cualquier request sin JWT válido retorna **401** antes de llegar al controller.

### Autorización por endpoint

| Endpoint | `@PreAuthorize` | Sin JWT | Sin permiso | Con permiso | JWT ADMIN |
|----------|-----------------|---------|-------------|-------------|-----------|
| `GET /api/product-categories` | — | 401 | 200 | 200 | 200 |
| `GET /api/product-categories/{uuid}` | — | 401 | 200 | 200 | 200 |
| `POST /api/product-categories` | `hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:CREATE')` | 401 | 403 | 201 | 201 |
| `PUT /api/product-categories/{uuid}` | `hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:UPDATE')` | 401 | 403 | 200 | 200 |
| `PATCH /api/product-categories/{uuid}/activate` | `hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:UPDATE')` | 401 | 403 | 200 | 200 |
| `PATCH /api/product-categories/{uuid}/deactivate` | `hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:UPDATE')` | 401 | 403 | 200 | 200 |
| `DELETE /api/product-categories/{uuid}` | `hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:DELETE')` | 401 | 403 | 204 | 204 |

- **Sin JWT**: 401 antes de llegar al controller (`anyRequest().authenticated()` en `SecurityConfig`).
- **Sin permiso**: JWT válido pero el rol del usuario no tiene el permiso granular asignado → 403.
- **Con permiso**: JWT válido y el permiso `PRODUCT_CATEGORY:ACTION` está en `role_permissions` del rol → éxito.
- **JWT ADMIN**: `hasRole('ADMIN')` siempre satisface la condición → éxito.
- Los endpoints `GET` no llevan `@PreAuthorize`; todo usuario autenticado puede leer.

### Patrón de implementación en el controller

```java
@PostMapping
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:CREATE')")
public ResponseEntity<ProductCategoryResponseDto> create(...) { ... }

@PutMapping("/{uuid}")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:UPDATE')")
public ResponseEntity<ProductCategoryResponseDto> update(...) { ... }

@PatchMapping("/{uuid}/activate")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:UPDATE')")
public ResponseEntity<ProductCategoryResponseDto> activate(...) { ... }

@PatchMapping("/{uuid}/deactivate")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:UPDATE')")
public ResponseEntity<ProductCategoryResponseDto> deactivate(...) { ... }

@DeleteMapping("/{uuid}")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PRODUCT_CATEGORY:DELETE')")
public ResponseEntity<Void> delete(...) { ... }

@GetMapping("/{uuid}")
// sin @PreAuthorize — anyRequest().authenticated() cubre la autenticación
public ResponseEntity<ProductCategoryResponseDto> findByUuid(...) { ... }
```

Ver `specs/PERMISSION-MODEL.md` para la política completa de autorización (Zona C — patrón granular).

### Origen del `userId`

Mismo mecanismo que `ManageWarehouseUseCaseImpl`. No se pasa como parámetro HTTP. El implementador sigue el patrón ya establecido en el proyecto para poblar `createdBy`, `updatedBy`, `deletedBy`.

### Permisos en base de datos

#### Migración V17 — `insert_product_category_permissions`

```sql
INSERT IGNORE INTO permissions (id, entity, action, condition_expr, description)
VALUES
    (UNHEX(REPLACE('11000000-0000-0000-0000-000000000001', '-', '')), 'PRODUCT_CATEGORY', 'CREATE', NULL, 'Crear categorías de productos'),
    (UNHEX(REPLACE('11000000-0000-0000-0000-000000000002', '-', '')), 'PRODUCT_CATEGORY', 'READ',   NULL, 'Consultar categorías de productos'),
    (UNHEX(REPLACE('11000000-0000-0000-0000-000000000003', '-', '')), 'PRODUCT_CATEGORY', 'UPDATE', NULL, 'Actualizar categorías de productos'),
    (UNHEX(REPLACE('11000000-0000-0000-0000-000000000004', '-', '')), 'PRODUCT_CATEGORY', 'DELETE', NULL, 'Eliminar categorías de productos');

-- Rol ADMIN: todos los permisos
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')), id
FROM permissions WHERE entity = 'PRODUCT_CATEGORY';

-- Rol USER: solo READ
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', '')), id
FROM permissions WHERE entity = 'PRODUCT_CATEGORY' AND action = 'READ';
```

---

## Requisitos del Pipeline (GitHub Actions)

### Push a cualquier rama (no-main)
Ejecuta `mvn clean compile`. **Condición de paso**: compilación sin errores.

### PR a `main`
Ejecuta `mvn clean test jacoco:report`. **Condiciones de paso**:
1. Todos los tests pasan.
2. Cobertura global de instrucciones ≥ **90%**.
3. Cada archivo `.java` nuevo en `src/main/java/` debe tener **100% de cobertura de instrucciones**.

**Implicaciones para esta implementación:**
- Todo archivo de producción (modelo, excepciones, validator, service, port, use cases, entity, mapper, adapter, util, controller, DTOs) debe tener test que lo cubra al 100%.
- Las interfaces y `record`s son inspeccionados por JaCoCo; si tienen bytecode (default methods, constructores de record) deben estar cubiertos.
- Las excepciones que extienden `RuntimeException` solo con constructor deben ser instanciadas al menos una vez en los tests.
- `BeanConfiguration` es inspeccionado: los nuevos `@Bean` deben estar cubiertos por el contexto de test si el archivo se modifica.

---

## Restricciones Fijas

- `ProductCategoryValidator`, `ProductCategoryDomainService`: sin `@Component`. Declarados en `BeanConfiguration`.
- `ProductCategoryRepositoryAdapter`: con `@Component` y `@RequiredArgsConstructor`.
- `ProductCategoryDtoMapper`, `ProductCategoryEntityMapper`: `@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)`.
- `ProductCategoryEntity`: `@SQLRestriction("deleted_at IS NULL")`. No `@Data`.
- Tests: `@ExtendWith(MockitoExtension.class)` sin contexto Spring. H2 para adapter tests.
- Migraciones: V16 (tabla), V17 (permisos). Ambas deben sincronizarse en `docker/mysql-init/`.
