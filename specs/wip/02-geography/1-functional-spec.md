# Functional Specification: Módulo de Geografía

**Feature**: Geography Module (Departments & Municipalities)  
**Created**: January 10, 2026  
**Updated**: February 12, 2026  
**Module Type**: Independent Catalog (Sin dependencias)  
**Phase**: PHASE 1 - Draft

**Latest Update (2026-02-12)**: Agregado endpoint para obtener municipios por departamento sin paginación
- ✅ Nuevo Scenario 2.9 - Para uso en dropdowns/selects del frontend
- ✅ Respuesta simplificada sin campos de auditoría ni objeto department
- ✅ No afecta endpoints existentes

---

## 📋 Overview

El módulo de Geografía gestiona la jerarquía de ubicaciones administrativas de Colombia (Departamento > Municipio). Es un catálogo base independiente sin dependencias de otros módulos, requerido por múltiples módulos del sistema (Company, Contact, Warehouse, Sales, Purchases) para la gestión de direcciones y reportes por ubicación.

### Business Value
- Elimina ingreso manual de 32 departamentos y 1,100+ municipios
- Garantiza consistencia en datos geográficos
- Habilita búsqueda y validación de direcciones
- Permite reportes por ubicación geográfica

---

## 👥 User Stories

### User Story 1: Gestión de Departamentos (Priority: P1)

**Como** administrador del sistema  
**Quiero** gestionar el catálogo de departamentos  
**Para** poder usarlo en la configuración de empresas, contactos y direcciones

**Why this priority?** Es el nivel base de la jerarquía geográfica y es requerido por múltiples módulos.

**Acceptance Criteria:**

1. ✅ Puedo crear departamentos con código único y nombre
2. ✅ Puedo listar departamentos activos con paginación
3. ✅ Puedo buscar departamentos por nombre (case-insensitive)
4. ✅ Puedo actualizar información de departamentos
5. ✅ Puedo desactivar/activar departamentos (soft delete)
6. ✅ El sistema valida códigos únicos (2 dígitos)
7. ✅ El sistema previene eliminación si hay municipios asociados
8. ✅ Todas las operaciones quedan registradas en auditoría

**Acceptance Scenarios:**

#### Scenario 1.1: Crear departamento con datos completos
- **Given** estoy autenticado como administrador
- **When** envío POST /api/departments con:
  ```json
  {
    "code": "05",
    "name": "Antioquia",
    "enabled": true
  }
  ```
- **Then** recibo status 201 con el departamento creado
- **And** el departamento tiene UUID asignado
- **And** se registra en AuditLog: action=DEPARTMENT_CREATED

#### Scenario 1.2: Validación de código único
- **Given** existe departamento con code "05"
- **When** intento crear otro departamento con code "05"
- **Then** recibo status 409 con mensaje "Department code already exists"

#### Scenario 1.3: Listar departamentos activos con paginación
- **Given** existen 32 departamentos, 30 activos
- **When** envío GET /api/departments?enabled=true&page=0&size=20
- **Then** recibo status 200 con 20 departamentos activos
- **And** metadata de paginación correcta (total, pages, current)

#### Scenario 1.4: Validación de eliminación con relaciones
- **Given** departamento "Antioquia" tiene 125 municipios asociados
- **When** intento DELETE /api/departments/{uuid}
- **Then** recibo status 409 con mensaje "Cannot delete department with associated municipalities"

#### Scenario 1.5: Desactivar departamento
- **Given** departamento activo con uuid
- **When** envío PATCH /api/departments/{uuid}/deactivate
- **Then** recibo status 200 con enabled=false
- **And** el departamento no aparece en búsquedas con filtro enabled=true

#### Scenario 1.6: Búsqueda de departamento por nombre
- **Given** existen múltiples departamentos
- **When** envío GET /api/departments/search?name=Antio
- **Then** recibo status 200 con departamentos cuyo nombre contenga "Antio"
- **And** resultados ordenados alfabéticamente

#### Scenario 1.7: Actualizar departamento
- **Given** existe departamento con uuid
- **When** envío PUT /api/departments/{uuid} con datos actualizados
- **Then** recibo status 200 con el departamento actualizado
- **And** se registra en AuditLog: action=DEPARTMENT_UPDATED

