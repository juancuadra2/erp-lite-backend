# ERP Lite Backend

Sistema ERP ligero para pequeñas y medianas empresas, desarrollado con arquitectura hexagonal y principios de Domain-Driven Design.

## 📋 Descripción

ERP Lite es un sistema empresarial modular que proporciona gestión integral de operaciones de negocio, incluyendo seguridad, inventarios, ventas, compras y contabilidad. Diseñado para empresas con hasta 100 usuarios concurrentes, implementa las mejores prácticas de desarrollo y arquitectura limpia.

## 🏗️ Arquitectura

### Patrón Hexagonal (Ports & Adapters)

El proyecto sigue una arquitectura hexagonal con separación clara de responsabilidades:

```
src/main/java/com/jcuadrado/erplitebackend/
├── application/          # Capa de Aplicación (Use Cases & Ports)
│   ├── port/
│   │   ├── in/          # Input Ports (interfaces de casos de uso)
│   │   └── out/         # Output Ports (interfaces de repositorio)
│   └── service/         # Implementación de casos de uso
│
├── domain/              # Capa de Dominio (Lógica de Negocio)
│   ├── shared/          # Elementos compartidos (Value Objects, Eventos)
│   └── [module]/        # Módulos de dominio (model, service, util)
│
└── infrastructure/      # Capa de Infraestructura (Detalles Técnicos)
    ├── config/          # Configuración Spring
    ├── out/             # Adaptadores de salida (persistencia)
    │   └── [module]/
    │       ├── persistence/ # Entidades JPA y repositorios
    │       ├── mapper/      # Mapeo entidad-dominio
    │       └── adapter/     # Implementación de ports
    └── in/              # Adaptadores de entrada (REST API)
        └── api/
            └── [module]/
                ├── rest/     # Controllers
                ├── dto/      # DTOs de API
                └── mapper/   # Mapeo DTO-dominio
```

### Flujo de Dependencias

- **Domain**: 100% independiente, sin dependencias externas
- **Application**: Depende solo de Domain, define puertos (interfaces)
- **Infrastructure**: Implementa los puertos, depende de Application y Domain

## 🛠️ Stack Tecnológico

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| **Lenguaje** | Java | 21 |
| **Framework** | Spring Boot | 3.x |
| **Persistencia** | Spring Data JPA | 3.x |
| **Base de Datos** | MySQL | 8.0+ |
| **Migraciones** | Flyway | Latest |
| **Seguridad** | Spring Security + JWT | 6.x |
| **Mapeo** | MapStruct | 1.5+ |
| **Utilidades** | Lombok | Latest |
| **Documentación** | SpringDoc OpenAPI | 2.x |
| **Testing** | JUnit 5, Mockito, Testcontainers | Latest |

## 🚀 Inicio Rápido

### Prerrequisitos

- JDK 21+
- Maven 3.8+
- MySQL 8.0+

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd erp-lite-backend
   ```

2. **Configurar base de datos**
   ```sql
   CREATE DATABASE erp_lite;
   CREATE USER 'erp_user'@'localhost' IDENTIFIED BY 'password';
   GRANT ALL PRIVILEGES ON erp_lite.* TO 'erp_user'@'localhost';
   ```

3. **Configurar application.yml**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/erp_lite
       username: erp_user
       password: password
   ```

4. **Ejecutar migraciones**
   ```bash
   mvn flyway:migrate
   ```

5. **Compilar y ejecutar**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

6. **Acceder a Swagger UI**
   ```
   http://localhost:8080/swagger-ui.html
   ```

## 📦 Módulos del Sistema

### ✅ Implementados

- **Document Types** - Catálogo de tipos de documento de identificación

### 🚧 En Desarrollo

Ver carpeta [specs/wip/](specs/wip/) para features en desarrollo activo.

### 📋 Planeados

- **Security** - Autenticación JWT, usuarios, roles y permisos granulares
- **Geography** - Gestión de departamentos y municipios
- **Tax Types** - Tipos de impuestos configurables
- **Payment Methods** - Métodos de pago
- **Units of Measure** - Unidades de medida y conversiones
- **Company** - Configuración de empresa y bodegas
- **Products & Inventory** - Gestión de productos y control de stock multi-bodega
- **Contacts** - Clientes y proveedores (base unificada)
- **Sales** - Proceso completo de ventas con facturación DIAN
- **Purchases** - Órdenes de compra y recepciones

## 📐 Principios de Diseño

### Arquitectura

- **Hexagonal Architecture**: Separación clara entre dominio, aplicación e infraestructura
- **Domain-Driven Design**: Modelado rico del dominio con lógica de negocio encapsulada
- **SOLID Principles**: Código mantenible y extensible
- **Clean Code**: Legibilidad y simplicidad

### Implementación

