# Especificación Técnica: Módulo de Tipos de Documento

**Creado:** 10 de enero de 2026  
**Actualizado:** 6 de febrero de 2026  
**Número de Funcionalidad:** 01  
**Arquitectura:** Hexagonal (Puertos y Adaptadores)

---

## Resumen Técnico

Este módulo implementa un catálogo de tipos de documento siguiendo principios de arquitectura hexagonal con aislamiento completo del dominio y clara separación entre lógica de negocio, orquestación de aplicación y preocupaciones de infraestructura.

### Stack Tecnológico
- **Lenguaje**: Java 21
- **Framework**: Spring Boot 3.x
- **ORM**: Spring Data JPA con Hibernate
- **Base de Datos**: MySQL 8.0+
- **Migraciones**: Flyway
- **Mapeo**: MapStruct 1.5.5
- **Validación**: Hibernate Validator (Bean Validation)
- **Pruebas**: JUnit 5, Mockito, Testcontainers
- **Documentación de API**: SpringDoc OpenAPI 3

---

## Arquitectura

### Capas de Arquitectura Hexagonal

```
┌─────────────────────────────────────────────────────────────┐
│                        CAPA DE DOMINIO                       │
│  (Lógica de negocio pura - sin dependencias de framework)   │
│                                                              │
│  • DocumentType (Raíz Agregada)                             │
│  • DocumentTypeDomainService (Reglas de Negocio)            │
│  • DocumentTypeValidator (Validación de Dominio)            │
│  • Excepciones de Dominio                                   │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE APLICACIÓN                        │
│  (Orquestación de casos de uso - capa delgada)              │
│                                                              │
│  PUERTOS DE ENTRADA (Interfaces):                           │
│  • CompareDocumentTypesUseCase                              │
│  • ManageDocumentTypeUseCase                                │
│                                                              │
│  IMPLEMENTACIONES DE CASOS DE USO:                          │
│  • CompareDocumentTypesUseCaseImpl                          │
│  • ManageDocumentTypeUseCaseImpl                            │
│                                                              │
│  PUERTOS DE SALIDA (Interfaces):                            │
│  • DocumentTypeRepository (Abstracción de repositorio)      │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
┌─────────────────────────────────────────────────────────────┐
│                  CAPA DE INFRAESTRUCTURA                     │
│  (Implementaciones específicas del framework)                │
│                                                              │
│  ADAPTADORES DE ENTRADA (in/web):                           │
│  • DocumentTypeController (REST API)                        │
│  • DTOs (Request/Response)                                  │
│  • Mappers (DTO ↔ Dominio)                                  │
│  • GlobalExceptionHandler (Advice)                          │
│                                                              │
│  ADAPTADORES DE SALIDA (out/persistence):                   │
│  • DocumentTypeRepositoryAdapter (Implementación de repo)   │
│  • DocumentTypeEntity (Entidad JPA)                         │
│  • Mappers (Entity ↔ Dominio)                               │
│  • JPA Repository                                           │
│                                                              │
│  CONFIGURACIÓN:                                             │
│  • BeanConfiguration (Inyección de Dependencias)            │
└─────────────────────────────────────────────────────────────┘
```

### Estructura de Paquetes (Organizada por Features)

**NOTA**: Cada feature tiene su propia carpeta dentro de cada capa para mantener el código organizado y escalable.

