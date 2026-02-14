# Functional Specification: Módulo de Seguridad y Control de Acceso

**Feature**: 06-security  
**Version**: 1.0  
**Created**: 2026-02-01  
**Last Updated**: 2026-02-01  
**Status**: ⏳ PHASE 1 - Awaiting Approval

---

## 🎯 Overview

### Purpose

Este módulo proporciona **autenticación, autorización y auditoría** para el sistema ERP Lite. Implementa autenticación JWT con refresh tokens, control de acceso basado en roles y permisos granulares, bloqueo automático de cuentas, auditoría completa de operaciones, y gestión de políticas de contraseña.

### Why This Feature Exists

**Business Driver**: Necesidad crítica de controlar el acceso al sistema, proteger datos sensibles y cumplir con requisitos de seguridad y auditoría.

**Problem**: Sin autenticación y autorización robustas, el sistema está vulnerable a accesos no autorizados y no hay trazabilidad de operaciones.

**Solution**: Sistema integral de seguridad con JWT, roles personalizables, permisos granulares con condiciones y auditoría exhaustiva.

### Scope

**✅ In Scope:**
- Autenticación JWT (access + refresh tokens)
- Refresh token rotation
- Bloqueo automático por intentos fallidos (5 intentos)
- CRUD completo de usuarios
- CRUD de roles y permisos
- Permisos granulares con condiciones (SpEL)
- Múltiples roles por usuario
- Auditoría completa de operaciones críticas
- Política de contraseñas configurable
- Historial de contraseñas (5 anteriores)
- Soft delete de usuarios
- Desbloqueo manual de cuentas

**❌ Out of Scope:**
- OAuth2 / Social Login (Google, Facebook)
- Two-Factor Authentication (2FA)
- SSO (Single Sign-On)
- LDAP/Active Directory integration
- Password recovery via email
- Biometric authentication
- Session management con WebSockets
- API keys para integración externa

---

## 👥 User Stories

### US-01: Autenticación JWT con Refresh Token (Prioridad: P0 - Crítica)

**Como** usuario del sistema  
**Quiero** autenticarme de forma segura usando JWT  
**Para** acceder a las funcionalidades del ERP con tokens de corta duración y renovación automática

**Why this priority?**: Sin autenticación, no hay acceso al sistema. Es el requisito más crítico.

**Independent Test**: Hacer login, recibir access/refresh tokens, usar access token, renovar con refresh token, verificar rotation.

#### Acceptance Scenarios

##### 1. Login exitoso con credenciales válidas

**Given** existe un usuario activo con username "admin" y password "Admin123!"  
**When** envío POST `/api/auth/login`
```json
{
  "username": "admin",
  "password": "Admin123!"
}
```
**Then** sistema responde HTTP 200 OK  
**And** retorna `accessToken` (JWT)  
**And** retorna `refreshToken` (UUID)  
**And** accessToken expira en 30 minutos (1800 segundos)  
**And** refreshToken expira en 7 días (604800 segundos)  
**And** response incluye campo `expiresIn` con segundos restantes  
**And** se registra evento LOGIN en AuditLog

##### 2. Login fallido con contraseña incorrecta

**Given** existe usuario activo con username "admin"  
**When** envío POST `/api/auth/login` con contraseña incorrecta  
**Then** sistema responde HTTP 401 Unauthorized  
**And** mensaje: "Credenciales inválidas"  
**And** se incrementa `failedAttempts` del usuario  
**And** se registra intento fallido en AuditLog

##### 3. Renovación de token con refresh token válido

**Given** tengo un refreshToken válido y no revocado  
**When** envío POST `/api/auth/refresh`
```json
{
  "refreshToken": "<token>"
}
```
**Then** sistema responde HTTP 200 OK  
**And** retorna nuevo accessToken y nuevo refreshToken  
**And** refreshToken anterior se marca como `revoked=true`  
**And** no puedo reutilizar el refreshToken anterior (rotation)

##### 4. Intento de renovación con refresh token revocado