---

### User Story 2: Gestión de Municipios (Priority: P1)

**Como** administrador  
**Quiero** gestionar municipios dentro de cada departamento  
**Para** completar la información de direcciones de contactos, empresas y bodegas

**Why this priority?** Segundo nivel de la jerarquía geográfica, esencial para direcciones completas y reportes por ubicación.

**Acceptance Criteria:**

1. ✅ Puedo crear municipios asociados a un departamento
2. ✅ Puedo listar municipios con filtro por departamento
3. ✅ Puedo buscar municipios por nombre
4. ✅ Puedo usar autocompletado de municipios en formularios
5. ✅ Puedo actualizar información de municipios
6. ✅ Puedo desactivar/activar municipios
7. ✅ El sistema valida código único por departamento (5 dígitos)
8. ✅ El sistema valida que el departamento exista y esté activo
9. ✅ Todas las operaciones quedan registradas en auditoría

**Acceptance Scenarios:**

#### Scenario 2.1: Crear municipio asociado a departamento
- **Given** existe departamento "Antioquia" (id=1)
- **When** envío POST /api/municipalities con:
  ```json
  {
    "departmentId": 1,
    "code": "05001",
    "name": "Medellín",
    "enabled": true
  }
  ```
- **Then** recibo status 201 con el municipio creado
- **And** el municipio incluye información del departamento
- **And** se registra en AuditLog: action=MUNICIPALITY_CREATED

#### Scenario 2.2: Validación de código único por departamento
- **Given** departamento "Antioquia" tiene municipio con code "05001"
- **When** intento crear otro municipio en Antioquia con code "05001"
- **Then** recibo status 409 con mensaje "Municipality code already exists for this department"

#### Scenario 2.3: Listar municipios por departamento
- **Given** Antioquia tiene 125 municipios
- **When** envío GET /api/municipalities?departmentId=1&page=0&size=50
- **Then** recibo status 200 con 50 municipios
- **And** cada municipio incluye información del departamento

#### Scenario 2.4: Búsqueda de municipio por nombre
- **Given** existen múltiples municipios
- **When** envío GET /api/municipalities/search?name=Medellin
- **Then** recibo status 200 con municipios cuyo nombre contenga "Medellin"
- **And** incluye información del departamento para cada resultado

#### Scenario 2.5: Autocompletado de municipio para formularios
- **Given** usuario ingresa "Med" en campo de municipio
- **When** envío GET /api/municipalities/autocomplete?query=Med&departmentId=1&limit=10
- **Then** recibo status 200 con máximo 10 municipios que coincidan
- **And** formato compacto: `[{id, uuid, name, departmentName}]`

#### Scenario 2.6: Obtener municipio con jerarquía completa
- **Given** municipio "Medellín" con uuid
- **When** envío GET /api/municipalities/{uuid}
- **Then** recibo status 200 con municipio y su departamento completo

#### Scenario 2.7: Actualizar municipio
- **Given** existe municipio con uuid
- **When** envío PUT /api/municipalities/{uuid} con datos actualizados
- **Then** recibo status 200 con el municipio actualizado
- **And** se registra en AuditLog: action=MUNICIPALITY_UPDATED

#### Scenario 2.8: Desactivar municipio
- **Given** municipio activo con uuid
- **When** envío PATCH /api/municipalities/{uuid}/deactivate
- **Then** recibo status 200 con enabled=false

#### Scenario 2.9: Obtener todos los municipios por departamento (sin paginación)
- **Given** departamento "Antioquia" con uuid tiene 125 municipios activos
- **When** envío GET /api/departments/{departmentUuid}/municipalities
- **Then** recibo status 200 con lista completa de 125 municipios
- **And** cada municipio incluye información básica (uuid, code, name) sin campos de auditoría ni objeto department
- **And** los municipios están ordenados alfabéticamente por nombre
- **And** solo retorna municipios activos (enabled=true)

---

### User Story 3: Carga Inicial de Datos Geográficos (Priority: P1)

