# ERP Lite Backend - Project Information

**Última actualización**: 2026-02-01  
**Versión**: 1.0.0-SNAPSHOT  
**Estado**: En desarrollo activo

---

## 📋 Información General

- **Nombre**: ERP Lite Backend
- **Descripción**: Sistema ERP ligero para pequeñas y medianas empresas
- **Stack Tecnológico**: 
  - Java 21
  - Spring Boot 3.x
  - Spring Data JPA
  - MySQL 8.0
  - Flyway (Migrations)
  - MapStruct (Object Mapping)
  - Lombok
  - JUnit 5 + Mockito + Testcontainers
- **Arquitectura**: Hexagonal (Ports & Adapters)
- **Patrón de Diseño**: DDD (Domain-Driven Design)

---

## 📊 Features Overview

### ✅ Implemented Features

#### 01. Document Types (Tipos de Documento)
- **Estado**: ✅ Completado
- **Fecha implementación**: 2026-01-15
- **Descripción**: Catálogo de tipos de documentos de identificación (NIT, CC, CE, Pasaporte, etc.)
- **Endpoints**: 7 REST APIs (CRUD + listar con filtros avanzados)
- **Tests Coverage**: 85%+
- **Dependencias**: Ninguna (módulo independiente)
- **Usado por**: Geography (futuro), Security (futuro), Contact (futuro)
- **Documentación**: [features/01-document-types/](features/01-document-types/)

---

### 🟡 En Desarrollo (Work In Progress)

_Ningún feature actualmente en desarrollo activo_

---

### 📋 Planeados (Backlog)

#### Orden de Implementación Recomendado

**Fase 1 - Catálogos Base (P1 - Crítica)**

**02. Geography** (Departamentos y Municipios)
- **Prioridad**: 🔴 Alta
- **Descripción**: Catálogo de geografía administrativa de Colombia
- **Dependencias**: Ninguna
- **Estimación**: 2 semanas
- **Documentación**: [geography/](geography/)

**03. Tax Types** (Tipos de Impuestos)
- **Prioridad**: 🔴 Alta
- **Descripción**: Catálogo de tipos de impuestos (IVA, ReteFuente, etc.)
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Documentación**: [tax-types/](tax-types/)
- **Nota**: Sin spec detallado aún

**04. Payment Methods** (Métodos de Pago)
- **Prioridad**: 🟡 Media
- **Descripción**: Catálogo de métodos de pago (efectivo, tarjeta, transferencia, etc.)
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Documentación**: [payment-methods/](payment-methods/)
- **Nota**: Sin spec detallado aún

**05. Units of Measure** (Unidades de Medida)
- **Prioridad**: 🟡 Media
- **Descripción**: Catálogo de unidades de medida (kg, m, l, etc.)
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Documentación**: [units-of-measure/](units-of-measure/)
- **Nota**: Sin spec detallado aún

**Fase 2 - Seguridad y Estructura (P1 - Crítica)**

**06. Security** (Autenticación y Autorización)
- **Prioridad**: 🔴 Crítica
- **Descripción**: Módulo de usuarios, roles, permisos y autenticación JWT
- **Dependencias**: Document Types (01)
- **Estimación**: 4 semanas
- **Documentación**: [security/](security/)
- **BLOQUEADOR**: Sin este módulo no hay control de acceso

**07. Product Categories** (Categorías de Productos)
- **Prioridad**: 🟡 Media
- **Descripción**: Catálogo de categorías de productos con jerarquía
- **Dependencias**: Ninguna
- **Estimación**: 1 semana
- **Nota**: Incluido en inventory-spec.md

**Fase 3 - Infraestructura de Inventario (P1)**

**08. Warehouses** (Bodegas)
- **Prioridad**: 🔴 Alta
- **Descripción**: Gestión de bodegas con restricciones de acceso
- **Dependencias**: Security (06), Geography (02)
- **Estimación**: 2 semanas

**09. Products** (Productos)
- **Prioridad**: 🔴 Alta
- **Descripción**: Gestión completa de productos con múltiples códigos de barras
- **Dependencias**: Product Categories (07), Units of Measure (05), Tax Types (03)
- **Estimación**: 3 semanas
- **Documentación**: [inventory/](inventory/)

**Fase 4 - Gestión de Inventario (P1)**

**10. Stock Management** (Gestión de Stock)
- **Prioridad**: 🔴 Alta
- **Descripción**: Control de inventario por bodega
- **Dependencias**: Products (09), Warehouses (08)
- **Estimación**: 3 semanas
- **Nota**: Incluido en inventory-spec.md

**Fase 5+ - Módulos de Negocio (P2-P3)**
- Contacts (Clientes y Proveedores)
- Company (Configuración de Empresa)
- Sales (Ventas)
- Purchases (Compras)
- Accounting (Contabilidad)

---

## 🏗️ Arquitectura General

### Principios Arquitectónicos

