# Status: Módulo de Gestión de Bodegas (Warehouses)

**Última actualización**: 2026-02-20
**Developer**: AI Assistant
**Estado general**: ✅ Specs Aprobadas — Listo para Implementación
**Versión**: 1.1.0

---

## 📊 Progreso General

- **Completado**: 0/39 tareas (0%)
- **En progreso**: 0 tareas
- **Bloqueado**: 0 tareas
- **Por hacer**: 39 tareas
- **Estimación total**: 43 story points

```
░░░░░░░░░░░░░░░░░░░░░░░░░ 0% completado
```

---

## 🎯 Estado Actual

### Planeación — ✅ COMPLETA

Todos los artefactos de specs han sido creados y revisados:
- `1-functional-spec.md` ✅
- `2-technical-spec.md` ✅
- `3-tasks.json` ✅ (39 tareas, 6 fases, 43 SP)
- `STATUS.md` ✅

Ambigüedades resueltas y specs aprobadas (v1.2). Ver decisiones documentadas abajo.

---

## 📐 Plan de Implementación

### PHASE-1: Foundation & Domain (9 SP)
| ID | Tarea | SP |
|----|-------|----|
| WH-001 | Crear enum WarehouseType | 1 |
| WH-002 | Crear domain model Warehouse | 2 |
| WH-003 | Crear excepciones de dominio (6 clases) | 2 |
| WH-004 | Crear WarehouseRepository port | 1 |
| WH-005 | Crear WarehouseValidator | 1 |
| WH-006 | Crear WarehouseDomainService | 1 |
| WH-007 | Crear WarehouseValidationService | 1 |

### PHASE-2: Database Schema & Migration (4 SP)
| ID | Tarea | SP |
|----|-------|----|
| WH-008 | Migración V13__create_warehouses_table.sql | 1 |
| WH-009 | Migración V14__insert_warehouses_seed_data.sql | 1 |
| WH-010 | Sincronizar docker/mysql-init/ (scripts 13 y 14) | 1 |
| WH-011 | Verificar compatibilidad H2 para tests | 1 |

### PHASE-3: Persistence Layer (6 SP)
| ID | Tarea | SP |
|----|-------|----|
| WH-012 | Crear WarehouseEntity (JPA) | 1 |
| WH-013 | Crear WarehouseJpaRepository | 1 |
| WH-014 | Crear WarehouseEntityMapper (MapStruct) | 1 |
| WH-015 | Crear WarehouseRepositoryAdapter | 2 |
| WH-016 | Crear WarehouseSpecificationUtil | 1 |

### PHASE-4: Application Layer — Use Cases (6 SP)
| ID | Tarea | SP |
|----|-------|----|
| WH-017 | Crear CreateWarehouseCommand | 1 |
| WH-018 | Crear UpdateWarehouseCommand | 1 |
| WH-019 | Crear ManageWarehouseUseCase (interfaz) | 1 |
| WH-020 | Crear CompareWarehouseUseCase (interfaz) | 1 |
| WH-021 | Crear ManageWarehouseUseCaseImpl | 1 |
| WH-022 | Crear CompareWarehouseUseCaseImpl | 1 |

### PHASE-5: Web Layer — Input Adapters (8 SP)
| ID | Tarea | SP |
|----|-------|----|
| WH-023 | Crear CreateWarehouseRequestDto | 1 |
| WH-024 | Crear UpdateWarehouseRequestDto | 1 |
| WH-025 | Crear WarehouseResponseDto | 1 |
| WH-026 | Crear WarehouseDtoMapper (MapStruct) | 1 |
| WH-027 | Crear WarehouseController (7 endpoints) | 2 |
| WH-028 | Actualizar BeanConfiguration | 1 |
| WH-029 | Actualizar GlobalExceptionHandler | 1 |

### PHASE-6: Testing & Quality (10 SP)
| ID | Tarea | SP |
|----|-------|----|
| WH-030 | Tests: Warehouse domain model | 1 |
| WH-031 | Tests: WarehouseValidator | 1 |
| WH-032 | Tests: WarehouseDomainService | 1 |
| WH-033 | Tests: ManageWarehouseUseCaseImpl | 1 |
| WH-034 | Tests: CompareWarehouseUseCaseImpl | 1 |
| WH-035 | Tests: WarehouseEntityMapper y WarehouseDtoMapper | 1 |
| WH-036 | Tests: WarehouseController | 1 |
| WH-037 | Tests: WarehouseValidationService | 1 |
| WH-038 | Tests: WarehouseRepositoryAdapter | 1 |
| WH-039 | Tests: WarehouseSpecificationUtil | 1 |

---

## 🔑 Decisiones Clave