```
com.jcuadrado.erplitebackend/
│
├── domain/
│   ├── model/
│   │   └── document-types/
│   │       └── DocumentType.java                    # Raíz Agregada
│   ├── service/
│   │   └── document-types/
│   │       ├── DocumentTypeDomainService.java       # Reglas de negocio
│   │       └── DocumentTypeValidator.java           # Validación de dominio
│   ├── port/
│   │   └── out/
│   │       └── document-types/
│   │           └── DocumentTypeRepository.java      # Puerto de salida
│   └── exception/
│       └── document-types/
│           ├── DocumentTypeNotFoundException.java
│           ├── DuplicateCodeException.java
│           ├── InvalidDocumentTypeException.java
│           └── DocumentTypeDomainException.java
│
├── application/
│   ├── port/
│   │   └── in/
│   │       └── document-types/
│   │           ├── CompareDocumentTypesUseCase.java     # Operaciones de consulta
│   │           └── ManageDocumentTypeUseCase.java       # Operaciones de comando
│   └── usecase/
│       └── document-types/
│           ├── CompareDocumentTypesUseCaseImpl.java
│           └── ManageDocumentTypeUseCaseImpl.java
│
└── infrastructure/
    ├── config/
    │   └── BeanConfiguration.java                   # Configuración compartida
    │
    ├── in/
    │   └── web/
    │       ├── controller/
    │       │   └── document-types/
    │       │       └── DocumentTypeController.java
    │       ├── dto/
    │       │   └── document-types/
    │       │       ├── DocumentTypeDto.java
    │       │       ├── CreateDocumentTypeRequestDto.java
    │       │       ├── UpdateDocumentTypeRequestDto.java
    │       │       ├── DocumentTypeResponseDto.java
    │       │       ├── DocumentTypeComparisonDto.java
    │       │       ├── DocumentTypeFilterDto.java
    │       │       ├── SortDto.java
    │       │       └── PagedResponseDto.java
    │       ├── mapper/
    │       │   └── document-types/
    │       │       ├── DocumentTypeDtoMapper.java
    │       │       └── DocumentTypeComparisonDtoMapper.java
    │       └── advice/
    │           └── GlobalExceptionHandler.java       # Compartido entre features
    │
    └── out/
        └── persistence/
            ├── adapter/
            │   └── document-types/
            │       └── DocumentTypeRepositoryAdapter.java
            ├── entity/
            │   └── document-types/
            │       └── DocumentTypeEntity.java
            ├── mapper/
            │   └── document-types/
            │       └── DocumentTypeEntityMapper.java
            └── util/
                └── document-types/
                    └── DocumentTypeSpecificationUtil.java
```

**Ventajas de la Estructura por Features:**
1. **Escalabilidad**: Cada nuevo feature se agrega en su propia carpeta sin afectar otros
2. **Cohesión**: Todo el código relacionado con document-types está agrupado en cada capa
3. **Mantenibilidad**: Fácil encontrar y modificar código específico del feature
4. **Claridad**: La estructura refleja los módulos de negocio del sistema
5. **Independencia**: Cada feature puede evolucionar independientemente

---

## Modelo de Datos

### Modelo de Dominio

```java
// Modelo de dominio puro - sin anotaciones JPA
public class DocumentType {
    private Long id;
    private UUID uuid;
    private String code;          // Único, 2-10 caracteres, mayúsculas
    private String name;          // Requerido, 1-200 caracteres
    private String description;   // Opcional, máximo 500 caracteres
    private Boolean active;       // Predeterminado true
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // Métodos de negocio
    public void activate() { ... }
    public void deactivate() { ... }
    public boolean isActive() { ... }
    public boolean isDeleted() { ... }
    public void markAsDeleted(Long userId) { ... }
}
```

### Esquema de Base de Datos

```sql
CREATE TABLE document_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    INDEX idx_code (code),
    INDEX idx_uuid (uuid),
    INDEX idx_active (active),
    INDEX idx_deleted_at (deleted_at)
);
```

#### Estrategia de Índices
- **uuid**: Índice UNIQUE para búsqueda rápida por identificador externo
- **code**: Índice UNIQUE para validación y búsqueda rápida
- **active**: Índice para filtrado de tipos activos/inactivos
- **deleted_at**: Índice para consultas de eliminación lógica

#### Archivos de Migración
- **V1.3__create_document_types_tables.sql**: Creación de tabla
- **V1.4__insert_colombia_document_types.sql**: Datos iniciales (6 tipos colombianos)

---

## Especificación de API

### Ruta Base
```
/api/document-types
```

### Endpoints

