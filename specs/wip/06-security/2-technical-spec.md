# Technical Specification: Módulo de Seguridad y Control de Acceso

**Feature**: 06-security
**Version**: 2.0
**Created**: 2026-02-01
**Last Updated**: 2026-02-19
**Status**: 🚀 EN IMPLEMENTACIÓN

---

## 🎯 Architecture Overview

Este módulo implementa **autenticación, autorización y auditoría** siguiendo la **arquitectura hexagonal** (Ports & Adapters) alineada con el scaffolding del proyecto.

### Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.2, Spring Security 7.x
- **JWT**: jjwt-api 0.12.5, jjwt-impl 0.12.5, jjwt-jackson 0.12.5
- **Persistence**: MySQL 8.0+ con Flyway
- **Security**: BCrypt (Spring Security), HMAC-SHA256 (JWT)
- **Mapping**: MapStruct 1.5.5.Final
- **Testing**: JUnit 5, Mockito, Spring Security Test
- **Documentation**: Swagger/OpenAPI 3.0

---

## 🏗️ Architecture Layers

### Regla de Dependencias

```
infrastructure → application → domain
```

Los servicios con dependencias de framework (BCrypt, JWT, SpEL) **viven en `infrastructure/`** e implementan ports definidos en `application/port/security/`. Nunca al revés.

Los beans de dominio y use cases **no usan `@Component`/`@Service`** — se declaran en `BeanConfiguration.java`.

---

### Domain Layer

#### Domain Models (`domain/model/security/`)

**User.java** (Aggregate Root) — mutable, Lombok específico (`@Getter @Builder @AllArgsConstructor`)
- Campos: id, username, email, passwordHash, firstName, lastName, documentTypeId, documentNumber, active, failedAttempts, lockedAt, lastLogin, lastFailedLoginAt, createdAt, createdBy, updatedAt, updatedBy, deletedAt
- Business methods: `incrementFailedAttempts()`, `resetFailedAttempts()`, `recordSuccessfulLogin()`, `unlock()`, `softDelete()`, `isLocked()`, `isDeleted()`
- Factory method: `User.create(...)`

**Role.java** — mutable, Lombok específico
- Campos: id, name, description, active, createdAt, updatedAt
- Business methods: `update(name, description)`
- Factory method: `Role.create(...)`

**Permission.java** — mutable, Lombok específico
- Campos: id, entity, action (PermissionAction), condition (SpEL string, nullable), description
- Business method: `hasCondition()`
- Factory method: `Permission.create(...)`

**RefreshToken.java** — mutable, Lombok específico
- Campos: id, userId, token, expiresAt, revoked, createdAt
- Business methods: `revoke()`, `isExpired()`, `isValid()`
- Factory method: `RefreshToken.create(userId, token, daysValid)`

**AuditLog.java** — mutable, Lombok específico
- Campos: id, userId, username, entity, entityId, action (AuditAction), oldValue (JSON), newValue (JSON), ipAddress, userAgent, timestamp
- Factory method: `AuditLog.create(...)`

#### Domain Enums (`domain/model/security/`)

```java
public enum PermissionAction {
    CREATE, READ, UPDATE, DELETE, APPROVE, APPROVE_DISCOUNT, UNLOCK
}

public enum AuditAction {
    LOGIN, LOGOUT, LOGIN_FAILED, ACCOUNT_LOCKED, ACCOUNT_UNLOCKED,
    USER_CREATED, USER_UPDATED, USER_DELETED,
    ROLE_CREATED, ROLE_UPDATED, ROLE_DELETED,
    PERMISSION_DENIED, CREATE, UPDATE, DELETE
}
```

#### Domain Exceptions (`domain/exception/security/`)

```
SecurityDomainException (base, RuntimeException)
├── UserNotFoundException
├── InvalidCredentialsException
├── AccountLockedException
├── DuplicateUsernameException
├── DuplicateEmailException
├── InvalidPasswordException
├── InvalidRefreshTokenException
├── RoleNotFoundException
├── PermissionDeniedException
└── RoleInUseException
```

#### Domain Service (`domain/service/security/`)