**Como** administrador  
**Quiero** cargar masivamente los datos geográficos de Colombia  
**Para** no tener que ingresarlos manualmente

**Why this priority?** Evita trabajo manual tedioso y asegura consistencia en los datos maestros.

**Acceptance Criteria:**

1. ✅ Puedo importar datos de Colombia con un solo comando
2. ✅ Se cargan 32 departamentos
3. ✅ Se cargan 1,100+ municipios
4. ✅ Cada municipio está correctamente asociado a su departamento
5. ✅ El sistema evita duplicados en reimportación
6. ✅ Recibo reporte de registros cargados/omitidos

**Acceptance Scenarios:**

#### Scenario 3.1: Carga masiva de datos de Colombia
- **Given** el sistema está vacío de datos geográficos
- **When** ejecuto POST /api/import/colombia
- **Then** se cargan 32 departamentos y 1,100+ municipios
- **And** cada municipio está asociado correctamente
- **And** recibo status 200 con resumen: `{departmentsLoaded: 32, municipalitiesLoaded: 1122}`

#### Scenario 3.2: Validación de duplicados en carga masiva
- **Given** ya existen departamentos y municipios en la BD
- **When** ejecuto POST /api/import/colombia
- **Then** se omiten registros duplicados por code
- **And** recibo status 200 con resumen: `{skipped: 150, inserted: 0}`

---

## 📊 Business Rules

### BR-01: Validación de Códigos
- Code de departamento: **2 dígitos** (ej: "05")
- Code de municipio: **5 dígitos** (ej: "05001")
- Códigos deben ser únicos según nivel (departamento: global, municipio: por departamento)

### BR-02: Integridad Referencial
- No se puede eliminar departamento con municipios asociados
- Municipio debe referenciar un departamento válido y activo
- Al desactivar departamento, sus municipios no aparecen en búsquedas activas

### BR-03: Soft Delete
- Usar campo `enabled` en lugar de eliminar físicamente
- Registros desactivados no aparecen en búsquedas por defecto
- Se puede reactivar con PATCH /activate

### BR-04: Auditoría
- Todos los cambios registran: createdBy, updatedBy, createdAt, updatedAt
- Operaciones críticas generan eventos de auditoría (CREATED, UPDATED, DELETED)

### BR-05: Búsqueda y Paginación
- Búsquedas case-insensitive
- Paginación con tamaño máximo de 100 items
- Ordenamiento por defecto: alfabético por name
- Autocompletado limitado a 10 resultados por defecto

---

## 🎯 Functional Requirements

### FR-01: CRUD de Departamentos
- ✅ Crear departamento con código y nombre
- ✅ Listar departamentos con filtros (enabled, name)
- ✅ Buscar departamento por UUID
- ✅ Actualizar departamento
- ✅ Desactivar/activar departamento
- ✅ Eliminar departamento (con validación de relaciones)
- ✅ Buscar por nombre con autocompletado

### FR-02: CRUD de Municipios
- ✅ Crear municipio asociado a departamento
- ✅ Listar municipios con filtros (departmentId, enabled, name)
- ✅ Buscar municipio por UUID
- ✅ Actualizar municipio
- ✅ Desactivar/activar municipio
- ✅ Eliminar municipio
- ✅ Buscar por nombre con autocompletado
- ✅ Autocompletado para formularios (query rápida)
- ✅ Obtener todos los municipios de un departamento (sin paginación)

### FR-03: Importación Masiva
- ✅ Importar datos de Colombia (32 departamentos + 1,100+ municipios)
- ✅ Validar duplicados por código
- ✅ Reportar estadísticas de importación

### FR-04: Validaciones
- ✅ Código único (departamento: global, municipio: por departamento)
- ✅ Nombres no vacíos
- ✅ Formato de código correcto (2 o 5 dígitos)
- ✅ Departamento existente y activo al crear municipio
- ✅ Prevenir eliminación con relaciones

### FR-05: Paginación y Filtros
- ✅ Paginación estándar (page, size, sort)
- ✅ Filtro por enabled (true/false)
- ✅ Filtro por departmentId (para municipios)
- ✅ Búsqueda por nombre parcial (LIKE)
- ✅ Ordenamiento configurable

