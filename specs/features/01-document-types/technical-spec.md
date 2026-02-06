# Technical Specification: Document Types Module

**Created:** January 10, 2026  
**Feature Number:** 01  
**Architecture:** Hexagonal (Ports & Adapters)

---

## Technical Overview

This module implements a document type catalog following hexagonal architecture principles with complete domain isolation and clear separation between business logic, application orchestration, and infrastructure concerns.

### Technology Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **ORM**: Spring Data JPA with Hibernate
- **Database**: MySQL 8.0+
- **Migrations**: Flyway
- **Mapping**: MapStruct 1.5.5
- **Validation**: Hibernate Validator (Bean Validation)
- **Testing**: JUnit 5, Mockito, Testcontainers
- **API Documentation**: SpringDoc OpenAPI 3

---

## Architecture

### Hexagonal Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                          │
│  (Pure business logic - no framework dependencies)          │
│                                                              │
│  • DocumentType (Aggregate Root)                            │
│  • DocumentTypeDomainService (Business Rules)               │
│  • Domain Exceptions                                         │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                         │
│  (Use Case orchestration - thin layer)                      │
│                                                              │
│  INPUT PORTS (Interfaces):                                  │
│  • CreateDocumentTypeUseCase                                │
│  • GetDocumentTypeUseCase                                   │
│  • UpdateDocumentTypeUseCase                                │
│  • DeactivateDocumentTypeUseCase                            │
│  • ListDocumentTypesUseCase                                 │
│                                                              │
│  OUTPUT PORTS (Interfaces):                                 │
│  • DocumentTypePort (Repository abstraction)                │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
┌─────────────────────────────────────────────────────────────┐
│                  INFRASTRUCTURE LAYER                        │
│  (Framework-specific implementations)                       │
│                                                              │
│  INPUT ADAPTERS:                                            │
│  • DocumentTypeController (REST API)                        │
│  • DTOs and Mappers                                         │
│                                                              │
│  OUTPUT ADAPTERS:                                           │
│  • DocumentTypeRepositoryAdapter (JPA implementation)       │
│  • DocumentTypeEntity (JPA entity)                          │
│  • DocumentTypeJpaRepository (Spring Data)                  │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.jcuadrado.erplitebackend/
│
├── domain/
│   └── documenttype/
│       ├── model/
│       │   └── DocumentType.java
│       ├── service/
│       │   └── DocumentTypeDomainService.java
│       └── exception/
│           ├── DocumentTypeNotFoundException.java
│           ├── DuplicateCodeException.java
│           └── DocumentTypeConstraintException.java
│
├── application/
│   └── port/
│       ├── in/
│       │   └── documenttype/
│       │       ├── CreateDocumentTypeUseCase.java
│       │       ├── GetDocumentTypeUseCase.java
│       │       ├── UpdateDocumentTypeUseCase.java
│       │       ├── DeactivateDocumentTypeUseCase.java
│       │       └── ListDocumentTypesUseCase.java
│       └── out/
│           └── DocumentTypePort.java
│
└── infrastructure/
    ├── out/
    │   └── documenttype/
    │       └── persistence/
    │           ├── entity/
    │           │   └── DocumentTypeEntity.java
    │           ├── repository/
    │           │   └── DocumentTypeJpaRepository.java
    │           ├── adapter/
    │           │   └── DocumentTypeRepositoryAdapter.java
    │           └── mapper/
    │               └── DocumentTypeEntityMapper.java
    │
    └── in/
        └── api/
            └── documenttype/
                ├── rest/
                │   └── DocumentTypeController.java
                ├── dto/
                │   ├── CreateDocumentTypeRequestDto.java
                │   ├── UpdateDocumentTypeRequestDto.java
                │   ├── DocumentTypeResponseDto.java
                │   ├── DocumentTypeFilterDto.java
                │   ├── SortDto.java
                │   └── PagedResponseDto.java
                └── mapper/
                    └── DocumentTypeDtoMapper.java
```

---

## Data Model

### Domain Model

```java
// Pure domain model - no JPA annotations
public class DocumentType {
    private Long id;
    private UUID uuid;
    private String code;          // Unique, 2-10 chars, uppercase
    private String name;          // Required, 1-200 chars
    private String description;   // Optional, max 500 chars
    private Boolean active;       // Default true
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // Business methods
    public void activate() { ... }
    public void deactivate() { ... }
    public boolean isActive() { ... }
    public boolean isDeleted() { ... }
}
```

### Database Schema

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

#### Indexes Strategy
- **uuid**: UNIQUE index for fast lookup by external identifier
- **code**: UNIQUE index for validation and fast search
- **active**: Index for filtering active/inactive types
- **deleted_at**: Index for soft delete queries

#### Migration Files
- **V1.3__create_document_types_tables.sql**: Table creation
- **V1.4__insert_colombia_document_types.sql**: Initial data (6 Colombian types)

---

## API Specification

### Base Path
```
/api/document-types
```

### Endpoints

#### 1. Create Document Type
```http
POST /api/document-types
Content-Type: application/json
Authorization: Bearer {jwt_token}

