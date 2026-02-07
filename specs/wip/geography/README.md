# Geography Module

Módulo de gestión de la división político-administrativa de Colombia (departamentos y municipios) con codificación DANE.

## Descripción General

Este módulo permite la administración de:
- **Departamentos**: 33 divisiones administrativas de Colombia (código DANE de 2 dígitos)
- **Municipios**: ~1,100+ divisiones municipales (código DANE de 5 dígitos), vinculados a un departamento

Relación: `Department (1) → (N) Municipality`

## Arquitectura

Implementado siguiendo **arquitectura hexagonal** (Ports & Adapters) con separación CQRS:

```
domain/
├── model/geography/          # Department (Aggregate Root), Municipality (Entity)
├── exception/geography/      # 7 excepciones específicas
├── service/geography/        # GeographyValidator, GeographyDomainService (POJOs)
└── port/geography/           # DepartmentRepository, MunicipalityRepository (Output Ports)

application/
├── port/geography/           # CompareDepartmentsUseCase, ManageDepartmentUseCase, etc. (Input Ports)
└── usecase/geography/        # 4 implementaciones (Compare* = queries, Manage* = commands)

infrastructure/
├── config/                   # BeanConfiguration (beans de dominio sin Spring)
├── in/web/
│   ├── controller/geography/ # DepartmentController, MunicipalityController
│   ├── dto/geography/        # 6 DTOs (Create/Update Request + Response)
│   ├── mapper/geography/     # DepartmentDtoMapper, MunicipalityDtoMapper (MapStruct)
│   └── advice/               # GlobalExceptionHandler (6 handlers geography)
└── out/persistence/
    ├── entity/geography/     # DepartmentEntity, MunicipalityEntity (JPA)
    ├── mapper/geography/     # DepartmentEntityMapper, MunicipalityEntityMapper (MapStruct)
    ├── adapter/geography/    # DepartmentRepositoryAdapter, MunicipalityRepositoryAdapter
    ├── util/geography/       # DepartmentSpecificationUtil, MunicipalitySpecificationUtil
    ├── DepartmentJpaRepository.java
    └── MunicipalityJpaRepository.java
```

## API Endpoints

### Departamentos — `/api/geography/departments`

| Método | Endpoint | Descripción | Código |
|--------|----------|-------------|--------|
| `POST` | `/api/geography/departments` | Crear departamento | 201 |
| `GET` | `/api/geography/departments` | Listar con paginación y filtros | 200 |
| `GET` | `/api/geography/departments/active` | Listar solo activos | 200 |
| `GET` | `/api/geography/departments/{uuid}` | Obtener por UUID | 200/404 |
| `GET` | `/api/geography/departments/code/{code}` | Obtener por código DANE | 200/404 |
| `PUT` | `/api/geography/departments/{uuid}` | Actualizar departamento | 200/404 |
| `DELETE` | `/api/geography/departments/{uuid}` | Eliminar departamento | 204/404/409 |
| `PATCH` | `/api/geography/departments/{uuid}/activate` | Activar | 200/404 |
| `PATCH` | `/api/geography/departments/{uuid}/deactivate` | Desactivar | 200/404 |

### Municipios — `/api/geography/municipalities`

| Método | Endpoint | Descripción | Código |
|--------|----------|-------------|--------|
| `POST` | `/api/geography/municipalities` | Crear municipio | 201 |
| `GET` | `/api/geography/municipalities` | Listar con paginación y filtros | 200 |
| `GET` | `/api/geography/municipalities/active` | Listar solo activos | 200 |
| `GET` | `/api/geography/municipalities/{uuid}` | Obtener por UUID | 200/404 |
| `PUT` | `/api/geography/municipalities/{uuid}` | Actualizar municipio | 200/404 |
| `DELETE` | `/api/geography/municipalities/{uuid}` | Eliminar municipio | 204/404 |
| `PATCH` | `/api/geography/municipalities/{uuid}/activate` | Activar | 200/404 |
| `PATCH` | `/api/geography/municipalities/{uuid}/deactivate` | Desactivar | 200/404 |

### Filtros de listado

| Parámetro | Tipo | Descripción | Aplica a |
|-----------|------|-------------|----------|
| `enabled` | Boolean | Filtrar por estado activo/inactivo | Ambos |
| `search` | String | Búsqueda global en código y nombre | Ambos |
| `departmentId` | Long | Filtrar por departamento | Solo municipios |
| `page` | Integer | Página (0-based, default: 0) | Ambos |
| `size` | Integer | Elementos por página (default: 10) | Ambos |
| `sort` | String | Campo de ordenamiento (default: id) | Ambos |
| `direction` | String | Dirección: asc/desc (default: asc) | Ambos |