**Given** tengo un refreshToken ya usado (`revoked=true`)  
**When** envío POST `/api/auth/refresh` con ese token  
**Then** sistema responde HTTP 401 Unauthorized  
**And** mensaje: "Refresh token inválido o revocado"

##### 5. Logout y revocación de tokens

**Given** estoy autenticado con un refreshToken activo  
**When** envío POST `/api/auth/logout`
```json
{
  "refreshToken": "<token>"
}
```
**Then** sistema responde HTTP 200 OK  
**And** el refreshToken se marca como `revoked=true`  
**And** se registra evento LOGOUT en AuditLog

---

### US-02: Bloqueo de Cuenta por Intentos Fallidos (Prioridad: P0 - Crítica)

**Como** administrador de seguridad  
**Quiero** que el sistema bloquee automáticamente cuentas después de múltiples intentos fallidos  
**Para** prevenir ataques de fuerza bruta

**Why this priority?**: Protección básica de seguridad contra ataques automatizados.

**Independent Test**: Intentar login con contraseña incorrecta 5 veces y verificar que la cuenta se bloquea.

#### Acceptance Scenarios

##### 1. Bloqueo automático tras 5 intentos fallidos

**Given** un usuario activo "vendedor1" con 0 intentos fallidos  
**When** intento login 5 veces consecutivas con contraseña incorrecta  
**Then** en el 5to intento recibo HTTP 403 Forbidden  
**And** mensaje: "Cuenta bloqueada por múltiples intentos fallidos. Contacte al administrador"  
**And** campo `active` cambia a `false`  
**And** `failedAttempts = 5`  
**And** campo `lockedAt` se establece con timestamp actual  
**And** se registra evento ACCOUNT_LOCKED en AuditLog con IP y user agent

##### 2. Reset de intentos fallidos tras login exitoso

**Given** un usuario con 3 intentos fallidos  
**When** hago login exitoso  
**Then** `failedAttempts` se resetea a 0  
**And** `lastLogin` se actualiza

##### 3. Intento de login en cuenta bloqueada

**Given** un usuario con `active=false` por intentos fallidos  
**When** intento login incluso con contraseña correcta  
**Then** recibo HTTP 403 Forbidden  
**And** mensaje: "Cuenta bloqueada. Contacte al administrador"

##### 4. Desbloqueo manual por administrador

**Given** un usuario bloqueado con `active=false`  
**When** un administrador envía PUT `/api/users/{id}/unlock`  
**Then** `active` cambia a `true`  
**And** `failedAttempts` se resetea a 0  
**And** el usuario puede hacer login nuevamente

---

### US-03: Gestión de Usuarios (CRUD) (Prioridad: P0 - Crítica)

**Como** administrador  
**Quiero** crear, consultar, actualizar y eliminar usuarios  
**Para** dar acceso a múltiples personas en la organización

**Why this priority?**: Necesario para gestionar accesos en la organización.

**Independent Test**: Crear usuario, modificar sus datos, consultar información, eliminarlo (soft delete).

#### Acceptance Scenarios

##### 1. Crear nuevo usuario con validaciones

**Given** estoy autenticado como administrador  
**When** envío POST `/api/users`
```json
{
  "username": "vendedor1",
  "email": "vendedor1@empresa.com",
  "password": "Vendedor123!",
  "firstName": "Juan",
  "lastName": "Pérez",
  "documentTypeId": 1,
  "documentNumber": "1234567890",
  "roleIds": [2]
}
```
**Then** sistema responde HTTP 201 Created  
**And** retorna UUID del usuario creado  
**And** password se almacena hasheado con BCrypt  
**And** `active = true` por defecto  
**And** se registra evento USER_CREATED en AuditLog

##### 2. Validación de password débil

**Given** intento crear usuario con password "123456"  
**When** envío POST `/api/users`  
**Then** recibo HTTP 400 Bad Request  
**And** mensaje con violaciones:
```json
{
  "error": "Validation failed",
  "field": "password",
  "violations": [
    "Debe tener mínimo 8 caracteres (actual: 6)",
    "Debe contener al menos 1 letra mayúscula",
    "Debe contener al menos 1 carácter especial (!@#$%^&*)"
  ]
}
```