#### 1. Crear Tipo de Documento
```http
POST /api/document-types
Content-Type: application/json
Authorization: Bearer {jwt_token}

Cuerpo de Solicitud:
{
  "code": "NIT",
  "name": "Número de Identificación Tributaria",
  "description": "Documento de identificación para empresas en Colombia"
}

Respuesta Exitosa (201 Created):
Location: /api/document-types/550e8400-e29b-41d4-a716-446655440000
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "NIT",
  "name": "Número de Identificación Tributaria",
  "description": "Documento de identificación para empresas en Colombia",
  "active": true,
  "createdAt": "2026-01-11T12:00:00Z",
  "updatedAt": "2026-01-11T12:00:00Z",
  "deletedAt": null,
  "createdBy": 1,
  "updatedBy": null,
  "deletedBy": null
}

Respuestas de Error:
400 Bad Request - Validación fallida
409 Conflict - El código ya existe
500 Internal Server Error
```

#### 2. Obtener Tipo de Documento por UUID
```http
GET /api/document-types/{uuid}
Authorization: Bearer {jwt_token}

Respuesta Exitosa (200 OK):
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "NIT",
  "name": "Número de Identificación Tributaria",
  "description": "...",
  "active": true,
  "createdAt": "2026-01-11T12:00:00Z",
  "updatedAt": "2026-01-11T12:00:00Z",
  "deletedAt": null,
  "createdBy": 1,
  "updatedBy": null,
  "deletedBy": null
}

Respuestas de Error:
404 Not Found - El UUID no existe
500 Internal Server Error
```

#### 3. Listar Tipos de Documento con Filtros
```http
GET /api/document-types?enabled=true&search=Cedula&page=0&limit=10&sort.field=name&sort.order=ASC
Authorization: Bearer {jwt_token}

Parámetros de Consulta:
- enabled: Boolean (opcional) - Filtrar por estado activo
- search: String (opcional) - Búsqueda global en código, nombre, descripción
- page: Integer (predeterminado: 0, mínimo: 0) - Número de página (basado en 0)
- limit: Integer (predeterminado: 10, mínimo: 1, máximo: 100) - Elementos por página
- fields: String[] (opcional) - Campos a incluir en la respuesta
- sort.field: String (predeterminado: id) - Campo por el que ordenar
- sort.order: String (predeterminado: ASC) - ASC o DESC
- populate: String[] (opcional) - Relaciones a poblar
- filters.*: Filtros dinámicos (ej., filters.country=CO)

Respuesta Exitosa (200 OK):
{
  "content": [
    {
      "id": 2,
      "uuid": "...",
      "code": "CC",
      "name": "Cédula de Ciudadanía",
      ...
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 10
}

Respuestas de Error:
400 Bad Request - Parámetros de consulta inválidos
500 Internal Server Error
```

#### 4. Actualizar Tipo de Documento
```http
PUT /api/document-types/{uuid}
Content-Type: application/json
Authorization: Bearer {jwt_token}

Cuerpo de Solicitud:
{
  "name": "Número de Identificación Tributaria Actualizado",
  "description": "Descripción actualizada"
}

Respuesta Exitosa (200 OK):
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "NIT",
  "name": "Número de Identificación Tributaria Actualizado",
  "description": "Descripción actualizada",
  "active": true,
  "createdAt": "2026-01-11T12:00:00Z",
  "updatedAt": "2026-01-11T14:30:00Z",
  "deletedAt": null,
  "createdBy": 1,
  "updatedBy": 1,
  "deletedBy": null
}

Respuestas de Error:
400 Bad Request - Validación fallida
404 Not Found - El UUID no existe
409 Conflict - El código ya existe (si se cambia el código)
500 Internal Server Error
```

#### 5. Desactivar Tipo de Documento
```http
PATCH /api/document-types/{uuid}/deactivate
Authorization: Bearer {jwt_token}

Respuesta Exitosa (200 OK):
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "NIT",
  "name": "Número de Identificación Tributaria",
  "active": false,
  ...
}

Respuestas de Error:
404 Not Found - El UUID no existe
422 Unprocessable Entity - Violación de regla de negocio
500 Internal Server Error
```