## Ejemplos de Request/Response

### Crear departamento

```json
// POST /api/geography/departments
{
  "code": "05",
  "name": "Antioquia"
}

// Response 201
{
  "id": 1,
  "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "code": "05",
  "name": "Antioquia",
  "enabled": true,
  "createdBy": null,
  "updatedBy": null,
  "createdAt": "2026-02-07T10:00:00",
  "updatedAt": "2026-02-07T10:00:00"
}
```

### Crear municipio

```json
// POST /api/geography/municipalities
{
  "departmentId": 1,
  "code": "05001",
  "name": "Medellín"
}

// Response 201
{
  "id": 1,
  "uuid": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "code": "05001",
  "name": "Medellín",
  "department": {
    "id": 1,
    "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "code": "05",
    "name": "Antioquia",
    "enabled": true
  },
  "enabled": true,
  "createdBy": null,
  "updatedBy": null,
  "createdAt": "2026-02-07T10:00:00",
  "updatedAt": "2026-02-07T10:00:00"
}
```

### Listar municipios filtrados

```
GET /api/geography/municipalities?departmentId=1&enabled=true&search=med&page=0&size=10&sort=name&direction=asc
```

## Reglas de Negocio

1. **Código de departamento**: exactamente 2 dígitos (`^\d{2}$`), único en el sistema
2. **Código de municipio**: exactamente 5 dígitos (`^\d{5}$`), único por departamento
3. **Nombre**: entre 1 y 100 caracteres, requerido
4. **Eliminación de departamento**: solo si no tiene municipios asociados (409 si los tiene)
5. **Creación de municipio**: el departamento referenciado debe existir (404 si no)
6. **Soft delete**: se usa flag `enabled` (true/false), no eliminación física
7. **Activación/Desactivación**: cambia el campo `enabled` del registro

## Base de Datos

### Tablas

- `departments`: id (PK), uuid (BINARY 16 UNIQUE), code (VARCHAR 10 UNIQUE), name (VARCHAR 100), enabled, audit fields
- `municipalities`: id (PK), uuid (BINARY 16 UNIQUE), code (VARCHAR 10), name (VARCHAR 100), department_id (FK), enabled, audit fields
  - Constraint: `UNIQUE(code, department_id)`
  - FK: `department_id REFERENCES departments(id) ON DELETE RESTRICT`

### Migraciones Flyway

- `V3__create_geography_tables.sql` — Creación de tablas e índices
- `V4__insert_colombia_geography.sql` — Seed data: 33 departamentos + ~400 municipios (códigos DANE)

## Tests

14 archivos de tests unitarios, 193 tests totales:

| Capa | Archivo | Tests |
|------|---------|-------|
| Domain | DepartmentTest | 11 |
| Domain | MunicipalityTest | 9 |
| Domain | GeographyValidatorTest | 50 |
| Domain | GeographyDomainServiceTest | 25 |
| Application | CompareDepartmentsUseCaseImplTest | 9 |
| Application | ManageDepartmentUseCaseImplTest | 10 |
| Application | CompareMunicipalitiesUseCaseImplTest | 7 |
| Application | ManageMunicipalityUseCaseImplTest | 10 |
| Infrastructure | DepartmentDtoMapperTest | 7 |
| Infrastructure | MunicipalityDtoMapperTest | 9 |
| Infrastructure | DepartmentEntityMapperTest | 6 |
| Infrastructure | MunicipalityEntityMapperTest | 6 |
| Infrastructure | DepartmentControllerTest | 18 |
| Infrastructure | MunicipalityControllerTest | 16 |

Cobertura por capa (JaCoCo):
- Domain (model + service): **97-100%**
- Application (use cases): **100%**
- Controllers: **95%**
- Mappers (DTO + Entity): **95-97%**

## Documentación API

Anotaciones OpenAPI (`@Operation`, `@ApiResponses`, `@Parameter`, `@Tag`) en ambos controllers.
Swagger UI disponible en: `http://localhost:8080/swagger-ui.html`

## Especificaciones

| Documento | Descripción |
|-----------|-------------|
| [1-functional-spec.md](1-functional-spec.md) | Especificación funcional |
| [2-technical-spec.md](2-technical-spec.md) | Especificación técnica |
| [3-tasks.json](3-tasks.json) | Plan de tareas |
| [status.md](status.md) | Estado actual del módulo |