##### 3. Validación de username duplicado

**Given** existe usuario con username "admin"  
**When** intento crear otro usuario con username "admin"  
**Then** recibo HTTP 409 Conflict  
**And** mensaje: "Username ya existe"

##### 4. Actualización de usuario con auditoría

**Given** existe usuario con id=5 y email="old@mail.com"  
**When** envío PUT `/api/users/5`
```json
{
  "email": "new@mail.com"
}
```
**Then** sistema responde HTTP 200 OK  
**And** retorna usuario actualizado  
**And** se registra en AuditLog: entity=User, entityId=5, action=UPDATE, oldValue={"email":"old@mail.com"}, newValue={"email":"new@mail.com"}

##### 5. Eliminación lógica de usuario

**Given** existe usuario activo con id=10  
**When** envío DELETE `/api/users/10`  
**Then** sistema responde HTTP 204 No Content  
**And** campo `deletedAt` se llena con timestamp actual  
**And** usuario no aparece en listados normales  
**And** no se puede eliminar usuario admin (id=1)

##### 6. Listado de usuarios con paginación

**Given** existen 50 usuarios en el sistema  
**When** envío GET `/api/users?page=0&size=20`  
**Then** recibo HTTP 200 OK con 20 usuarios  
**And** metadata de paginación (totalElements, totalPages, currentPage)

---

### US-04: Gestión de Roles y Permisos Granulares (Prioridad: P1 - Alta)

**Como** administrador  
**Quiero** crear roles personalizados y asignar permisos granulares  
**Para** controlar exactamente qué puede hacer cada usuario

**Why this priority?**: Esencial para empresas con múltiples niveles de responsabilidad.

**Independent Test**: Crear rol "Vendedor Junior", asignar permisos específicos, asignar rol a usuario, verificar que los permisos se aplican.

#### Acceptance Scenarios

##### 1. Crear rol con permisos CRUD básicos

**Given** estoy autenticado como administrador  
**When** envío POST `/api/roles`
```json
{
  "name": "Vendedor",
  "description": "Puede crear y ver ventas",
  "permissionIds": [10, 11]
}
```
**Then** sistema responde HTTP 201 Created  
**And** retorna rol creado  
**And** los permisos quedan asociados en role_permissions

##### 2. Permisos con condiciones (límites de aprobación)

**Given** creo permiso "Aprobar descuentos hasta 15%"
```json
{
  "entity": "Sale",
  "action": "APPROVE_DISCOUNT",
  "condition": "discountPercentage <= 15",
  "description": "Puede aprobar descuentos hasta 15%"
}
```
**When** usuario con este permiso intenta aprobar descuento del 10%  
**Then** la operación se permite y retorna HTTP 200 OK  
**When** intenta aprobar descuento del 20%  
**Then** recibo HTTP 403 Forbidden  
**And** mensaje: "Descuento de 20% excede límite permitido por su rol (15%)"  
**And** se registra evento PERMISSION_DENIED en AuditLog

##### 3. Asignar múltiples roles a usuario

**Given** existen roles "Vendedor" (id=2) y "Bodeguero" (id=3)  
**When** envío POST `/api/users/5/roles`
```json
{
  "roleIds": [2, 3]
}
```
**Then** sistema responde HTTP 200 OK  
**And** el usuario tiene permisos acumulados de ambos roles

##### 4. Validación para eliminar rol con usuarios asignados

**Given** rol "Vendedor" (id=2) tiene 5 usuarios asignados  
**When** envío DELETE `/api/roles/2`  
**Then** recibo HTTP 409 Conflict  
**And** mensaje con detalles:
```json
{
  "error": "Role deletion not allowed",
  "message": "No se puede eliminar rol 'Vendedor' porque tiene 5 usuarios asignados",
  "affectedUserIds": [3, 5, 8, 12, 15],
  "suggestion": "Reasigne los usuarios a otro rol antes de eliminar"
}
```

