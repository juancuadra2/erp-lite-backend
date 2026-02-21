# Modelo de Autorización y Permisos

**Creado:** 20 de febrero de 2026
**Estado:** Activo — fuente de verdad para todo lo relacionado con autorización de endpoints.

---

## 1. Propósito

Este documento define de forma exhaustiva y sin ambigüedad:

- Cómo funciona el mecanismo de autorización del sistema.
- Qué módulos son exclusivos de ADMIN y cuáles admiten permisos delegables.
- Qué permisos existen en la base de datos y cuáles no.
- El patrón exacto de `@PreAuthorize` que debe usarse en cada tipo de endpoint.
- Las reglas que impiden escalada de privilegios.

**Precedencia**: Este documento es complemento de `specs/RULES.md` (sección 22). En caso de conflicto, aplica `RULES.md`.

---

## 2. Cómo funciona el mecanismo de autorización

### Flujo de autorización en cada request

```
JWT válido → JwtAuthenticationFilter extrae:
  - roles[]       → authorities tipo "ROLE_ADMIN", "ROLE_USER"
  - permissions[] → authorities tipo "WAREHOUSE:CREATE", "BRAND:READ"
    (los permisos se leen frescos de BD en cada request)

→ SecurityContext contiene authorities (roles + permisos)

→ @PreAuthorize evalúa contra ese SecurityContext:
    hasRole('ADMIN')               → busca "ROLE_ADMIN" en authorities
    hasAuthority('ENTITY:ACTION')  → busca exactamente "ENTITY:ACTION"
```

### Dos roles base del sistema

| Rol | ID | Descripción | Permisos por defecto |
|-----|----|-------------|---------------------|
| `ADMIN` | `00000000-0000-0000-0000-000000000001` | Acceso total al sistema | Todos los permisos de todos los módulos |
| `USER` | `00000000-0000-0000-0000-000000000002` | Usuario estándar | Solo `READ` en módulos operacionales/catálogo |

El rol `ADMIN` siempre puede operar cualquier endpoint mediante `hasRole('ADMIN')`, sin necesidad de tener permisos granulares explícitamente asignados en `role_permissions`. Los permisos granulares son para **roles distintos de ADMIN**.

---

## 3. Clasificación de módulos: tres zonas

### ZONA A — Solo ADMIN, sin excepción

Los controllers de estos módulos usan **únicamente** `hasRole('ADMIN')`. No existe permiso granular delegable para ellos. No se insertan registros para ellos en la tabla `permissions` (excepto lectura, que sí puede consultarse — ver detalle abajo).

**Fundamento:** Cualquier operación de escritura en estos módulos permite escalar privilegios. Ejemplo: si un no-admin puede crear roles o asignar roles a usuarios, puede darse a sí mismo acceso total.

| Módulo | Endpoints siempre ADMIN | Endpoints abiertos a autenticados |
|--------|------------------------|----------------------------------|
| **Gestión de Roles** (`/api/v1/roles`) | POST, PUT, DELETE, POST `/{id}/permissions` | GET lista, GET `/{id}` |
| **Gestión de Permisos** (`/api/v1/permissions`) | POST | GET lista |
| **Gestión de Usuarios — crítico** | POST `/users` (crear), DELETE `/users/{id}`, POST `/users/{id}/roles` (asignar roles), PUT `/users/{id}/change-password` de otro usuario | — |

Para los `GET` de Zona A: no llevan `@PreAuthorize`. `anyRequest().authenticated()` cubre la autenticación. Un usuario autenticado puede consultar qué roles y permisos existen.

### ZONA B — Operaciones de usuario delegables

Operaciones sobre usuarios que no permiten escalar privilegios. El endpoint puede usarse por no-admins con el permiso específico.

| Endpoint | `@PreAuthorize` | Descripción |
|----------|-----------------|-------------|
| `PUT /api/v1/users/{id}` (datos básicos) | `hasRole('ADMIN') or hasAuthority('USER:UPDATE')` | Solo modifica nombre, email, documento. El handler rechaza cualquier intento de cambiar roles o contraseña. |
| `PUT /api/v1/users/{id}/unlock` | `hasRole('ADMIN') or hasAuthority('USER:UNLOCK')` | Desbloquea cuentas sin alterar roles ni permisos. |
| `PUT /api/v1/users/{id}/change-password` (self) | Solo self-service: el handler valida que `userId == tokenUserId`, sin `@PreAuthorize` adicional | Un usuario solo cambia su propia contraseña. |

### ZONA C — Catálogos y módulos operacionales (totalmente delegable)

Todos los módulos que no gestionan seguridad. Un rol no-admin puede recibir cualquier acción (CREATE/READ/UPDATE/DELETE) para estos módulos.

