# Functional Specification: Document Types Module

**Created:** January 10, 2026  
**Module Type:** Independent Catalog (No dependencies)  
**Feature Number:** 01

---

## Overview

Module to manage identification document types (NIT, CC, CE, Passport, etc.) used to identify natural and legal persons in the system. Acts as an independent base catalog that provides document type information to other modules.

### Business Objectives
- Maintain standardized catalog of identification document types
- Support Colombian document types initially
- Enable validation and proper identification of contacts, users, and companies
- Provide extensible foundation for additional countries

### Target Users
- System administrators managing document type catalog
- Other system modules consuming document type data (Contact, User, Company)

---

## User Stories & Acceptance Scenarios

### User Story 1 - Document Type Management (Priority: P1)

**As** a system administrator  
**I need** to manage accepted identification document types in the system  
**So that** I can correctly validate contact and user information

#### Acceptance Scenarios

**Scenario 1.1: Create document type with validations**
- **Given** I am authenticated as administrator
- **When** I send POST /api/document-types with:
  ```json
  {
    "code": "NIT",
    "name": "Número de Identificación Tributaria",
    "description": "Documento de identificación para empresas en Colombia"
  }
  ```
- **Then** I receive **201 Created** with the created type
- **And** response includes: id, uuid, code, name, description, active=true, timestamps

**Scenario 1.2: Unique code validation**
- **Given** a document type with code "NIT" exists
- **When** I try to create another with code "NIT"
- **Then** I receive **409 Conflict** with error message

**Scenario 1.3: Input data validation**
- **Given** I am authenticated as administrator
- **When** I send POST with invalid data (code too short, empty name, description too long)
- **Then** I receive **400 Bad Request** with validation errors

**Scenario 1.4: Query non-existent document type**
- **Given** no document type exists with given uuid
- **When** I send GET /api/document-types/{uuid}
- **Then** I receive **404 Not Found**

**Scenario 1.5: Update document type successfully**
- **Given** a document type exists
- **When** I send PUT /api/document-types/{uuid} with updated data
- **Then** I receive **200 OK** with the updated type
- **And** updatedBy and updatedAt are recorded

**Scenario 1.6: Deactivate document type**
- **Given** an active document type exists
- **When** I send PATCH /api/document-types/{uuid}/deactivate
- **Then** I receive **200 OK** with active=false

**Scenario 1.7: Delete document type (soft delete)**
- **Given** a document type exists
- **When** I send DELETE /api/document-types/{uuid}
- **Then** I receive **204 No Content**
- **And** the record has deletedAt populated
- **And** deletedBy is recorded

**Scenario 1.8: List active types with advanced filters**
- **Given** 5 active types and 2 inactive types exist
- **When** I send GET /api/document-types with query params:
  - enabled=true
  - search=Cedula
  - page=0, limit=10
  - fields=id,uuid,code,name,active
  - sort.field=name, sort.order=ASC
  - populate=createdBy,updatedBy
- **Then** I receive **200 OK** with matching types, sorted by name ascending
- **And** only specified fields are included
- **And** createdBy and updatedBy relationships are populated
- **And** pagination metadata is included

**Scenario 1.9: Global search**
- **Given** document types with names "Cédula de Ciudadanía", "Número de Identificación Tributaria"
- **When** I send GET /api/document-types?search=Cedula
- **Then** I receive **200 OK** with types containing "Cedula" in code, name, or description

**Scenario 1.10: Sorting and pagination**
- **Given** 15 document types exist
- **When** I send GET /api/document-types?page=1&limit=5&sort.field=code&sort.order=DESC
- **Then** I receive **200 OK** with 5 types from second page, sorted by code descending
- **And** response includes pagination metadata

**Scenario 1.11: Pagination limit exceeded**
- **Given** the listing endpoint exists
- **When** I send GET /api/document-types?limit=150
- **Then** I receive **400 Bad Request** with "Limit must not exceed 100"

---

### User Story 2 - Colombian Initial Data (Priority: P1)

**As** a system administrator  
**I need** the system to include pre-loaded Colombian document types  
**So that** I can start using them immediately

#### Acceptance Scenarios

**Scenario 2.1: Pre-loaded document types**
- **Given** the system runs migration V1.4__insert_colombia_document_types.sql
- **Then** the following types are loaded:
  - NIT - Número de Identificación Tributaria
  - CC - Cédula de Ciudadanía
  - CE - Cédula de Extranjería
  - PA - Pasaporte
  - TI - Tarjeta de Identidad
  - RC - Registro Civil

**Scenario 2.2: Query pre-loaded types**
- **Given** migrations have been executed
- **When** I send GET /api/document-types
- **Then** I receive **200 OK** with all 6 pre-loaded types

---

## Functional Requirements

### Core Functionality
- **FR-001**: System MUST allow create, read, update, and deactivate document types
- **FR-002**: System MUST validate code uniqueness
- **FR-003**: System MUST filter types by active/inactive status