---

### US-05: Auditoría Completa de Operaciones (Prioridad: P2 - Media)

**Como** auditor/administrador  
**Quiero** consultar un historial completo de todas las operaciones críticas  
**Para** compliance, seguridad y resolución de incidentes

**Why this priority?**: Importante para compliance, pero puede implementarse después de funcionalidad básica.

**Independent Test**: Realizar varias operaciones y verificar que todas quedan registradas en AuditLog con información completa.

#### Acceptance Scenarios

##### 1. Auditoría de login exitoso

**Given** usuario "vendedor1" hace login desde IP 192.168.1.100  
**When** consulto GET `/api/audit-logs?entity=User&action=LOGIN`  
**Then** veo registro con:
- user_id, entity=User, action=LOGIN
- ip_address=192.168.1.100
- user_agent (navegador)
- timestamp

##### 2. Auditoría de cambio de datos (old/new values)

**Given** usuario modifica precio de producto de $100 a $150  
**When** consulto GET `/api/audit-logs?entity=Product&entityId=10`  
**Then** veo registro con:
```json
{
  "action": "UPDATE",
  "entity": "Product",
  "entityId": 10,
  "oldValue": {"price": 100.00},
  "newValue": {"price": 150.00},
  "userId": 5,
  "username": "vendedor1",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "timestamp": "2026-01-10T14:30:00Z"
}
```

##### 3. Filtrado de auditoría por fecha y usuario

**Given** existen 1000 registros de auditoría  
**When** consulto GET `/api/audit-logs?userId=5&startDate=2026-01-01&endDate=2026-01-31`  
**Then** recibo solo registros del usuario 5 en ese rango de fechas  
**And** resultados paginados

##### 4. Auditoría de operaciones fallidas

**Given** usuario intenta eliminar producto sin permiso  
**When** la operación es rechazada con HTTP 403  
**Then** se registra en AuditLog con action=DELETE_FAILED y reason="Insufficient permissions"

---

### US-06: Política de Contraseñas Configurable (Prioridad: P3 - Baja)

**Como** administrador de seguridad  
**Quiero** configurar políticas de contraseña  
**Para** cumplir con estándares de seguridad de la organización

**Why this priority?**: Importante para seguridad, pero puede implementarse después de autenticación básica.

**Independent Test**: Configurar política, intentar cambiar contraseña que no cumple, verificar rechazo. Intentar reutilizar contraseña anterior, verificar rechazo.

#### Acceptance Scenarios

##### 1. Configuración de política de contraseñas

**Given** soy administrador  
**When** configuro política:
```json
{
  "minLength": 10,
  "requireUppercase": true,
  "requireLowercase": true,
  "requireNumber": true,
  "requireSpecialChar": true,
  "passwordHistorySize": 5
}
```
**Then** la política se almacena en configuración

##### 2. Validación de contraseña contra política

**Given** política requiere 10 caracteres mínimo  
**When** usuario intenta cambiar a "Pass123!"  
**Then** recibe error "Password debe tener mínimo 10 caracteres"

##### 3. Validación de historial de contraseñas

**Given** usuario tiene historial de 5 contraseñas anteriores  
**When** intenta cambiar a una de sus últimas 5 contraseñas  
**Then** recibe error "No puede reutilizar las últimas 5 contraseñas"

---

## 📊 Business Rules

### BR-01: Autenticación
- Access token expira en **exactamente 30 minutos** (1800 segundos)
- Refresh token expira en **exactamente 7 días** (604800 segundos)
- Refresh token se invalida al usarse (token rotation)
- Passwords hasheados con BCrypt (cost factor 12)

### BR-02: Bloqueo de Cuenta
- Cuenta se bloquea automáticamente tras **5 intentos fallidos**
- Solo administradores pueden desbloquear cuentas
- Intentos fallidos se resetean tras login exitoso