---

## 🚫 Out of Scope

Los siguientes elementos **NO** están incluidos en esta versión:

❌ Otros niveles geográficos (veredas, barrios, comunas)  
❌ Coordenadas GPS/geolocalización  
❌ Importación de otros países  
❌ Fusión de municipios  
❌ Jerarquía de más de 2 niveles  
❌ Multiidioma (solo español)  
❌ API pública sin autenticación  

---

## 📄 API Endpoints Summary

### Departamentos

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/departments | Crear departamento |
| GET | /api/departments | Listar departamentos (paginado) |
| GET | /api/departments/{uuid} | Obtener departamento |
| PUT | /api/departments/{uuid} | Actualizar departamento |
| DELETE | /api/departments/{uuid} | Eliminar departamento |
| PATCH | /api/departments/{uuid}/deactivate | Desactivar |
| PATCH | /api/departments/{uuid}/activate | Activar |
| GET | /api/departments/search | Búsqueda por nombre |

### Municipios

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/municipalities | Crear municipio |
| GET | /api/municipalities | Listar municipios (paginado) |
| GET | /api/municipalities/{uuid} | Obtener municipio |
| PUT | /api/municipalities/{uuid} | Actualizar municipio |
| DELETE | /api/municipalities/{uuid} | Eliminar municipio |
| PATCH | /api/municipalities/{uuid}/deactivate | Desactivar |
| PATCH | /api/municipalities/{uuid}/activate | Activar |
| GET | /api/municipalities/search | Búsqueda por nombre |
| GET | /api/municipalities/autocomplete | Autocompletado |
| GET | /api/departments/{uuid}/municipalities | Obtener todos los municipios de un departamento |

### Importación

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/import/colombia | Carga masiva de Colombia |

---

## 🔒 Security Requirements

### Autenticación
- Todos los endpoints requieren autenticación JWT
- Token válido en header: `Authorization: Bearer <token>`

### Autorización
- **SUPERADMIN, ADMIN**: Acceso completo (CRUD)
- **USER**: Solo consultas (GET)
- Operaciones de escritura (POST, PUT, DELETE, PATCH) solo para ADMIN+

### Auditoría
- Registrar usuario que realiza cada operación
- Timestamp de creación y modificación
- Eventos de auditoría para operaciones críticas

---

## 📈 Non-Functional Requirements (High-Level)

### Performance
- Autocompletado: < 100ms p95
- Listados paginados: < 200ms p95
- Importación masiva: < 30 segundos

### Usability
- Búsquedas case-insensitive
- Mensajes de error claros y específicos
- Respuestas paginadas con metadata completa

### Reliability
- Validaciones en múltiples capas (API, dominio, BD)
- Transacciones para operaciones críticas
- Tests de cobertura > 90%

---

## 🔗 Dependencies

**Dependencias de este módulo**: Ninguna (catálogo base independiente)

**Módulos que dependen de Geography**:
- Company (direcciones de empresas)
- Contact (direcciones de contactos)
- Warehouse (ubicación de bodegas)
- Sales (origen/destino de ventas)
- Purchases (proveedores por ubicación)

---

## ✅ Acceptance Criteria (Summary)

### Para considerar esta feature completa:

1. ✅ Todos los 17 endpoints funcionando correctamente
2. ✅ Validaciones de negocio implementadas
3. ✅ Importación de datos de Colombia exitosa
4. ✅ Tests con cobertura > 90%
5. ✅ Documentación API actualizada (Swagger)
6. ✅ Performance dentro de objetivos
7. ✅ Auditoría funcionando en todas las operaciones
8. ✅ Autenticación y autorización implementadas

---

## 📝 Notes

- Esta especificación cubre **solo la parte funcional** del módulo
- Ver [technical-spec.md](2-technical-spec.md) para detalles técnicos de arquitectura, base de datos, y diseño
- Este es un **catálogo base independiente** que debe implementarse antes de módulos que lo requieran

---

**Status**: ⚠️ PHASE 1 - Draft  
**Next Step**: Review → Clarify → Approve → Move to PHASE 2