### Advanced Filtering
- **FR-010**: System MUST support advanced filters in listings:
  - **enabled**: Boolean filter for active/inactive status
  - **search**: Global search in code, name, and description (case-insensitive)
  - **page**: Page number (0-based)
  - **limit**: Results per page (default: 10, max: 100)
  - **fields**: Field selection for response
  - **sort.field**: Field to sort by (code, name, createdAt, etc.)
  - **sort.order**: Sort order (ASC or DESC)
  - **populate**: Relationships to populate (createdBy, updatedBy, deletedBy)
  - **filters**: Dynamic additional filters (e.g., country, type)

### Data Validation
- **FR-004**: Code MUST be alphanumeric, 2-10 characters, unique
- **FR-005**: Name MUST be required, 1-200 characters
- **FR-006**: Description MUST be optional, maximum 500 characters

### Auditing
- **FR-008**: System MUST audit operations with:
  - createdBy, updatedBy, deletedBy (user IDs)
  - createdAt, updatedAt, deletedAt (timestamps)

### Response Format
- **FR-009**: Responses MUST include pagination and metadata for listings:
  - totalElements, totalPages, currentPage, pageSize
- **FR-011**: System MUST allow optional population of audit relationships

---

## Business Rules

### Code Management
- **BR-001**: Document codes MUST be normalized to uppercase before storage
- **BR-002**: Document codes MUST be unique across active and deleted records
- **BR-003**: Code uniqueness validation MUST be case-insensitive

### Soft Delete
- **BR-004**: Delete operations MUST be soft deletes (set deletedAt timestamp)
- **BR-005**: Soft-deleted records MUST NOT appear in default queries
- **BR-006**: Soft-deleted records MUST maintain historical data integrity

### Status Management
- **BR-007**: Newly created document types MUST be active by default
- **BR-008**: Deactivation MUST preserve all data and relationships
- **BR-009**: Only active document types SHOULD be available for selection

### Data Integrity
- **BR-010**: Document type cannot be permanently deleted if referenced by other modules
- **BR-011**: Updates MUST preserve code uniqueness

---

## Integration Points

### Consumer Modules
This module provides data to:
- **Contact Module**: Uses DocumentType for customer and supplier taxId validation
- **User Module**: May use DocumentType for personal identification
- **Company Module**: Uses DocumentType (typically NIT) for fiscal identification

### API Contract
All consumer modules will:
1. Call GET /api/document-types to retrieve available types
2. Store document type ID or UUID reference
3. Validate user-selected document type against active types

---

## Out of Scope

1. ❌ Online verification with government entities (DIAN, Registraduría)
2. ❌ OCR of physical documents
3. ❌ Validation of document validity/expiration
4. ❌ Document number format validation
5. ❌ Check digit calculation
6. ❌ Multi-language support for names
7. ❌ Document type versioning
8. ❌ Custom validation rules per document type

---

## Success Criteria

### Functional Completeness
- ✅ Complete CRUD operations implemented and verified
- ✅ Colombian initial data loaded via Flyway
- ✅ All advanced filters working correctly
- ✅ Soft delete functioning properly
- ✅ Audit trail complete for all operations

### Quality Metrics
- **Test Coverage**: > 80% on domain and application layers
- **API Documentation**: Complete Swagger/OpenAPI documentation
- **Error Handling**: All error scenarios properly handled with appropriate HTTP codes

### Performance Benchmarks
- **List Endpoint**: < 100ms p95
- **CRUD Operations**: < 150ms p95

---

## Assumptions

1. **ASSUME**: Only Colombian document types are implemented initially
2. **ASSUME**: Other countries will be added on demand
3. **ASSUME**: Document number format validation is NOT performed in this module
4. **ASSUME**: Document format validation will be implemented in consuming modules if required

---

## Open Questions

1. **Q**: Are specific format validations required per document type?
   - **PENDING**: Will be implemented in consuming modules if needed

2. **Q**: Should we allow permanent deletion of document types?
   - **DECISION**: Only soft delete allowed to preserve data integrity

3. **Q**: Is multi-language support needed for document type names?
   - **DECISION**: Not in initial version, can be added later

---

## Notes & Edge Cases

### Edge Cases

**Case 1: Duplicate codes**
- **Issue**: Prevent code duplication
- **Solution**: UNIQUE constraint on code column + validation in domain service

**Case 2: Soft delete**
- **Issue**: Deleted types must remain in system for historical data
- **Solution**: deletedAt timestamp with @Where clause in JPA entity

**Case 3: Code normalization**
- **Issue**: Ensure consistency in codes
- **Solution**: Normalize to uppercase before saving in domain service

**Case 4: Active/Inactive filtering**
- **Issue**: Users typically only want active types
- **Solution**: Provide enabled filter with default showing all (let UI decide)

**Case 5: Pagination limits**
- **Issue**: Prevent excessive data retrieval
- **Solution**: Maximum limit of 100 records per page with validation

---

## Related Documentation

- Technical Specification: [technical-spec.md](technical-spec.md)
- Implementation Plan: [plan.md](plan.md)
- Implementation Summary: [IMPLEMENTED.md](IMPLEMENTED.md)
- README Principal: [../../README.md](../../README.md)
- Error Format Standard: [../../framework/STANDARD_ERROR_FORMAT.md](../../framework/STANDARD_ERROR_FORMAT.md)