**UserDomainService.java** — sin anotaciones Spring; wired en `BeanConfiguration`
- `validateUsername(String)`: longitud 3-50, solo alfanumérico + guión bajo
- `validateEmail(String)`: formato básico RFC
- `validatePassword(String)`: mínimo 8 chars, mayúsculas, minúsculas, número, especial
- `ensureNotProtectedUser(UUID userId, UUID protectedAdminId)`: previene operaciones sobre el admin raíz

#### Domain Ports / Repository Interfaces (`domain/port/security/`)

```java
UserRepository.java
RoleRepository.java
PermissionRepository.java
RefreshTokenRepository.java
AuditLogRepository.java
```

---

### Application Layer

#### Input Ports — Use Case Interfaces (`application/port/security/`)

```java
AuthUseCase.java          // login, refreshToken, logout
ManageUserUseCase.java    // createUser, updateUser, deleteUser, unlockUser, changePassword, assignRoles
CompareUserUseCase.java   // getById, list
ManageRoleUseCase.java    // createRole, updateRole, deleteRole, assignPermissions
CompareRoleUseCase.java   // getById, listAll
ManagePermissionUseCase.java  // createPermission, checkPermission
AuditLogUseCase.java      // getAuditLogs, getById
```

#### Output Ports — Infrastructure Service Interfaces (`application/port/security/`)

Ports para servicios de infraestructura que los use cases necesitan:

```java
PasswordEncoder.java      // encode(raw), matches(raw, encoded)
TokenService.java         // generateAccessToken(user, roles, perms), extractUsername(token), validateToken(token)
ConditionEvaluator.java   // evaluate(condition, context)
```

#### Commands y Responses (`application/command/security/`) — records

```java
LoginCommand(username, password, ipAddress, userAgent)
RefreshTokenCommand(refreshToken)
LogoutCommand(refreshToken)
CreateUserCommand(username, email, password, firstName, lastName, documentTypeId, documentNumber, roleIds, createdBy)
UpdateUserCommand(email, firstName, lastName, documentTypeId, documentNumber, updatedBy)
ChangePasswordCommand(currentPassword, newPassword)
CreateRoleCommand(name, description, permissionIds)
UpdateRoleCommand(name, description)
CreatePermissionCommand(entity, action, condition, description)
LoginResponse(accessToken, refreshToken, expiresIn)
AuditLogFilter(userId, entity, action, startDate, endDate)
```

#### Use Case Implementations (`application/usecase/security/`)

Naming: `[Nombre]UseCaseImpl` — pueden usar `@Slf4j`; wired en `BeanConfiguration`

```
AuthUseCaseImpl.java            (implementa AuthUseCase)
ManageUserUseCaseImpl.java      (implementa ManageUserUseCase)
CompareUserUseCaseImpl.java     (implementa CompareUserUseCase)
ManageRoleUseCaseImpl.java      (implementa ManageRoleUseCase)
CompareRoleUseCaseImpl.java     (implementa CompareRoleUseCase)
ManagePermissionUseCaseImpl.java (implementa ManagePermissionUseCase)
AuditLogUseCaseImpl.java        (implementa AuditLogUseCase)
```

---

### Infrastructure Layer

#### Infrastructure Services (`infrastructure/security/`)

Implementaciones de los output ports de application. Usan `@Component`.

```java
JwtTokenService.java              // implementa TokenService — usa JJWT 0.12.5
BCryptPasswordEncoderAdapter.java  // implementa PasswordEncoder — usa BCryptPasswordEncoder(12)
SpelConditionEvaluator.java        // implementa ConditionEvaluator — usa SpelExpressionParser
```

**JJWT 0.12.5 API** (no usar métodos deprecated):
```java
// Generación
SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
Jwts.builder().subject(username).claim("roles", roles)
    .issuedAt(new Date()).expiration(expDate)
    .signWith(key).compact();

// Validación/extracción
Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
```

#### JPA Entities (`infrastructure/out/persistence/entity/security/`)

```
UserEntity.java        (@ManyToMany fetch=EAGER a RoleEntity via user_roles)
RoleEntity.java        (@ManyToMany a PermissionEntity via role_permissions)
PermissionEntity.java
RefreshTokenEntity.java
AuditLogEntity.java
```

Todas con `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` (MUST NOT usar @Data).

