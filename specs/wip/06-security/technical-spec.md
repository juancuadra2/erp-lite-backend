# Technical Specification: Módulo de Seguridad y Control de Acceso

**Feature**: 06-security  
**Version**: 1.0  
**Created**: 2026-02-01  
**Last Updated**: 2026-02-01  
**Status**: ⏳ PHASE 2 - Awaiting Technical Review

---

## 🎯 Architecture Overview

Este módulo implementa **autenticación, autorización y auditoría** siguiendo la **arquitectura hexagonal** (Ports & Adapters).

### Tech Stack

- **Backend**: Java 17+, Spring Boot 3.x, Spring Security 6.2+
- **JWT**: jjwt-api 0.12.5, jjwt-impl 0.12.5, jjwt-jackson 0.12.5
- **Persistence**: MySQL 8.0+ con Flyway
- **Security**: BCrypt (Spring Security), HMAC-SHA256 (JWT)
- **Mapping**: MapStruct 1.5+
- **Testing**: JUnit 5, Mockito, Spring Security Test, Testcontainers
- **Documentation**: Swagger/OpenAPI 3.0

---

## 🏗️ Architecture Layers

### Domain Layer (Core Business Logic)

**Purpose**: Modelos y reglas de negocio independientes de tecnología.

#### Domain Models (`domain/security/model/`)

**User.java** (Aggregate Root)
```java
@Getter
@Builder
@AllArgsConstructor
public class User {
    private UUID id;
    private String username;           // 3-50 chars, unique
    private String email;              // unique, RFC 5322 format
    private String passwordHash;       // BCrypt, 60 chars
    private String firstName;
    private String lastName;
    private UUID documentTypeId;
    private String documentNumber;
    private boolean active;
    private int failedAttempts;
    private LocalDateTime lockedAt;
    private LocalDateTime lastLogin;
    private LocalDateTime lastFailedLoginAt;
    
    // Audit fields
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
    private LocalDateTime deletedAt;
    
    // Factory method
    public static User create(String username, String email, String passwordHash, 
                             String firstName, String lastName, UUID userId) {
        return User.builder()
            .id(UUID.randomUUID())
            .username(username)
            .email(email)
            .passwordHash(passwordHash)
            .firstName(firstName)
            .lastName(lastName)
            .active(true)
            .failedAttempts(0)
            .createdAt(LocalDateTime.now())
            .createdBy(userId)
            .build();
    }
    
    // Business methods
    public void incrementFailedAttempts() {
        this.failedAttempts++;
        this.lastFailedLoginAt = LocalDateTime.now();
        if (this.failedAttempts >= 5) {
            this.active = false;
            this.lockedAt = LocalDateTime.now();
        }
    }
    
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lastFailedLoginAt = null;
    }
    
    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        resetFailedAttempts();
    }
    
    public void unlock() {
        this.active = true;
        this.failedAttempts = 0;
        this.lockedAt = null;
    }
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    public boolean isLocked() {
        return !this.active && this.failedAttempts >= 5;
    }
    
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
```

