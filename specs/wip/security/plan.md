# Implementation Plan: Security Module

**Feature**: 06-security  
**Module**: Security and Access Control  
**Created**: 2026-02-01  
**Status**: ⏳ Awaiting Approval

---

## 📋 Project Summary

| Property | Value |
|----------|-------|
| **Estimated Complexity** | 90 Story Points (~11.25 days @ 8 SP/day) |
| **Total Tasks** | 90 tasks |
| **Total Phases** | 12 phases |
| **Risk Level** | 🔴 HIGH (Critical security module) |
| **Dependencies** | Document Types (for user documents) |
| **Blocks** | ALL other modules (foundational) |

---

## 🎯 Objectives

1. **JWT Authentication**: Access tokens (30 min) + refresh tokens (7 days)
2. **Authorization**: Roles & granular permissions with SpEL conditions
3. **Account Security**: Auto-lock after 5 failed attempts
4. **User Management**: CRUD with soft delete
5. **Audit Logging**: Complete traceability of all actions
6. **Password Security**: Policy validation + BCrypt hashing

---

## 📦 Task Distribution by Phase

| Phase | Tasks | Story Points | Duration |
|-------|-------|--------------|----------|
| **PHASE 1**: Setup | 4 | 4 | 0.5 days |
| **PHASE 2**: Domain Models | 10 | 10 | 1.25 days |
| **PHASE 3**: Domain Services | 7 | 7 | 0.88 days |
| **PHASE 4**: Application Ports | 8 | 8 | 1 day |
| **PHASE 5**: Authentication | 12 | 14 | 1.75 days |
| **PHASE 6**: User Management | 10 | 10 | 1.25 days |
| **PHASE 7**: Role & Permissions | 10 | 10 | 1.25 days |
| **PHASE 8**: Audit Logging | 6 | 6 | 0.75 days |
| **PHASE 9**: Infrastructure (DB) | 8 | 8 | 1 day |
| **PHASE 10**: Security Config | 5 | 5 | 0.63 days |
| **PHASE 11**: Testing | 8 | 8 | 1 day |
| **PHASE 12**: Documentation | 2 | 1 | 0.13 days |

---

## 🚀 Detailed Implementation Plan

### PHASE 1: Setup (4 SP)

**Goal**: Configure project dependencies and structure.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-001 | Add Spring Security dependencies (6.2.1) | 1 | - |
| SEC-002 | Add JWT dependencies (jjwt 0.12.5: api, impl, jackson) | 1 | - |
| SEC-003 | Add BCrypt dependency (included in Spring Security) | 1 | - |
| SEC-004 | Create package structure (domain, application, infrastructure) | 1 | - |

---

### PHASE 2: Domain Models (10 SP)

**Goal**: Create domain entities and value objects.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-005 | Create User domain model | 2 | SEC-004 |
| SEC-006 | Create Role domain model | 1 | SEC-004 |
| SEC-007 | Create Permission domain model | 1 | SEC-004 |
| SEC-008 | Create RefreshToken domain model | 1 | SEC-004 |
| SEC-009 | Create AuditLog domain model | 1 | SEC-004 |
| SEC-010 | Create PermissionAction enum (CREATE, READ, etc.) | 1 | SEC-007 |
| SEC-011 | Create AuditAction enum (LOGIN, LOGOUT, etc.) | 1 | SEC-009 |
| SEC-012 | Create SecurityException hierarchy | 1 | SEC-004 |
| SEC-013 | Add factory methods to domain models | 1 | SEC-005 to SEC-009 |

---

### PHASE 3: Domain Services (7 SP)

**Goal**: Implement domain business logic.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-014 | Implement UserDomainService (validations) | 2 | SEC-005 |
| SEC-015 | Implement PasswordHashingService (BCrypt) | 1 | SEC-001, SEC-003 |
| SEC-016 | Implement JwtService (generate, validate, extract) | 2 | SEC-002 |
| SEC-017 | Implement PermissionEvaluationService (SpEL) | 2 | SEC-007 |

---

### PHASE 4: Application Ports (8 SP)

**Goal**: Define input/output ports (interfaces).

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-018 | Create authentication ports (LoginUseCase, etc.) | 1 | SEC-004 |
| SEC-019 | Create user management ports (CreateUser, etc.) | 1 | SEC-004 |
| SEC-020 | Create role management ports | 1 | SEC-004 |
| SEC-021 | Create audit ports | 1 | SEC-004 |
| SEC-022 | Create UserPort (repository interface) | 1 | SEC-005 |
| SEC-023 | Create RolePort (repository interface) | 1 | SEC-006 |
| SEC-024 | Create PermissionPort (repository interface) | 1 | SEC-007 |
| SEC-025 | Create RefreshTokenPort (repository interface) | 1 | SEC-008 |
| SEC-026 | Create AuditLogPort (repository interface) | 1 | SEC-009 |

---

### PHASE 5: Authentication (14 SP)