#### JPA Repositories (`infrastructure/out/persistence/`)

```
UserJpaRepository.java
RoleJpaRepository.java
PermissionJpaRepository.java
RefreshTokenJpaRepository.java
AuditLogJpaRepository.java
```

#### MapStruct Mappers (`infrastructure/out/persistence/mapper/security/`)

```
UserEntityMapper.java      (Entity ↔ Domain)
RoleEntityMapper.java
PermissionEntityMapper.java
RefreshTokenEntityMapper.java
AuditLogEntityMapper.java
```

#### Repository Adapters (`infrastructure/out/persistence/adapter/security/`)

```
UserRepositoryAdapter.java        (implementa UserRepository)
RoleRepositoryAdapter.java        (implementa RoleRepository)
PermissionRepositoryAdapter.java  (implementa PermissionRepository)
RefreshTokenRepositoryAdapter.java (implementa RefreshTokenRepository)
AuditLogRepositoryAdapter.java    (implementa AuditLogRepository)
```

#### Specification Utils (`infrastructure/out/persistence/util/security/`)

```
UserSpecificationUtil.java
AuditLogSpecificationUtil.java
```

#### Web Layer (`infrastructure/in/web/`)

**Controllers** (`controller/security/`):
```
AuthController.java      → POST /api/v1/auth/login, /api/v1/auth/refresh, /api/v1/auth/logout
UserController.java      → /api/v1/users (CRUD + unlock + change-password + assign-roles)
RoleController.java      → /api/v1/roles (CRUD + assign-permissions)
AuditLogController.java  → GET /api/v1/audit-logs
```

**DTOs** (`dto/security/`) — records:
```
LoginRequestDto, LoginResponseDto
RefreshTokenRequestDto, RefreshTokenResponseDto
LogoutRequestDto
CreateUserRequestDto, UpdateUserRequestDto, UserResponseDto
AssignRolesRequestDto
ChangePasswordRequestDto
CreateRoleRequestDto, UpdateRoleRequestDto, RoleResponseDto
CreatePermissionRequestDto, PermissionResponseDto
AssignPermissionsRequestDto
AuditLogResponseDto
```

**DTO Mappers** (`mapper/security/`):
```
UserDtoMapper.java        (Domain ↔ DTO)
RoleDtoMapper.java
AuditLogDtoMapper.java
```

**Filter** (`filter/`):
```
JwtAuthenticationFilter.java   (extiende OncePerRequestFilter)
```

#### Configuración Actualizada

- `infrastructure/config/SecurityConfig.java` — SecurityFilterChain con JWT filter, stateless sessions, CORS, rutas públicas vs protegidas
- `infrastructure/config/BeanConfiguration.java` — wiring de todos los beans nuevos (UserDomainService, todos los UseCaseImpl, adicionalmente ports registrados)

---

## 🗄️ Database Schema

**Flyway**: `src/main/resources/db/migration/V1_9__create_security_tables.sql`
**Docker sync**: `docker/mysql-init/V1_9__create_security_tables.sql`

Tablas: `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `refresh_tokens`, `audit_logs`

**Seed**: `V1_10__insert_security_seed_data.sql`
- Admin user (username: `admin`, password BCrypt de `Admin123!`)
- Roles por defecto: `ADMIN`, `USER`
- Permisos básicos de sistema

---

## ⚙️ Configuration

Agregar a `application.properties`:
```properties
jwt.secret=${JWT_SECRET:dev-secret-key-must-be-at-least-256-bits-long-for-hs256}
jwt.access-token-expiration=1800
jwt.refresh-token-expiration=604800
```

---

## 🧪 Testing Strategy

- **Domain models**: tests puros JUnit 5 — business methods y factory methods
- **UserDomainService**: tests unitarios puros — validaciones
- **Infrastructure services**: tests unitarios — JwtTokenService, BCryptPasswordEncoderAdapter
- **Use cases**: tests unitarios con Mockito — todos los ports mockeados
- **Controllers**: MockMvc + `@WithMockUser` de Spring Security Test
- **Cobertura objetivo**: >= 90% (estándar del proyecto)

---

**Status**: 🚀 EN IMPLEMENTACIÓN — v2.0 alineada con scaffolding del proyecto