| Módulo | `entity` en BD | Acciones disponibles |
|--------|---------------|---------------------|
| 01 — Document Types | `DOCUMENT_TYPE` | CREATE, READ, UPDATE, DELETE |
| 02 — Geography | `GEOGRAPHY` | CREATE, READ, UPDATE, DELETE |
| 03 — Payment Methods | `PAYMENT_METHOD` | CREATE, READ, UPDATE, DELETE |
| 04 — Tax Types | `TAX_TYPE` | CREATE, READ, UPDATE, DELETE |
| 05 — Units of Measure | `UNIT_OF_MEASURE` | CREATE, READ, UPDATE, DELETE |
| 08 — Warehouses | `WAREHOUSE` | CREATE, READ, UPDATE, DELETE |
| 09 — Product Categories | `PRODUCT_CATEGORY` | CREATE, READ, UPDATE, DELETE |
| 10 — Brands | `BRAND` | CREATE, READ, UPDATE, DELETE |
| 07 — Inventory *(futuro)* | `INVENTORY` | CREATE, READ, UPDATE *(sin DELETE — movimientos inmutables)* |
| 11 — Items *(futuro)* | `ITEM` | CREATE, READ, UPDATE, DELETE |
| 12 — Price Lists *(futuro)* | `PRICE_LIST` | CREATE, READ, UPDATE, DELETE, APPROVE_DISCOUNT |

---

## 4. Reglas anti-escalada de privilegios

Estas reglas son absolutas. No existe excepción ni caso de uso que las supere.

| # | Regla | Consecuencia de violarla |
|---|-------|--------------------------|
| AE-01 | Un no-admin no puede crear roles. | Crearía un rol con todos los permisos y se lo asignaría. |
| AE-02 | Un no-admin no puede modificar roles ni asignarles permisos. | Agregaría permisos a su propio rol. |
| AE-03 | Un no-admin no puede crear permisos. | Crearía rutas de acceso nuevas. |
| AE-04 | Un no-admin no puede crear usuarios. | Crearía un usuario con rol ADMIN. |
| AE-05 | Un no-admin no puede asignar roles a usuarios. | Se asignaría a sí mismo el rol ADMIN. |
| AE-06 | Un no-admin no puede eliminar usuarios. | Podría eliminar al admin dejando el sistema sin control. |
| AE-07 | Un no-admin no puede cambiar la contraseña de otro usuario. | Tomaría el control de la cuenta del admin. |

---

## 5. Patrón de `@PreAuthorize` por zona

### Zona A (siempre ADMIN)

```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<...> create(...) { ... }

@GetMapping
// Sin @PreAuthorize — autenticación cubre anyRequest().authenticated()
public ResponseEntity<...> list(...) { ... }
```

### Zona B (delegable con permiso específico)

```java
@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER:UPDATE')")
public ResponseEntity<...> update(...) { ... }

@PutMapping("/{id}/unlock")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER:UNLOCK')")
public ResponseEntity<Void> unlock(...) { ... }
```

### Zona C (catálogos y operacional — patrón granular)

```java
// GET — sin @PreAuthorize
@GetMapping
public ResponseEntity<...> list(...) { ... }

@GetMapping("/{uuid}")
public ResponseEntity<...> findByUuid(...) { ... }

// CREATE
@PostMapping
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ENTITY:CREATE')")
public ResponseEntity<...> create(...) { ... }

// UPDATE (también aplica a activate/deactivate)
@PutMapping("/{uuid}")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ENTITY:UPDATE')")
public ResponseEntity<...> update(...) { ... }

@PatchMapping("/{uuid}/activate")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ENTITY:UPDATE')")
public ResponseEntity<...> activate(...) { ... }

@PatchMapping("/{uuid}/deactivate")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ENTITY:UPDATE')")
public ResponseEntity<...> deactivate(...) { ... }

// DELETE
@DeleteMapping("/{uuid}")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('ENTITY:DELETE')")
public ResponseEntity<Void> delete(...) { ... }
```

Sustituir `ENTITY` por el nombre exacto del módulo en mayúsculas (ej. `PRODUCT_CATEGORY`, `BRAND`, `WAREHOUSE`).

---

## 6. Permisos en la tabla `permissions`

### Qué se inserta y qué no

**Se insertan** los permisos de módulos Zona B y Zona C (todos los que tienen efecto real en un controller).

**No se insertan** permisos para operaciones de Zona A (CREATE/UPDATE/DELETE en roles, permisos, crear/eliminar/asignar-roles en usuarios), porque el controller los verifica solo por `hasRole('ADMIN')` — no hay permiso granular que los desbloquee.

### Registro de permisos por feature