- **Soft Delete**: Todas las entidades usan borrado lógico (`deleted_at`)
- **Optimistic Locking**: Control de concurrencia con `@Version`
- **Fail-Fast Validation**: Validación temprana con Bean Validation
- **Immutability**: Uso de objetos inmutables donde sea posible
- **Auditoría**: Registro completo de operaciones críticas

## 🔒 Seguridad

- **Autenticación JWT**: Access tokens (15 min) + Refresh tokens (7 días)
- **Permisos Granulares**: Control a nivel de entidad y operación (CRUD + Approve)
- **Políticas de Contraseña**: Complejidad configurable, historial, BCrypt
- **Auditoría**: Registro de todas las operaciones críticas
- **Bloqueo de Cuentas**: Protección contra fuerza bruta

## 📊 API REST

### Estándares HTTP

- **200 OK**: Consultas y actualizaciones exitosas
- **201 Created**: Recursos creados (con header `Location`)
- **204 No Content**: Eliminaciones exitosas
- **400 Bad Request**: Errores de validación
- **401 Unauthorized**: Autenticación fallida
- **403 Forbidden**: Permisos insuficientes
- **404 Not Found**: Recurso no encontrado
- **409 Conflict**: Conflictos de unicidad
- **422 Unprocessable Entity**: Violación de reglas de negocio
- **500 Internal Server Error**: Errores inesperados

### Formato de Errores

```json
{
  "message": "Descripción clara del error",
  "error": "CODIGO_ERROR"
}
```

Ver [specs/framework/STANDARD_ERROR_FORMAT.md](specs/framework/STANDARD_ERROR_FORMAT.md) para detalles completos.

## 🧪 Testing

### Estrategia de Testing

- **Tests Unitarios**: Lógica de dominio y casos de uso
- **Tests de Integración**: Adaptadores de persistencia y API
- **Tests de Contrato**: Validación de contratos de API
- **Tests End-to-End**: Flujos completos de negocio

### Ejecución

```bash
# Todos los tests
mvn test

# Tests de integración con Testcontainers
mvn verify

# Coverage report
mvn jacoco:report
```

**Coverage Objetivo**: Mínimo 90%

## 📚 Documentación

### Estructura de Documentación

```
specs/
├── framework/                   # Fundamentos del framework
│   ├── proyecto-framework-sdd.md
│   ├── STANDARD_ERROR_FORMAT.md
│   └── templates/
├── PROJECT_INFO.md              # Información del proyecto
├── STATUS.md                    # Estado general de features
├── features/                    # Features implementados
│   └── [XX-feature-name]/
│       ├── functional-spec.md
│       ├── technical-spec.md
│       ├── plan.md
│       ├── tasks.json
│       └── IMPLEMENTED.md
└── wip/                         # Features en desarrollo
    └── [feature-name]/
        ├── functional-spec.md
        ├── technical-spec.md
        ├── plan.md
        ├── tasks.json
        └── STATUS.md
```

### Documentos Clave

- **[Framework SDD](specs/framework/proyecto-framework-sdd.md)**: Sistema de documentación del proyecto
- **[Estado General](specs/STATUS.md)**: Dashboard de progreso de todos los features
- **[Información del Proyecto](specs/PROJECT_INFO.md)**: Detalles técnicos y arquitectura
- **[Formato de Errores](specs/framework/STANDARD_ERROR_FORMAT.md)**: Estándar de respuestas de error

## 🔄 Workflow de Desarrollo

### Nuevo Feature

1. Crear especificaciones en `specs/wip/[feature-name]/`
2. Implementar siguiendo arquitectura hexagonal
3. Crear tests con coverage > 80%
4. Actualizar `STATUS.md` continuamente
5. Al completar, mover a `specs/features/[XX-feature-name]/`

### Convenciones

- **Commits**: Mensajes descriptivos en español
- **Branches**: `feature/[name]`, `bugfix/[name]`, `hotfix/[name]`
- **Pull Requests**: Requieren revisión y tests pasando
- **Code Style**: Seguir convenciones de Spring Boot

## 🎯 Objetivos de Rendimiento

- **Usuarios Concurrentes**: 50+
- **Tiempo de Respuesta**: < 200ms (p95)
- **Disponibilidad**: 99.5%
- **Coverage de Tests**: > 90%

## 📈 Estado del Proyecto

**Versión Actual**: 1.0.0-SNAPSHOT  
**Sprint Actual**: Sprint 5 (2026-01-29 → 2026-02-11)  
**Progreso General**: 10% (1 de 10 features completados)

Ver [specs/STATUS.md](specs/STATUS.md) para detalles actualizados.

## 📞 Soporte

Para reportar bugs o solicitar features, crear un issue en el repositorio.

## 📄 Licencia

[Definir licencia]

---

**Última actualización**: Febrero 1, 2026
