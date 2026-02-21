# Especificación Funcional: Módulo de Categorías de Productos

**Creado:** 20 de febrero de 2026
**Número de Funcionalidad:** 09
**Prerequisito de:** 11-items

---

## Propósito

Catálogo plano de categorías para clasificar ítems (productos y servicios). Sin jerarquía. La clasificación adicional se gestiona mediante tags en Feature 11.

---

## Reglas de Validación de Campos

| Campo | Obligatorio | Restricciones |
|-------|------------|---------------|
| `code` | Sí (solo en POST) | Patrón `^[A-Za-z0-9-]{2,20}$`. Se almacena en mayúsculas. Inmutable tras la creación. |
| `name` | Sí | 1–100 caracteres. No puede ser blank. |
| `description` | No | Máximo 500 caracteres. Puede ser `null`. String vacío (`""`) se trata igual que `null`. |

---

## Reglas de Negocio

| ID | Regla | Error |
|----|-------|-------|
| RN-01 | `code` es único globalmente (comparación sobre valor ya normalizado a mayúsculas, incluyendo soft-deleted). | 409 |
| RN-02 | `code` no se puede modificar después de la creación. No existe en el request de actualización. | — |
| RN-03 | No se puede eliminar una categoría referenciada por ítems. **Stub en esta fase: `canDelete()` retorna siempre `true`.** La validación real se implementa en Feature 11. | 422 |
| RN-04 | Una categoría recién creada tiene `active = true`. | — |

---

## Endpoints y Contratos HTTP

| Método | Ruta | Body | Status éxito | Errores posibles |
|--------|------|------|-------------|-----------------|
| POST | `/api/product-categories` | `CreateProductCategoryRequestDto` | 201 + Location | 400, 401, 403, 409 |
| GET | `/api/product-categories/{uuid}` | — | 200 | 401, 404 |
| GET | `/api/product-categories` | — (query params) | 200 | 400, 401 |
| PUT | `/api/product-categories/{uuid}` | `UpdateProductCategoryRequestDto` | 200 | 400, 401, 403, 404 |
| PATCH | `/api/product-categories/{uuid}/activate` | — | 200 | 401, 403, 404 |
| PATCH | `/api/product-categories/{uuid}/deactivate` | — | 200 | 401, 403, 404 |
| DELETE | `/api/product-categories/{uuid}` | — | 204 | 401, 403, 404, 422 |

**Autorización:**
- `GET`: cualquier usuario autenticado (rol `USER` o `ADMIN`).
- `POST`, `PUT`, `PATCH`, `DELETE`: exclusivo rol `ADMIN`.
- Sin JWT válido: **401**.
- JWT válido pero sin rol suficiente: **403**.

---

## Comportamiento por Escenario

### POST — Crear categoría

| Escenario | Input | Resultado |
|-----------|-------|-----------|
| Datos válidos, sin duplicado | `code: "elec"`, `name: "Electrónica"` | 201. `code` almacenado como `"ELEC"`. `active=true`. UUID generado en servidor. `createdBy` = ID del usuario del JWT. |
| `code` ya existe (mismo valor normalizado) | `code: "ELEC"` | 409. |
| `code` inválido (patrón, longitud) | `code: ""` o `code: "A"` o `code: "A B"` | 400. |
| `name` blank o ausente | `name: ""` | 400. |
| `description` supera 500 chars | `description: "..."` (501+) | 400. |

### GET `/{uuid}` — Obtener por UUID

| Escenario | Resultado |
|-----------|-----------|
| UUID existe y no está soft-deleted | 200 con el recurso. |
| UUID no existe | 404. |
| UUID existe pero está soft-deleted | 404. |

### GET — Listar (ver parámetros de filtro en spec técnica)

| Escenario | Resultado |
|-----------|-----------|
| Sin filtros | 200. Devuelve todos (activos e inactivos, excluye soft-deleted). Página 0, 10 registros, orden `name ASC`. |
| `limit=150` | 400. Límite máximo es 100. |
| `page=-1` | 400. Valor mínimo es 0. |

### PUT `/{uuid}` — Actualizar

| Escenario | Resultado |
|-----------|-----------|
| UUID existe. `name` válido. | 200. `updatedAt` y `updatedBy` actualizados. |
| `description` ausente del JSON | `description` se almacena como `null` (campo borrado). |
| `description` presente como `null` o `""` | `description` se almacena como `null`. |
| `name` blank | 400. |
| UUID no existe | 404. |

### PATCH `/activate` y `/deactivate`

| Escenario | Resultado |
|-----------|-----------|
| UUID existe, estado cambia | 200. `active` actualizado. `updatedAt` y `updatedBy` actualizados. |
| UUID existe, ya está en el estado solicitado | 200. No-op idempotente. |
| UUID no existe | 404. |

### DELETE `/{uuid}` — Soft delete

| Escenario | Resultado |
|-----------|-----------|
| UUID existe, sin ítems referenciados (stub: siempre) | 204. `deleted_at` y `deleted_by` poblados. |
| UUID no existe | 404. |
| UUID existe pero ya está soft-deleted | 404 (filtrado por `@SQLRestriction`). |

---

## Fuera de Alcance

- Jerarquía padre/hijo.
- Imágenes o iconos por categoría.
- Reordenamiento manual.
- Validación de referencias a ítems (Feature 11).