**Goal**: Implement JWT authentication flow.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-027 | Implement LoginService | 2 | SEC-014 to SEC-017, SEC-018, SEC-022 |
| SEC-028 | Implement RefreshTokenService | 1 | SEC-016, SEC-025 |
| SEC-029 | Implement LogoutService (revoke refresh tokens) | 1 | SEC-025 |
| SEC-030 | Create LoginCommand, LoginResponse DTOs | 1 | SEC-027 |
| SEC-031 | Create RefreshTokenCommand, Response DTOs | 1 | SEC-028 |
| SEC-032 | Implement account locking logic in LoginService | 2 | SEC-027 |
| SEC-033 | Implement failed attempts tracking | 1 | SEC-027 |
| SEC-034 | Add audit logging to authentication flow | 1 | SEC-026, SEC-027 |
| SEC-035 | Unit test LoginService | 2 | SEC-027 |
| SEC-036 | Unit test RefreshTokenService | 1 | SEC-028 |
| SEC-037 | Unit test LogoutService | 1 | SEC-029 |

---

### PHASE 6: User Management (10 SP)

**Goal**: Implement user CRUD operations.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-038 | Implement CreateUserService | 1 | SEC-014, SEC-015, SEC-019, SEC-022 |
| SEC-039 | Implement UpdateUserService | 1 | SEC-014, SEC-019, SEC-022 |
| SEC-040 | Implement DeleteUserService (soft delete) | 1 | SEC-019, SEC-022 |
| SEC-041 | Implement GetUserService | 1 | SEC-019, SEC-022 |
| SEC-042 | Implement ListUsersService (pagination) | 1 | SEC-019, SEC-022 |
| SEC-043 | Implement UnlockUserService | 1 | SEC-019, SEC-022 |
| SEC-044 | Implement ChangePasswordService | 1 | SEC-014, SEC-015, SEC-019, SEC-022 |
| SEC-045 | Add audit logging to user operations | 1 | SEC-026 |
| SEC-046 | Unit test CreateUserService | 1 | SEC-038 |
| SEC-047 | Unit test UpdateUserService | 1 | SEC-039 |

---

### PHASE 7: Role & Permissions (10 SP)

**Goal**: Implement role-based access control.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-048 | Implement CreateRoleService | 1 | SEC-020, SEC-023 |
| SEC-049 | Implement UpdateRoleService | 1 | SEC-020, SEC-023 |
| SEC-050 | Implement DeleteRoleService (check usage) | 1 | SEC-020, SEC-023 |
| SEC-051 | Implement ListRolesService | 1 | SEC-020, SEC-023 |
| SEC-052 | Implement AssignRoleToUserService | 2 | SEC-020, SEC-022, SEC-023 |
| SEC-053 | Implement CreatePermissionService | 1 | SEC-020, SEC-024 |
| SEC-054 | Implement AssignPermissionToRoleService | 1 | SEC-020, SEC-023, SEC-024 |
| SEC-055 | Implement CheckPermissionService (SpEL evaluation) | 2 | SEC-017, SEC-024 |

---

### PHASE 8: Audit Logging (6 SP)

**Goal**: Implement audit trail.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-056 | Implement SaveAuditLogService (async) | 2 | SEC-021, SEC-026 |
| SEC-057 | Implement GetAuditLogsService (filter, pagination) | 2 | SEC-021, SEC-026 |
| SEC-058 | Create AuditLogFilter DTO | 1 | SEC-057 |
| SEC-059 | Configure @Async for audit logging | 1 | SEC-001, SEC-056 |

---

### PHASE 9: Infrastructure - Database (8 SP)

**Goal**: Implement JPA entities, repositories, mappers.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-060 | Create UserEntity (JPA) | 1 | SEC-005 |
| SEC-061 | Create RoleEntity (JPA) | 1 | SEC-006 |
| SEC-062 | Create PermissionEntity (JPA) | 1 | SEC-007 |
| SEC-063 | Create RefreshTokenEntity (JPA) | 1 | SEC-008 |
| SEC-064 | Create AuditLogEntity (JPA) | 1 | SEC-009 |
| SEC-065 | Create UserJpaRepository (Spring Data JPA) | 1 | SEC-060 |
| SEC-066 | Create RoleJpaRepository | 1 | SEC-061 |
| SEC-067 | Create PermissionJpaRepository | 1 | SEC-062 |
| SEC-068 | Create RefreshTokenJpaRepository | 1 | SEC-063 |
| SEC-069 | Create AuditLogJpaRepository | 1 | SEC-064 |
| SEC-070 | Create UserMapper (MapStruct: Entity ↔ Domain) | 1 | SEC-005, SEC-060 |
| SEC-071 | Create RoleMapper (MapStruct) | 1 | SEC-006, SEC-061 |
| SEC-072 | Create PermissionMapper (MapStruct) | 1 | SEC-007, SEC-062 |
| SEC-073 | Create RefreshTokenMapper (MapStruct) | 1 | SEC-008, SEC-063 |
| SEC-074 | Create AuditLogMapper (MapStruct) | 1 | SEC-009, SEC-064 |
| SEC-075 | Implement UserAdapter (Port implementation) | 1 | SEC-022, SEC-065, SEC-070 |
| SEC-076 | Implement RoleAdapter | 1 | SEC-023, SEC-066, SEC-071 |
| SEC-077 | Implement PermissionAdapter | 1 | SEC-024, SEC-067, SEC-072 |
| SEC-078 | Implement RefreshTokenAdapter | 1 | SEC-025, SEC-068, SEC-073 |
| SEC-079 | Implement AuditLogAdapter | 1 | SEC-026, SEC-069, SEC-074 |
| SEC-080 | Create Flyway migration V1.9__create_security_tables.sql | 2 | - |