| ID | Decisión | Detalle |
|----|----------|---------|
| DT-01 | UUID como identificador público | Consistente con todos los módulos |
| DT-02 | `municipalityId` = UUID, sin FK de BD | La API acepta UUID del municipio. En entidad se guarda como `municipalityUuid VARCHAR(36)`. Sin FK de BD para mantener desacoplamiento. |
| DT-03 | WarehouseValidationService sin cross-module check inicial | Inventario no existe aún; se añadirá en 07-inventory. Constructor sin argumentos. |
| DT-04 | Seed data idempotente con INSERT IGNORE | Permite re-ejecutar migrations sin error |
| DT-05 | `WarehouseDomainService(validator, repo)` | Necesita el repositorio para unicidad y BR-02.2. NO recibe `WarehouseValidationService`. |
| DT-06 | `ManageUseCaseImpl(repo, domainService, validationService)` | Separa lógica de negocio (DomainService) de integridad referencial (ValidationService). |
| DT-07 | Controller list usa `PagedResponseDto<T>` + params explícitos | Consistente con el patrón de `UnitOfMeasureController` establecido en el proyecto. |
| **BR-02.2** | Solo 1 bodega PRINCIPAL activa | Es la bodega del POS para ventas. Error 409 al intentar crear/activar segunda PRINCIPAL. |
| **BR-03.3** | No eliminar bodega PRINCIPAL activa | Si la bodega a eliminar es PRINCIPAL y está activa → `WarehouseInUseException (409)`. Debe desactivarse o reemplazarse primero. |
| **BR-04.3** | Código bloqueado tras eliminación | El código de una bodega eliminada NO puede reutilizarse. DB UNIQUE en `code` se mantiene. `existsByCode*` incluye eliminadas. |
| **SEC** | Permisos granulares fuera de scope | Solo autorización por rol (`ADMIN`). GET cubiertos por `anyRequest().authenticated()` en SecurityConfig. |

---

## 🌐 API REST Planificada

| Endpoint | Método | Auth | Status |
|----------|--------|------|--------|
| `/api/v1/warehouses` | POST | ADMIN | ⏳ |
| `/api/v1/warehouses/{uuid}` | GET | Authenticated | ⏳ |
| `/api/v1/warehouses` | GET | Authenticated | ⏳ |
| `/api/v1/warehouses/{uuid}` | PUT | ADMIN | ⏳ |
| `/api/v1/warehouses/{uuid}` | DELETE | ADMIN | ⏳ |
| `/api/v1/warehouses/{uuid}/activate` | PATCH | ADMIN | ⏳ |
| `/api/v1/warehouses/{uuid}/deactivate` | PATCH | ADMIN | ⏳ |

---

## 🗄️ Base de Datos

| Artefacto | Nombre | Estado |
|-----------|--------|--------|
| Migración schema | `V13__create_warehouses_table.sql` | ⏳ |
| Migración seed | `V14__insert_warehouses_seed_data.sql` | ⏳ |
| Docker init schema | `13_create_warehouses_table.sql` | ⏳ |
| Docker init seed | `14_insert_warehouses_seed_data.sql` | ⏳ |

---

## ⚠️ Blockers

_Ninguno. Planeación completa, listo para implementación._

---

## 🎯 Definition of Done

- [ ] Enum `WarehouseType` creado
- [ ] Domain model `Warehouse` con lifecycle methods
- [ ] 6 excepciones de dominio creadas
- [ ] `WarehouseRepository` port definido
- [ ] `WarehouseValidator`, `WarehouseDomainService`, `WarehouseValidationService` implementados
- [ ] Migración V13 (schema) + V14 (seed data con INSERT IGNORE)
- [ ] Scripts Docker sincronizados (13 + 14)
- [ ] Capas de persistencia: Entity, JpaRepository, EntityMapper, Adapter, SpecificationUtil
- [ ] Capas de aplicación: Commands, UseCase interfaces, UseCaseImpl
- [ ] Capa web: DTOs, DtoMapper, Controller (7 endpoints), BeanConfiguration, ExceptionHandler
- [ ] Tests unitarios: 10 suites (WH-030 a WH-039), cobertura >= 90%
- [ ] Build SUCCESS, 0 tests fallando
- [ ] Code review aprobado
- [ ] Módulo movido a `features/08-warehouses/`

---

## 🎬 Next Steps

1. Iniciar **PHASE-1** (Foundation & Domain): WH-001 → WH-007
2. Continuar con **PHASE-2** (Database): WH-008 → WH-011
3. Continuar con **PHASE-3** (Persistence): WH-012 → WH-016
4. Continuar con **PHASE-4** (Application): WH-017 → WH-022
5. Continuar con **PHASE-5** (Web Layer): WH-023 → WH-029
6. Cerrar con **PHASE-6** (Testing): WH-030 → WH-036