### BR-03: Usuarios
- Username único, 3-50 caracteres, solo alfanumérico y guión bajo
- Email único, formato válido
- Soft delete (deletedAt)
- Usuario admin (id=1) no se puede eliminar

### BR-04: Roles y Permisos
- Permisos granulares: entity + action + condition
- Múltiples roles por usuario
- Permisos acumulativos
- No se puede eliminar rol con usuarios asignados

### BR-05: Auditoría
- Todas las operaciones críticas se auditan
- Registros inmutables (no se pueden modificar/eliminar)
- Incluye: userId, entity, entityId, action, oldValue, newValue, IP, userAgent, timestamp

### BR-06: Política de Contraseñas
- Longitud mínima: 8 caracteres (configurable)
- Complejidad: mayúsculas, minúsculas, números, caracteres especiales
- Historial: 5 contraseñas anteriores (configurable)

---

## 🎨 API Contracts

### Authentication Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Autenticar usuario | Public |
| POST | `/api/auth/refresh` | Renovar access token | Public |
| POST | `/api/auth/logout` | Cerrar sesión | Public |

### User Management Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/users` | Crear usuario | Admin |
| GET | `/api/users/{id}` | Obtener usuario | User |
| PUT | `/api/users/{id}` | Actualizar usuario | Admin |
| DELETE | `/api/users/{id}` | Eliminar usuario (soft) | Admin |
| PUT | `/api/users/{id}/unlock` | Desbloquear cuenta | Admin |
| PUT | `/api/users/{id}/change-password` | Cambiar contraseña | Owner/Admin |
| GET | `/api/users` | Listar usuarios | Admin |
| POST | `/api/users/{id}/roles` | Asignar roles | Admin |

### Role Management Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/roles` | Crear rol | Admin |
| GET | `/api/roles/{id}` | Obtener rol | Admin |
| PUT | `/api/roles/{id}` | Actualizar rol | Admin |
| DELETE | `/api/roles/{id}` | Eliminar rol | Admin |
| GET | `/api/roles` | Listar roles | Admin |

### Audit Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/audit-logs` | Listar logs con filtros | Admin |
| GET | `/api/audit-logs/{id}` | Obtener log específico | Admin |

---

## 📈 Success Metrics

### Performance Targets

| Operation | Target | Measurement |
|-----------|--------|-------------|
| Login | < 500ms p95 | APM logs |
| Token refresh | < 200ms p95 | APM logs |
| Permission check | < 50ms p95 | APM logs |
| Audit log query | < 1s p95 | APM logs |

### Quality Targets

- **Test Coverage**: >= 85%
- **Security**: 0 passwords en texto plano, JWT firmados correctamente
- **Audit**: 100% operaciones críticas auditadas
- **Performance**: 50 usuarios concurrentes sin degradación

---

## 🚫 Out of Scope

Los siguientes elementos **NO** están incluidos en esta versión:

❌ OAuth2 / Social Login  
❌ Two-Factor Authentication (2FA)  
❌ SSO (Single Sign-On)  
❌ LDAP/Active Directory  
❌ Password recovery via email  
❌ Biometric authentication  
❌ Session management con WebSockets  
❌ API keys para integración externa  
❌ IP whitelist/blacklist  
❌ CAPTCHA en login  

---

## 🔗 Dependencies

### Upstream (Bloqueantes)
- Ninguna (módulo fundacional)

### Downstream (Dependientes)
- **TODOS los módulos** del sistema requieren este módulo para autenticación y autorización

---

## 📝 Notes

- Esta especificación cubre **solo la parte funcional** del módulo
- Ver [technical-spec.md](technical-spec.md) para detalles técnicos de arquitectura, base de datos, y diseño
- Este es un **módulo crítico y bloqueador** - sin él no hay acceso al sistema
- La implementación debe seguir estándares de seguridad OWASP

---

**Status**: ⚠️ PHASE 1 - Draft  
**Next Step**: Review → Clarify → Approve → Move to PHASE 2