Request Body:
{
  "code": "NIT",
  "name": "Número de Identificación Tributaria",
  "description": "Documento de identificación para empresas en Colombia"
}

Success Response (201 Created):
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

Error Responses:
400 Bad Request - Validation failed
409 Conflict - Code already exists
500 Internal Server Error
```

#### 2. Get Document Type by UUID
```http
GET /api/document-types/{uuid}
Authorization: Bearer {jwt_token}

Success Response (200 OK):
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

Error Responses:
404 Not Found - UUID does not exist
500 Internal Server Error
```

#### 3. List Document Types with Filters
```http
GET /api/document-types?enabled=true&search=Cedula&page=0&limit=10&sort.field=name&sort.order=ASC
Authorization: Bearer {jwt_token}

Query Parameters:
- enabled: Boolean (optional) - Filter by active status
- search: String (optional) - Global search in code, name, description
- page: Integer (default: 0, min: 0) - Page number (0-based)
- limit: Integer (default: 10, min: 1, max: 100) - Items per page
- fields: String[] (optional) - Fields to include in response
- sort.field: String (default: id) - Field to sort by
- sort.order: String (default: ASC) - ASC or DESC
- populate: String[] (optional) - Relationships to populate
- filters.*: Dynamic filters (e.g., filters.country=CO)

Success Response (200 OK):
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

Error Responses:
400 Bad Request - Invalid query parameters
500 Internal Server Error
```

#### 4. Update Document Type
```http
PUT /api/document-types/{uuid}
Content-Type: application/json
Authorization: Bearer {jwt_token}

Request Body:
{
  "name": "Número de Identificación Tributaria Actualizado",
  "description": "Descripción actualizada"
}

Success Response (200 OK):
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

Error Responses:
400 Bad Request - Validation failed
404 Not Found - UUID does not exist
409 Conflict - Code already exists (if changing code)
500 Internal Server Error
```

#### 5. Deactivate Document Type
```http
PATCH /api/document-types/{uuid}/deactivate
Authorization: Bearer {jwt_token}

Success Response (200 OK):
{
  "id": 1,
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "NIT",
  "name": "Número de Identificación Tributaria",
  "active": false,
  ...
}

Error Responses:
404 Not Found - UUID does not exist
422 Unprocessable Entity - Business rule violation
500 Internal Server Error
```

#### 6. Delete Document Type (Soft Delete)
```http
DELETE /api/document-types/{uuid}
Authorization: Bearer {jwt_token}

Success Response (204 No Content):
(No body)