#### 6. Eliminar Tipo de Documento (Eliminación Lógica)
```http
DELETE /api/document-types/{uuid}
Authorization: Bearer {jwt_token}

Respuesta Exitosa (204 No Content):
(Sin cuerpo)

Respuestas de Error:
404 Not Found - El UUID no existe
422 Unprocessable Entity - No se puede eliminar (en uso)
500 Internal Server Error
```

---

## Manejo de Errores

### Códigos de Estado HTTP

| Código | Uso | Escenarios |
|--------|-----|------------|
| 200 | OK | Éxito en GET, PUT, PATCH |
| 201 | Created | Éxito en POST (incluye encabezado Location) |
| 204 | No Content | Éxito en DELETE |
| 400 | Bad Request | Errores de validación, parámetros inválidos |
| 404 | Not Found | Recurso no encontrado por UUID |
| 409 | Conflict | Código duplicado |
| 422 | Unprocessable Entity | Violación de regla de negocio |
| 500 | Internal Server Error | Errores inesperados |

### Formato de Respuesta de Error

```json
{
  "message": "Descripción clara del error para el usuario",
  "error": "CODIGO_ERROR"
}
```

**Códigos de Error:**
- `VALIDATION_ERROR`: Validación de bean fallida
- `RESOURCE_NOT_FOUND`: Recurso no encontrado
- `DUPLICATE_CODE`: El código ya existe
- `BUSINESS_RULE_VIOLATION`: Regla de negocio no satisfecha
- `INTERNAL_SERVER_ERROR`: Error inesperado

### Mapeo de Excepciones

| Excepción de Dominio | Código HTTP | Código de Error |
|---------------------|-------------|-----------------|
| DocumentTypeNotFoundException | 404 | RESOURCE_NOT_FOUND |
| DuplicateCodeException | 409 | DUPLICATE_CODE |
| DocumentTypeConstraintException | 422 | BUSINESS_RULE_VIOLATION |
| InvalidDocumentTypeException | 400 | VALIDATION_ERROR |
| MethodArgumentNotValidException | 400 | VALIDATION_ERROR |
| ConstraintViolationException | 400 | VALIDATION_ERROR |
| Exception (genérica) | 500 | INTERNAL_SERVER_ERROR |

---

## Restricciones Técnicas

### Base de Datos
- **RT-BD-001**: Se requiere MySQL 8.0+
- **RT-BD-002**: Las migraciones de Flyway son obligatorias para cambios de esquema
- **RT-BD-003**: Patrón de eliminación lógica (marca de tiempo deleted_at) obligatorio
- **RT-BD-004**: Todas las tablas deben tener columna uuid para referencias externas
- **RT-BD-005**: Campos de auditoría (createdBy, updatedBy, timestamps) obligatorios

### Diseño de API
- **RT-API-001**: Se deben seguir los principios RESTful
- **RT-API-002**: Formato JSON para todas las solicitudes/respuestas
- **RT-API-003**: UUID en URLs, no IDs de base de datos
- **RT-API-004**: Paginación predeterminada: page=0, limit=10, límite máximo=100
- **RT-API-005**: Códigos de estado HTTP estándar según mejores prácticas REST
- **RT-API-006**: Encabezado Location en respuestas 201 Created

### Seguridad
- **RT-SEG-001**: Autenticación JWT requerida para todos los endpoints
- **RT-SEG-002**: Rol ADMIN requerido para: POST, PUT, DELETE, PATCH
- **RT-SEG-003**: Acceso de LECTURA para usuarios autenticados
- **RT-SEG-004**: Sanitización de entrada vía Bean Validation

### Arquitectura
- **RT-ARQ-001**: Arquitectura hexagonal obligatoria
- **RT-ARQ-002**: Los modelos de dominio deben ser agnósticos al framework (sin JPA en dominio)
- **RT-ARQ-003**: MapStruct para todo el mapeo de objetos
- **RT-ARQ-004**: Patrón de caso de uso para capa de aplicación
- **RT-ARQ-005**: Patrón de repositorio vía puertos y adaptadores
- **RT-ARQ-006**: Separación clara: dominio/aplicación/infraestructura