**Role.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class Role {
    private UUID id;
    private String name;              // unique
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static Role create(String name, String description) {
        return Role.builder()
            .id(UUID.randomUUID())
            .name(name)
            .description(description)
            .active(true)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
}
```

**Permission.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class Permission {
    private UUID id;
    private String entity;            // "Product", "Sale", "User"
    private PermissionAction action;  // Enum: CREATE, READ, UPDATE, DELETE, APPROVE
    private String condition;         // SpEL: "discountPercentage <= 15"
    private String description;
    
    public static Permission create(String entity, PermissionAction action, 
                                   String condition, String description) {
        return Permission.builder()
            .id(UUID.randomUUID())
            .entity(entity)
            .action(action)
            .condition(condition)
            .description(description)
            .build();
    }
    
    public boolean hasCondition() {
        return condition != null && !condition.isBlank();
    }
}
```

**RefreshToken.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String token;             // UUID v4
    private LocalDateTime expiresAt;
    private boolean revoked;
    private LocalDateTime createdAt;
    
    public static RefreshToken create(UUID userId, String token, int daysValid) {
        return RefreshToken.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .token(token)
            .expiresAt(LocalDateTime.now().plusDays(daysValid))
            .revoked(false)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public void revoke() {
        this.revoked = true;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
```

**AuditLog.java**
```java
@Getter
@Builder
@AllArgsConstructor
public class AuditLog {
    private UUID id;
    private UUID userId;
    private String username;
    private String entity;
    private UUID entityId;
    private AuditAction action;       // Enum: LOGIN, LOGOUT, CREATE, UPDATE, DELETE, etc.
    private String oldValue;          // JSON
    private String newValue;          // JSON
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    
    public static AuditLog create(UUID userId, String username, String entity,
                                 UUID entityId, AuditAction action, String ipAddress,
                                 String userAgent) {
        return AuditLog.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .username(username)
            .entity(entity)
            .entityId(entityId)
            .action(action)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

#### Domain Enums (`domain/security/model/`)

```java
public enum PermissionAction {
    CREATE, READ, UPDATE, DELETE, APPROVE, APPROVE_DISCOUNT, UNLOCK
}

public enum AuditAction {
    LOGIN, LOGOUT, LOGIN_FAILED, ACCOUNT_LOCKED, ACCOUNT_UNLOCKED,
    USER_CREATED, USER_UPDATED, USER_DELETED,
    ROLE_CREATED, ROLE_UPDATED, ROLE_DELETED,
    PERMISSION_DENIED,
    CREATE, READ, UPDATE, DELETE
}
```

#### Domain Exceptions (`domain/security/exception/`)

```java
// Base exception
public class SecurityException extends RuntimeException

// Specific exceptions
public class UserNotFoundException extends SecurityException
public class InvalidCredentialsException extends SecurityException
public class AccountLockedException extends SecurityException
public class DuplicateUsernameException extends SecurityException
public class DuplicateEmailException extends SecurityException
public class InvalidPasswordException extends SecurityException
public class InvalidRefreshTokenException extends SecurityException
public class RoleNotFoundException extends SecurityException
public class PermissionDeniedException extends SecurityException
public class RoleInUseException extends SecurityException
```

#### Domain Services (`domain/security/service/`)

**UserDomainService.java**
```java
@Service
@RequiredArgsConstructor
public class UserDomainService {
    
    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserDataException("Username no puede estar vacío");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new InvalidUserDataException("Username debe tener entre 3 y 50 caracteres");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new InvalidUserDataException("Username solo puede contener letras, números y guión bajo");
        }
    }
    
    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidUserDataException("Email no puede estar vacío");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidUserDataException("Email no tiene formato válido");
        }
    }
    
    public void validatePassword(String password) {
        List<String> violations = new ArrayList<>();
        
        if (password == null || password.length() < 8) {
            violations.add("Debe tener mínimo 8 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            violations.add("Debe contener al menos 1 letra mayúscula");
        }
        if (!password.matches(".*[a-z].*")) {
            violations.add("Debe contener al menos 1 letra minúscula");
        }
        if (!password.matches(".*[0-9].*")) {
            violations.add("Debe contener al menos 1 número");
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            violations.add("Debe contener al menos 1 carácter especial (!@#$%^&*)");
        }
        
        if (!violations.isEmpty()) {
            throw new InvalidPasswordException(String.join(", ", violations));
        }
    }
    
    public void ensureNotAdminUser(UUID userId) {
        // Admin user ID is reserved (typically first user created)
        // Prevent deletion or critical modifications
    }
}
```

**PasswordHashingService.java**
```java
@Service
public class PasswordHashingService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    
    public String hashPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }
    
    public boolean matches(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
```

**JwtService.java**
```java
@Service
@RequiredArgsConstructor
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token-expiration:1800}") // 30 minutes
    private int accessTokenExpiration;
    
    public String generateAccessToken(User user, List<String> roles, List<String> permissions) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("roles", roles)
            .claim("permissions", permissions)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
            .setIssuer("erp-lite")
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }
    
    public String extractUsername(String token) {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
```

**PermissionEvaluationService.java**
```java
@Service
public class PermissionEvaluationService {
    private final SpelExpressionParser parser = new SpelExpressionParser();
    
    public boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (condition == null || condition.isBlank()) {
            return true; // No condition = always allowed
        }
        
        try {
            Expression exp = parser.parseExpression(condition);
            StandardEvaluationContext evalContext = new StandardEvaluationContext();
            context.forEach(evalContext::setVariable);
            
            Boolean result = exp.getValue(evalContext, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            // Log error and deny by default
            return false;
        }
    }
}
```

---

### Application Layer (Use Cases)

**Purpose**: Orquestación de casos de uso, lógica de aplicación

#### Input Ports (Use Cases) - `application/port/input/security/`

```java
// Authentication
public interface LoginUseCase {
    LoginResponse execute(LoginCommand command);
}

public interface RefreshTokenUseCase {
    RefreshTokenResponse execute(RefreshTokenCommand command);
}

public interface LogoutUseCase {
    void execute(LogoutCommand command);
}

// User Management
public interface CreateUserUseCase {
    User execute(CreateUserCommand command);
}

public interface UpdateUserUseCase {
    User execute(UUID id, UpdateUserCommand command);
}

public interface DeleteUserUseCase {
    void execute(UUID id);
}

public interface UnlockUserUseCase {
    void execute(UUID id);
}

public interface ChangePasswordUseCase {
    void execute(UUID id, ChangePasswordCommand command);
}

public interface ListUsersUseCase {
    Page<User> execute(Pageable pageable);
}

// Role Management
public interface CreateRoleUseCase {
    Role execute(CreateRoleCommand command);
}

public interface AssignRoleToUserUseCase {
    void execute(UUID userId, List<UUID> roleIds);
}

// Audit
public interface GetAuditLogsUseCase {
    Page<AuditLog> execute(AuditLogFilter filter, Pageable pageable);
}
```

#### Output Ports (Repository Interfaces) - `application/port/output/security/`

```java
public interface UserPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Page<User> findAll(Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

public interface RolePort {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    List<Role> findByIds(List<UUID> ids);
    List<Role> findAll();
    long countUsersByRoleId(UUID roleId);
}

public interface PermissionPort {
    Permission save(Permission permission);
    List<Permission> findByRoleId(UUID roleId);
    List<Permission> findByUserId(UUID userId);
}

public interface RefreshTokenPort {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    void revokeByUserId(UUID userId);
}

public interface AuditLogPort {
    AuditLog save(AuditLog auditLog);
    Page<AuditLog> findByFilter(AuditLogFilter filter, Pageable pageable);
}
```

#### Services (`application/service/security/`)

**LoginService.java**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class LoginService implements LoginUseCase {
    private final UserPort userPort;
    private final RolePort rolePort;
    private final PermissionPort permissionPort;
    private final RefreshTokenPort refreshTokenPort;
    private final PasswordHashingService passwordHashingService;
    private final JwtService jwtService;
    private final AuditLogPort auditLogPort;
    
    @Override
    public LoginResponse execute(LoginCommand command) {
        // 1. Find user
        User user = userPort.findByUsername(command.username())
            .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));
        
        // 2. Check if locked
        if (user.isLocked()) {
            throw new AccountLockedException("Cuenta bloqueada. Contacte al administrador");
        }
        
        // 3. Verify password
        if (!passwordHashingService.matches(command.password(), user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userPort.save(user);
            
            // Audit failed attempt
            auditLogPort.save(AuditLog.create(
                user.getId(), user.getUsername(), "User", user.getId(),
                AuditAction.LOGIN_FAILED, command.ipAddress(), command.userAgent()
            ));
            
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
        
        // 4. Success - reset failed attempts
        user.recordSuccessfulLogin();
        userPort.save(user);
        
        // 5. Get roles and permissions
        List<String> roles = getRoleNames(user.getId());
        List<String> permissions = getPermissionStrings(user.getId());
        
        // 6. Generate tokens
        String accessToken = jwtService.generateAccessToken(user, roles, permissions);
        String refreshToken = UUID.randomUUID().toString();
        
        RefreshToken token = RefreshToken.create(user.getId(), refreshToken, 7);
        refreshTokenPort.save(token);
        
        // 7. Audit successful login
        auditLogPort.save(AuditLog.create(
            user.getId(), user.getUsername(), "User", user.getId(),
            AuditAction.LOGIN, command.ipAddress(), command.userAgent()
        ));
        
        return new LoginResponse(accessToken, refreshToken, 1800);
    }
}
```

---

### Infrastructure Layer (Adapters)

#### Output Adapters (Persistence)

**UserEntity.java** (`infrastructure/persistence/entity/`)
```java
@Entity
@Table(name = "users",
       indexes = {
           @Index(name = "idx_user_username", columnList = "username"),
           @Index(name = "idx_user_email", columnList = "email"),
           @Index(name = "idx_user_active", columnList = "active"),
           @Index(name = "idx_user_deleted_at", columnList = "deleted_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
           @UniqueConstraint(name = "uk_user_email", columnNames = "email")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;
    
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "document_type_id", columnDefinition = "BINARY(16)")
    private UUID documentTypeId;
    
    @Column(name = "document_number", length = 20)
    private String documentNumber;
    
    @Column(name = "active", nullable = false)
    private Boolean active;
    
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;
    
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", columnDefinition = "BINARY(16)", updatable = false)
    private UUID createdBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", columnDefinition = "BINARY(16)")
    private UUID updatedBy;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();
}
```

**Database Schema** (`resources/db/migration/V1.9__create_security_tables.sql`)
```sql
CREATE TABLE users (
    id             BINARY(16)   NOT NULL,
    username       VARCHAR(50)  NOT NULL,
    email          VARCHAR(100) NOT NULL,
    password_hash  VARCHAR(60)  NOT NULL,
    first_name     VARCHAR(50),
    last_name      VARCHAR(50),
    document_type_id BINARY(16),
    document_number VARCHAR(20),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts INT         NOT NULL DEFAULT 0,
    locked_at      DATETIME,
    last_login     DATETIME,
    last_failed_login_at DATETIME,
    created_at     DATETIME     NOT NULL,
    created_by     BINARY(16),
    updated_at     DATETIME,
    updated_by     BINARY(16),
    deleted_at     DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email),
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_active (active),
    INDEX idx_user_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    id          BINARY(16)   NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permissions (
    id          BINARY(16)   NOT NULL,
    entity      VARCHAR(50)  NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    `condition` VARCHAR(255),
    description VARCHAR(255),
    PRIMARY KEY (id),
    INDEX idx_permission_entity_action (entity, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    role_id       BINARY(16) NOT NULL,
    permission_id BINARY(16) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id         BINARY(16)   NOT NULL,
    user_id    BINARY(16)   NOT NULL,
    token      VARCHAR(36)  NOT NULL,
    expires_at DATETIME     NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token UNIQUE (token),
    INDEX idx_rt_user_id (user_id),
    INDEX idx_rt_expires_at (expires_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id         BINARY(16)   NOT NULL,
    user_id    BINARY(16),
    username   VARCHAR(50),
    entity     VARCHAR(50),
    entity_id  BINARY(16),
    action     VARCHAR(50)  NOT NULL,
    old_value  TEXT,
    new_value  TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    timestamp  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_entity (entity),
    INDEX idx_audit_action (action),
    INDEX idx_audit_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### Input Adapters (REST API)

**AuthController.java**
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticación")
public class AuthController {
    
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto,
            HttpServletRequest request) {
        
        LoginCommand command = new LoginCommand(
            dto.username(),
            dto.password(),
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        
        LoginResponse response = loginUseCase.execute(command);
        
        return ResponseEntity.ok(new LoginResponseDto(
            response.accessToken(),
            response.refreshToken(),
            response.expiresIn()
        ));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    public ResponseEntity<RefreshTokenResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto dto) {
        
        RefreshTokenCommand command = new RefreshTokenCommand(dto.refreshToken());
        RefreshTokenResponse response = refreshTokenUseCase.execute(command);
        
        return ResponseEntity.ok(new RefreshTokenResponseDto(
            response.accessToken(),
            response.refreshToken(),
            response.expiresIn()
        ));
    }
    
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesión")
    public void logout(@Valid @RequestBody LogoutRequestDto dto) {
        LogoutCommand command = new LogoutCommand(dto.refreshToken());
        logoutUseCase.execute(command);
    }
}
```

---

## 📊 Performance Considerations

### Database Optimization

1. **Indexes**: username, email, active, deletedAt, refresh token
2. **Query optimization**: Use covering indexes, avoid N+1
3. **Connection pooling**: HikariCP con 20-30 connections

### Security Optimization

1. **JWT**: Cache public key, validate signature once
2. **BCrypt**: Cost factor 12 (balance seguridad/performance)
3. **Auditoría**: Asíncrona con `@Async` y ThreadPoolTaskExecutor

### Performance Goals

| Operation | Target | Measurement |
|-----------|--------|-------------|
| Login | < 500ms p95 | JMeter |
| Token refresh | < 200ms p95 | APM logs |
| Permission check | < 50ms p95 | APM logs |
| Audit query | < 1s p95 | JMeter |

---

## ⚙️ Configuration

### Application Properties

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:replace-with-secure-256-bit-secret}
jwt.access-token-expiration=1800
jwt.refresh-token-expiration=604800

# BCrypt Configuration
security.bcrypt.cost-factor=12

# Async Audit Configuration
audit.async.core-pool-size=5
audit.async.max-pool-size=10
audit.async.queue-capacity=100
```

---

## 🧪 Testing Strategy

### Unit Tests
- Domain model tests: Business logic methods
- Domain service tests: Validation logic
- Use case tests: Mocked ports
- JWT service tests: Token generation/validation

### Integration Tests
- Repository tests: Testcontainers MySQL
- Controller tests: MockMvc + Spring Security Test
- End-to-end auth flow: Login → Use token → Refresh → Logout

### Test Coverage Target
- **Overall**: >= 85%
- **Domain layer**: >= 95%
- **Application layer**: >= 90%
- **Infrastructure layer**: >= 75%

---

## 🔒 Security Considerations

### OWASP Top 10 Compliance

1. **A01:2021 – Broken Access Control**: Permisos granulares con validación
2. **A02:2021 – Cryptographic Failures**: BCrypt para passwords, JWT firmados
3. **A03:2021 – Injection**: PreparedStatements, SpEL con whitelist
4. **A07:2021 – Identification and Authentication Failures**: Account locking, token rotation

### Security Best Practices

- JWT secret >= 256 bits, nunca en código
- BCrypt cost factor >= 12
- Refresh token rotation obligatorio
- Rate limiting en login (5 intentos / 60 segundos)
- Auditoría exhaustiva e inmutable
- HTTPS obligatorio en producción

---

**Status**: ⚠️ PHASE 2 - Technical Draft  
**Next Step**: Tech Lead Review → Approve → Move to PHASE 3