| Migración | Entity | Acciones insertadas | ADMIN | USER |
|-----------|--------|---------------------|-------|------|
| V15 | `WAREHOUSE` | CREATE, READ, UPDATE, DELETE | Todos | READ |
| V17 | `PRODUCT_CATEGORY` | CREATE, READ, UPDATE, DELETE | Todos | READ |
| V19 | `BRAND` | CREATE, READ, UPDATE, DELETE | Todos | READ |
| *Futuros* | `INVENTORY`, `ITEM`, `PRICE_LIST`, `DOCUMENT_TYPE`, `GEOGRAPHY`, `PAYMENT_METHOD`, `TAX_TYPE`, `UNIT_OF_MEASURE` | Según módulo | Todos | READ |
| Zona B | `USER` | UPDATE, UNLOCK | Ambos | — (se asigna explícitamente) |

> **READ en controllers**: El permiso `READ` se inserta en BD para ser **asignable** a roles. Sin embargo, los endpoints `GET` no llevan `@PreAuthorize` — la autenticación (`anyRequest().authenticated()`) es suficiente. Todo usuario autenticado puede leer, independientemente de si tiene o no el permiso `READ` asignado.

### Convención de IDs de permisos

| Prefijo UUID | Módulo |
|---|---|
| `10000000-0000-0000-0000-00000000000X` | WAREHOUSE (X=1 CREATE, 2 READ, 3 UPDATE, 4 DELETE) |
| `11000000-0000-0000-0000-00000000000X` | PRODUCT_CATEGORY |
| `12000000-0000-0000-0000-00000000000X` | BRAND |
| `13000000-0000-0000-0000-00000000000X` | INVENTORY *(reservado)* |
| `14000000-0000-0000-0000-00000000000X` | ITEM *(reservado)* |
| `15000000-0000-0000-0000-00000000000X` | PRICE_LIST *(reservado)* |
| `16000000-0000-0000-0000-00000000000X` | USER (zona B) *(reservado)* |

---

## 7. Estado de migración por módulo

Los módulos implementados antes de esta política (01-08) usan `hasRole('ADMIN')` puro en sus controllers. **No se modifican retroactivamente.** La política granular aplica a partir de Feature 09.

| Módulo | Implementado | Patrón en controller |
|--------|-------------|---------------------|
| 01 — Document Types | ✅ (parcial) | `hasRole('ADMIN')` — no se cambia |
| 02 — Geography | ✅ | `hasRole('ADMIN')` — no se cambia |
| 03 — Payment Methods | ✅ | `hasRole('ADMIN')` — no se cambia |
| 04 — Tax Types | ✅ | `hasRole('ADMIN')` — no se cambia |
| 05 — Units of Measure | ✅ | `hasRole('ADMIN')` — no se cambia |
| 06 — Security | ✅ | `hasRole('ADMIN')` — Zona A permanece así |
| 07 — Inventory | ⏳ | Implementará patrón granular |
| 08 — Warehouses | ✅ | `hasRole('ADMIN')` — no se cambia |
| **09 — Product Categories** | ⏳ | **Patrón granular** (`hasRole('ADMIN') or hasAuthority(...)`) |
| **10 — Brands** | ⏳ | **Patrón granular** (`hasRole('ADMIN') or hasAuthority(...)`) |
| **11+ — Futuros** | ⏳ | **Patrón granular** |

---

## 8. Enum `PermissionAction` — uso por módulo

```
CREATE          → POST endpoints (crear recursos)
READ            → GET endpoints (consultar) — se inserta en BD pero no se verifica en controller
UPDATE          → PUT y PATCH endpoints (modificar, activar, desactivar)
DELETE          → DELETE endpoints (eliminar / soft delete)
APPROVE         → Aprobaciones de operaciones (futuro)
APPROVE_DISCOUNT → Aprobar descuentos fuera de política (futuro: Price Lists)
UNLOCK          → Desbloquear cuentas de usuario (Zona B)
```

---

## 9. Decisiones registradas

| ID | Decisión | Fecha |
|----|----------|-------|
| PD-01 | Los módulos 01-08 ya implementados mantienen `hasRole('ADMIN')` — no se migran retroactivamente. | 2026-02-20 |
| PD-02 | Los permisos `READ` se insertan en BD para completitud y asignabilidad futura, pero no se verifican en controllers (GET abiertos a autenticados). | 2026-02-20 |
| PD-03 | `activate` y `deactivate` usan `ENTITY:UPDATE` (no un action específico) para evitar proliferación de acciones. | 2026-02-20 |
| PD-04 | No existe permiso granular para ninguna operación de Zona A. La verificación siempre es `hasRole('ADMIN')` puro. | 2026-02-20 |
| PD-05 | `condition_expr` en permisos es `NULL` en esta fase. El soporte SpEL queda como capacidad reservada para fases futuras. | 2026-02-20 |