Error Responses:
404 Not Found - UUID does not exist
422 Unprocessable Entity - Cannot delete (in use)
500 Internal Server Error
```

---

## Error Handling

### HTTP Status Codes

| Code | Usage | Scenarios |
|------|-------|-----------|
| 200 | OK | GET, PUT, PATCH success |
| 201 | Created | POST success (includes Location header) |
| 204 | No Content | DELETE success |
| 400 | Bad Request | Validation errors, invalid parameters |
| 404 | Not Found | Resource not found by UUID |
| 409 | Conflict | Duplicate code |
| 422 | Unprocessable Entity | Business rule violation |
| 500 | Internal Server Error | Unexpected errors |

### Error Response Format

Following standard defined in `framework/STANDARD_ERROR_FORMAT.md`:

```json
{
  "message": "Clear error description for the user",
  "error": "ERROR_CODE"
}
```

**Error Codes:**
- `VALIDATION_ERROR`: Bean validation failed
- `RESOURCE_NOT_FOUND`: Resource not found
- `DUPLICATE_CODE`: Code already exists
- `BUSINESS_RULE_VIOLATION`: Business rule not satisfied
- `INTERNAL_SERVER_ERROR`: Unexpected error

### Exception Mapping

| Domain Exception | HTTP Code | Error Code |
|-----------------|-----------|------------|
| DocumentTypeNotFoundException | 404 | RESOURCE_NOT_FOUND |
| DuplicateCodeException | 409 | DUPLICATE_CODE |
| DocumentTypeConstraintException | 422 | BUSINESS_RULE_VIOLATION |
| MethodArgumentNotValidException | 400 | VALIDATION_ERROR |
| ConstraintViolationException | 400 | VALIDATION_ERROR |
| Exception (generic) | 500 | INTERNAL_SERVER_ERROR |

---

## Technical Constraints

### Database
- **TC-DB-001**: MySQL 8.0+ required
- **TC-DB-002**: Flyway migrations mandatory for schema changes
- **TC-DB-003**: Soft delete pattern (deleted_at timestamp) mandatory
- **TC-DB-004**: All tables must have uuid column for external references
- **TC-DB-005**: Audit fields (createdBy, updatedBy, timestamps) mandatory

### API Design
- **TC-API-001**: RESTful principles must be followed
- **TC-API-002**: JSON format for all requests/responses
- **TC-API-003**: UUID in URLs, not database IDs
- **TC-API-004**: Pagination default: page=0, limit=10, max limit=100
- **TC-API-005**: Standard HTTP status codes as per REST best practices
- **TC-API-006**: Location header in 201 Created responses

### Security
- **TC-SEC-001**: JWT authentication required for all endpoints
- **TC-SEC-002**: ADMIN role required for: POST, PUT, DELETE, PATCH
- **TC-SEC-003**: READ access for authenticated users
- **TC-SEC-004**: Input sanitization via Bean Validation

### Architecture
- **TC-ARCH-001**: Hexagonal architecture mandatory
- **TC-ARCH-002**: Domain models must be framework-agnostic (no JPA in domain)
- **TC-ARCH-003**: MapStruct for all object mapping
- **TC-ARCH-004**: Use Case pattern for application layer
- **TC-ARCH-005**: Repository pattern via ports and adapters

### Code Quality
- **TC-QUAL-001**: Test coverage > 80% (domain and application layers)
- **TC-QUAL-002**: No critical SonarLint issues
- **TC-QUAL-003**: Cyclomatic complexity < 10 per method
- **TC-QUAL-004**: Lombok usage for boilerplate reduction

---

## Performance Requirements

### Response Time Targets (p95)
- **GET /api/document-types**: < 100ms
- **GET /api/document-types/{uuid}**: < 50ms
- **POST /api/document-types**: < 150ms
- **PUT /api/document-types/{uuid}**: < 150ms
- **DELETE /api/document-types/{uuid}**: < 100ms

### Optimization Strategies
1. **Database Indexes**: uuid, code, active, deleted_at
2. **Query Optimization**: Use JPA Specifications for dynamic filtering
3. **Pagination**: Always paginate list endpoints
4. **Caching**: Consider adding cache for frequently accessed types (future)

---

## Dependencies

### Module Dependencies
- **NONE** - This is a base catalog module with no dependencies on other business modules

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

### Modules that Depend on This
- **Contact Module**: References document types for customer/supplier identification
- **User Module**: May reference document types for user profiles
- **Company Module**: References document types (typically NIT) for company identification

---

## Testing Strategy

### Unit Tests (> 80% coverage)
- **Domain Layer**:
  - DocumentType business methods
  - DocumentTypeDomainService validation logic
  - Pure logic tests without mocks

- **Application Layer**:
  - All use cases with mocked ports
  - Verify orchestration logic
  - Exception handling

- **Mappers**:
  - Bidirectional mapping tests
  - Null handling
  - Edge cases

### Integration Tests
- **Repository Layer**:
  - Testcontainers with MySQL
  - CRUD operations
  - Query methods
  - Soft delete behavior
  - Dynamic filtering with Specifications

- **API Layer**:
  - MockMvc tests
  - All endpoints with success scenarios
  - All error scenarios with appropriate HTTP codes
  - Request/Response JSON validation
  - Filter combinations
  - Pagination validation

### E2E Tests
- Complete flows: create → get → update → deactivate
- Filter and search combinations
- Soft delete verification

---

## Security Considerations

### Authentication
- JWT token required in Authorization header
- Token validation on every request
- User information extracted from JWT claims

### Authorization
- **ADMIN role**: Full CRUD access
- **USER role**: Read-only access
- Role-based access control (RBAC)

### Input Validation
- Bean Validation on all DTOs
- SQL injection prevention via JPA
- XSS prevention via input sanitization

### Audit Trail
- Every create/update/delete records user ID
- Timestamps for all operations
- Soft delete preserves historical data

---

## Related Documentation

- Functional Specification: [functional-spec.md](functional-spec.md)
- Implementation Plan: [plan.md](plan.md)
- Implementation Summary: [IMPLEMENTED.md](IMPLEMENTED.md)
- Error Format Standard: [../../framework/STANDARD_ERROR_FORMAT.md](../../framework/STANDARD_ERROR_FORMAT.md)