### Calidad de Código
- **RT-CAL-001**: Cobertura de pruebas > 80% (capas de dominio y aplicación)
- **RT-CAL-002**: Sin problemas críticos de SonarLint
- **RT-CAL-003**: Complejidad ciclomática < 10 por método
- **RT-CAL-004**: Uso de Lombok para reducción de código repetitivo

---

## Requisitos de Rendimiento

### Objetivos de Tiempo de Respuesta (p95)
- **GET /api/document-types**: < 100ms
- **GET /api/document-types/{uuid}**: < 50ms
- **POST /api/document-types**: < 150ms
- **PUT /api/document-types/{uuid}**: < 150ms
- **DELETE /api/document-types/{uuid}**: < 100ms

### Estrategias de Optimización
1. **Índices de Base de Datos**: uuid, code, active, deleted_at
2. **Optimización de Consultas**: Usar JPA Specifications para filtrado dinámico
3. **Paginación**: Siempre paginar endpoints de listado
4. **Caché**: Considerar agregar caché para tipos frecuentemente accedidos (futuro)

---

## Dependencias

### Dependencias de Módulos
- **NINGUNA** - Este es un módulo de catálogo base sin dependencias en otros módulos de negocio

### External Dependencies

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
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-mysql</artifactId>
    </dependency>
    
    <!-- Mapping -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
        <scope>provided</scope>
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
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Módulos que Dependen de Este
- **Módulo de Contacto**: Referencia tipos de documento para identificación de cliente/proveedor
- **Módulo de Usuario**: Puede referenciar tipos de documento para perfiles de usuario
- **Módulo de Empresa**: Referencia tipos de documento (típicamente NIT) para identificación de empresa

---

## Estrategia de Pruebas

### Pruebas Unitarias (> 80% cobertura)
- **Capa de Dominio**:
  - Métodos de negocio de DocumentType
  - Lógica de validación de DocumentTypeDomainService
  - Pruebas de DocumentTypeValidator
  - Pruebas de lógica pura sin mocks

- **Capa de Aplicación**:
  - Todos los casos de uso con puertos simulados
  - Verificar lógica de orquestación
  - Manejo de excepciones

- **Mappers**:
  - Pruebas de mapeo bidireccional
  - Manejo de nulos
  - Casos extremos

### Pruebas de Integración
- **Capa de Repositorio**:
  - Testcontainers con MySQL
  - Operaciones CRUD
  - Métodos de consulta
  - Comportamiento de eliminación lógica
  - Filtrado dinámico con Specifications

- **Capa de API**:
  - Pruebas con MockMvc
  - Todos los endpoints con escenarios exitosos
  - Todos los escenarios de error con códigos HTTP apropiados
  - Validación de JSON Request/Response
  - Combinaciones de filtros
  - Validación de paginación

### Pruebas E2E
- Flujos completos: crear → obtener → actualizar → desactivar
- Combinaciones de filtros y búsqueda
- Verificación de eliminación lógica

---

## Consideraciones de Seguridad

### Autenticación
- Token JWT requerido en encabezado Authorization
- Validación de token en cada solicitud
- Información de usuario extraída de claims JWT

### Autorización
- **Rol ADMIN**: Acceso CRUD completo
- **Rol USER**: Acceso solo lectura
- Control de acceso basado en roles (RBAC)

### Validación de Entrada
- Bean Validation en todos los DTOs
- Prevención de inyección SQL vía JPA
- Prevención de XSS vía sanitización de entrada

### Pista de Auditoría
- Cada crear/actualizar/eliminar registra ID de usuario
- Marcas de tiempo para todas las operaciones
- La eliminación lógica preserva datos históricos

---

## Documentación Relacionada

- Especificación Funcional: [functional-spec.md](1-functional-spec.md)
- Plan de Implementación: [plan.md](3-plan.md)
- Seguimiento de Tareas: [tasks.json](4-tasks.json)
- Estado: [STATUS.md](status.md)
- Referencia de Scaffolding: [../../scaffolding.md](../../scaffolding.md)