#### Hexagonal Architecture (Ports & Adapters)
```
┌─────────────────────────────────────────────┐
│          Infrastructure Layer               │
│  (Controllers, Repositories, External APIs) │
└──────────────────┬──────────────────────────┘
                   │ Adapters
┌──────────────────▼──────────────────────────┐
│          Application Layer                  │
│  (Use Cases, Application Services)          │
└──────────────────┬──────────────────────────┘
                   │ Ports
┌──────────────────▼──────────────────────────┐
│            Domain Layer                     │
│  (Entities, Value Objects, Domain Services) │
└─────────────────────────────────────────────┘
```

#### Capas del Sistema

1. **Domain Layer**: 
   - Entidades puras sin dependencias de frameworks
   - Lógica de negocio core
   - Domain Services para reglas complejas
   - Excepciones de dominio

2. **Application Layer**: 
   - Use Cases / Application Services
   - Orquestación de operaciones
   - DTOs de entrada/salida
   - Ports (interfaces)

3. **Infrastructure Layer**:
   - Controllers REST
   - Entities JPA
   - Repositories
   - Mappers (MapStruct)
   - Configuración

---

## 🗄️ Base de Datos

### Información General
- **Motor**: MySQL 8.0
- **Migrations**: Flyway
- **Naming Convention**: snake_case
- **Charset**: utf8mb4
- **Collation**: utf8mb4_unicode_ci

### Tablas Actuales
1. **document_types** - Tipos de documento (6 registros seed)

### Convenciones
- Primary Key: `id` (BIGINT AUTO_INCREMENT)
- UUID: `uuid` (VARCHAR(36), UNIQUE) para referencias externas
- Soft Delete: `deleted_at`, `deleted_by`
- Auditoría: `created_at`, `created_by`, `updated_at`, `updated_by`
- Estado: `active` (BOOLEAN DEFAULT TRUE)

---

## 🎨 Estándares de Código