---

### PHASE 10: Security Configuration (5 SP)

**Goal**: Configure Spring Security, JWT filter, CORS.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-081 | Create JwtAuthenticationFilter | 2 | SEC-016 |
| SEC-082 | Create SecurityConfig (SecurityFilterChain) | 2 | SEC-001, SEC-081 |
| SEC-083 | Configure CORS (allow specific origins) | 1 | SEC-082 |

---

### PHASE 11: Testing (8 SP)

**Goal**: Comprehensive testing coverage.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-084 | Unit tests for domain models | 1 | SEC-005 to SEC-011 |
| SEC-085 | Unit tests for domain services | 2 | SEC-014 to SEC-017 |
| SEC-086 | Unit tests for use cases (LoginService, etc.) | 2 | SEC-027 to SEC-055 |
| SEC-087 | Integration tests for repositories (Testcontainers) | 2 | SEC-065 to SEC-079 |
| SEC-088 | Integration tests for REST controllers (MockMvc) | 2 | SEC-090 to SEC-092 |
| SEC-089 | End-to-end authentication flow test | 2 | SEC-027, SEC-028, SEC-029 |

---

### PHASE 12: Documentation (1 SP)

**Goal**: Complete API and code documentation.

| ID | Task | SP | Dependencies |
|----|------|---:|--------------|
| SEC-090 | Add OpenAPI/Swagger annotations to controllers | 1 | SEC-090 to SEC-092 |
| SEC-091 | Generate API documentation | 0 | SEC-093 |

---


## 📊 Dependencies Graph

```
PHASE 1 (Setup)
    ↓
PHASE 2 (Domain Models)
    ↓
PHASE 3 (Domain Services) + PHASE 4 (Ports)
    ↓
PHASE 5 (Authentication) + PHASE 6 (User Mgmt) + PHASE 7 (Roles) + PHASE 8 (Audit)
    ↓
PHASE 9 (Infrastructure)
    ↓
PHASE 10 (Security Config)
    ↓
PHASE 11 (Testing)
    ↓
PHASE 12 (Documentation)
```

---

## 🎯 Milestones

| Milestone | Tasks | Completion Criteria |
|-----------|-------|---------------------|
| **M1: Domain Complete** | SEC-001 to SEC-017 | All domain models and services implemented with unit tests |
| **M2: Auth Working** | SEC-018 to SEC-037 | Login, refresh, logout working with unit tests |
| **M3: User CRUD Ready** | SEC-038 to SEC-047 | User management operations functional |
| **M4: RBAC Complete** | SEC-048 to SEC-055 | Role and permission system working |
| **M5: Audit Operational** | SEC-056 to SEC-059 | Audit logs being created asynchronously |
| **M6: DB Persistence** | SEC-060 to SEC-080 | All data persisting correctly in MySQL |
| **M7: Security Configured** | SEC-081 to SEC-083 | Spring Security protecting endpoints |
| **M8: Production Ready** | SEC-084 to SEC-091 | All tests passing, documented |

---

## ⚠️ Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| JWT secret exposure | 🔴 Critical | Medium | Use environment variables, never commit to repo |
| Performance degradation (BCrypt) | 🟡 Medium | Low | Use cost factor 12, monitor login times |
| SpEL injection attacks | 🔴 Critical | Low | Whitelist allowed expressions, sanitize input |
| Audit log storage growth | 🟡 Medium | High | Implement log rotation, archiving strategy |
| Failed migration of security data | 🔴 Critical | Low | Test migrations thoroughly, have rollback plan |

---

## 📏 Definition of Done

✅ All 92 tasks completed and code merged  
✅ Unit test coverage >= 85%  
✅ Integration tests passing  
✅ All endpoints documented with Swagger  
✅ Security review completed (OWASP Top 10)  
✅ Performance tests meet targets (login < 500ms p95)  
✅ Code review approved by Tech Lead  
✅ Manual QA testing completed  
✅ Database migrations applied successfully  
✅ Documentation updated  

---

**Status**: ⏳ Awaiting Approval  
**Next Step**: Product Owner & Tech Lead Approval → Move to PHASE 1 Implementation