### Naming Conventions
- **Clases**: PascalCase (`DocumentType`)
- **Métodos**: camelCase (`findById`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_LIMIT`)
- **Packages**: lowercase (`com.jcuadrado.erplitebackend.domain.documenttype`)

### Package Structure
```
com.jcuadrado.erplitebackend/
├── domain/
│   └── {module}/
│       ├── model/
│       ├── service/
│       └── exception/
├── application/
│   └── service/
│       └── {module}/
└── infrastructure/
    ├── in/
    │   └── api/
    │       └── {module}/
    │           ├── rest/
    │           ├── dto/
    │           ├── mapper/
    │           └── constant/
    └── out/
        └── persistence/
            └── {module}/
                ├── entity/
                ├── repository/
                └── mapper/
```

### Formato de Errores

Todas las respuestas de error en el sistema DEBEN usar exclusivamente este formato con **dos campos obligatorios**:

```json
{
  "message": "Descripción clara del error para el usuario",
  "error": "CODIGO_ERROR"
}
```

#### Códigos de Error Estándar

| Código | HTTP Status | Descripción | Uso |
|--------|-------------|-------------|-----|
| `VALIDATION_ERROR` | 400 | Errores de validación de datos | Bean Validation, parámetros inválidos, JSON malformado |
| `AUTHENTICATION_FAILED` | 401 | Autenticación fallida | Token inválido, credenciales incorrectas, token expirado |
| `INSUFFICIENT_PERMISSIONS` | 403 | Permisos insuficientes | Usuario sin permisos para la operación |
| `RESOURCE_NOT_FOUND` | 404 | Recurso no encontrado | GET/PUT/DELETE de recurso inexistente |
| `DUPLICATE_CODE` | 409 | Código duplicado | Violación de constraint UNIQUE |
| `BUSINESS_RULE_VIOLATION` | 422 | Regla de negocio no satisfecha | Restricciones del dominio, operación no permitida |
| `INTERNAL_SERVER_ERROR` | 500 | Error inesperado | Excepciones no controladas, errores de infraestructura |

#### Ejemplos de Respuestas de Error

**400 Bad Request - VALIDATION_ERROR**
```json
{
  "message": "Code must be between 2 and 10 characters; Name is required and cannot be blank",
  "error": "VALIDATION_ERROR"
}
```

**404 Not Found - RESOURCE_NOT_FOUND**
```json
{
  "message": "Resource not found with uuid: 123e4567-e89b-12d3-a456-426614174000",
  "error": "RESOURCE_NOT_FOUND"
}
```

**409 Conflict - DUPLICATE_CODE**
```json
{
  "message": "Resource with code 'CODE-001' already exists",
  "error": "DUPLICATE_CODE"
}
```

**422 Unprocessable Entity - BUSINESS_RULE_VIOLATION**
```json
{
  "message": "Cannot delete resource that is currently in use by 150 related entities",
  "error": "BUSINESS_RULE_VIOLATION"
}
```

#### Implementación

**DTO de Error**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    private String message;
    private String error;
}
```

**GlobalExceptionHandler Base**
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        return ErrorResponseDto.builder()
                .message(message)
                .error("VALIDATION_ERROR")
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleResourceNotFound(ResourceNotFoundException ex) {
        return ErrorResponseDto.builder()
                .message(ex.getMessage())
                .error("RESOURCE_NOT_FOUND")
                .build();
    }

    @ExceptionHandler(DuplicateCodeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDuplicateCode(DuplicateCodeException ex) {
        return ErrorResponseDto.builder()
                .message(ex.getMessage())
                .error("DUPLICATE_CODE")
                .build();
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponseDto handleBusinessRule(BusinessRuleException ex) {
        return ErrorResponseDto.builder()
                .message(ex.getMessage())
                .error("BUSINESS_RULE_VIOLATION")
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ErrorResponseDto.builder()
                .message("An unexpected error occurred. Please try again later or contact support")
                .error("INTERNAL_SERVER_ERROR")
                .build();
    }
}
```

**⚠️ Reglas Importantes:**
- Los mensajes de error DEBEN ser claros y accionables
- Múltiples errores de validación se concatenan con "; "
- Las respuestas 500 NO DEBEN exponer información sensible (stack traces, queries SQL, rutas de archivos)
- Logging: WARN para 4xx, ERROR para 5xx

---

## 📊 Métricas del Proyecto

### Resumen General
- **Total Features Planeados**: 7
- **Features Completados**: 1 (14%)
- **Features En Desarrollo**: 0
- **Test Coverage Global**: 85%
- **Technical Debt**: Bajo

### Por Módulo
| Módulo | Estado | Tests | LOC | Complejidad |
|--------|--------|-------|-----|-------------|
| Document Types | ✅ | 85% | ~2,500 | Baja |
| Geography | 📋 | - | - | Media |
| Security | 📋 | - | - | Alta |
| Inventory | 📋 | - | - | Alta |

---

## 🎯 Roadmap 2026

### Q1 (Enero - Marzo)
- [x] ✅ Document Types
- [ ] 🎯 Geography Module (Feb)
- [ ] 🎯 Security Module (Mar)

### Q2 (Abril - Junio)
- [ ] Payment Methods
- [ ] Tax Types  
- [ ] Units of Measure

### Q3 (Julio - Septiembre)
- [ ] Inventory Module
- [ ] Contacts Module

### Q4 (Octubre - Diciembre)
- [ ] Sales Module
- [ ] Reports Module

---

## 👥 Team

### Roles
- **Tech Lead**: Por definir
- **Backend Developers**: Por definir
- **QA Engineer**: Por definir
- **DevOps**: Por definir

---

## 🔗 Enlaces Importantes

### Documentación
- [README Principal](../README.md) - Documentación general del proyecto
- [Framework SDD](framework/proyecto-framework-sdd.md) - Guía del framework de documentación
- [Standard Error Format](framework/STANDARD_ERROR_FORMAT.md) - Formato estándar de errores

### Features Implementados
- [Document Types](document-types/) - Catálogo de tipos de documento

### Features Planeados
- [Geography](geography/) - Departamentos y municipios
- [Security](security/) - Autenticación y autorización
- [Inventory](inventory/) - Gestión de inventario
- [Payment Methods](payment-methods/) - Métodos de pago
- [Tax Types](tax-types/) - Tipos de impuestos
- [Units of Measure](units-of-measure/) - Unidades de medida

---

## 🚀 Getting Started

### Prerequisitos
- Java 21+
- Maven 3.8+
- MySQL 8.0+
- IDE con soporte para Lombok y MapStruct

### Setup Local
```bash
# 1. Clonar repositorio
git clone <repository-url>
cd erp-lite-backend

# 2. Configurar base de datos
mysql -u root -p < scripts/setup-db.sql

# 3. Configurar application.properties
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Editar credenciales de BD

# 4. Ejecutar migraciones
mvn flyway:migrate

# 5. Compilar y ejecutar tests
mvn clean test

# 6. Ejecutar aplicación
mvn spring-boot:run
```

### Endpoints Disponibles
- **API Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

---

## 📝 Notas Importantes

### Decisiones Técnicas Clave

#### 2026-01-15 - MapStruct para Mappers
**Decisión**: Usar MapStruct en lugar de manual mapping  
**Razón**: Mejor rendimiento, menos código boilerplate, type-safe en compile-time  
**Impacto**: Todos los módulos deben usar MapStruct

#### 2026-01-10 - Soft Delete por Defecto
**Decisión**: Implementar soft delete en todos los catálogos base  
**Razón**: Trazabilidad y recuperación de datos  
**Impacto**: Todas las entidades tienen `deleted_at` y `deleted_by`

#### 2026-01-08 - UUID para Referencias Externas
**Decisión**: Usar UUID además de ID numérico  
**Razón**: Seguridad (no exponer IDs secuenciales) y flexibilidad para integraciones  
**Impacto**: Todas las entidades tienen campo `uuid` (VARCHAR(36))

---

## 🐛 Issues Conocidos

_No hay issues críticos actualmente_

---

## 📞 Soporte y Contacto

Para preguntas sobre el proyecto:
- **Documentación**: Ver [proyecto-framework-sdd.md](proyecto-framework-sdd.md)
- **Issues**: Crear issue en el repositorio
- **Slack**: Canal #erp-lite-backend (si aplica)

---

**Última revisión**: 2026-02-01  
**Próxima revisión**: 2026-02-15
