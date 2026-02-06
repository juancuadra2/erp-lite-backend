# Functional Specification: Módulo de Métodos de Pago

**Feature**: Payment Methods Module  
**Created**: February 1, 2026  
**Module Type**: Independent Catalog (Sin dependencias)  
**Phase**: PHASE 1 - Draft

---

## 📋 Overview

El módulo de Métodos de Pago gestiona el catálogo de formas de pago aceptadas en transacciones comerciales (Efectivo, Tarjeta de Crédito, Transferencia Bancaria, etc.). Es un catálogo base independiente sin dependencias de otros módulos, requerido por múltiples módulos del sistema (Sales, Purchases, Expenses) para el registro correcto de pagos y conciliación bancaria.

### Business Value
- Centraliza métodos de pago en un único catálogo reutilizable
- Facilita reportes de ventas/compras por método de pago
- Permite seguimiento de flujo de caja por forma de cobro/pago
- Simplifica conciliación bancaria al identificar origen de fondos
- Habilita configuración de comisiones por método de pago

### Scope
Este módulo gestiona únicamente el catálogo de métodos de pago y sus configuraciones básicas. **No incluye** el procesamiento de pagos con pasarelas externas, la conciliación bancaria automática, ni la gestión de cuentas bancarias (esto es responsabilidad de otros módulos).

---

## 👥 User Stories

### User Story 1: Gestión de Métodos de Pago (Priority: P1)

**Como** administrador del sistema  
**Quiero** gestionar el catálogo de métodos de pago (Efectivo, Tarjeta, Transferencia, etc.)  
**Para** poder registrarlos correctamente en ventas, compras y gastos

**Why this priority?** Es fundamental para el registro correcto de transacciones comerciales y control de flujo de caja.

**Acceptance Criteria:**

1. ✅ Puedo crear métodos de pago con código único y nombre
2. ✅ Puedo configurar si el método requiere referencia de transacción
3. ✅ Puedo especificar si aplica comisión o descuento
4. ✅ Puedo definir el tipo de método (efectivo, electrónico, crédito)
5. ✅ Puedo listar métodos de pago activos con paginación
6. ✅ Puedo buscar métodos de pago por nombre
7. ✅ Puedo actualizar información de métodos de pago existentes
8. ✅ Puedo desactivar/activar métodos de pago (soft delete)
9. ✅ El sistema valida códigos únicos
10. ✅ El sistema previene eliminación si hay transacciones asociadas
11. ✅ Todas las operaciones quedan registradas en auditoría

**Acceptance Scenarios:**

#### Scenario 1.1: Crear método de pago Efectivo
- **Given** estoy autenticado como administrador
- **When** envío POST /api/payment-methods con:
  ```json
  {
    "code": "CASH",
    "name": "Efectivo",
    "type": "CASH",
    "requiresReference": false,
    "hasCommission": false,
    "commissionPercentage": 0.0,
    "description": "Pago en efectivo",
    "enabled": true
  }
  ```
- **Then** recibo status 201 con el método de pago creado
- **And** el método tiene UUID asignado
- **And** se registra en AuditLog: action=PAYMENT_METHOD_CREATED

#### Scenario 1.2: Validación de código único
- **Given** existe método de pago con code "CASH"
- **When** intento crear otro método con code "CASH"
- **Then** recibo status 409 con mensaje "Payment method code already exists"

#### Scenario 1.3: Crear método con comisión (Tarjeta de Crédito)
- **Given** estoy autenticado como administrador
- **When** envío POST /api/payment-methods con:
  ```json
  {
    "code": "CC_VISA",
    "name": "Tarjeta de Crédito Visa",
    "type": "CREDIT_CARD",
    "requiresReference": true,
    "hasCommission": true,
    "commissionPercentage": 2.5,
    "description": "Pago con tarjeta de crédito Visa",
    "enabled": true
  }
  ```
- **Then** recibo status 201 con el método creado
- **And** hasCommission=true y commissionPercentage=2.5000

#### Scenario 1.4: Listar métodos de pago activos
- **Given** existen 10 métodos de pago, 8 activos y 2 inactivos
- **When** envío GET /api/payment-methods?enabled=true&page=0&size=20
- **Then** recibo status 200 con 8 métodos activos
- **And** metadata de paginación correcta
- **And** resultados ordenados alfabéticamente por nombre

#### Scenario 1.5: Búsqueda por nombre
- **Given** existen múltiples métodos de pago
- **When** envío GET /api/payment-methods/search?name=Tarjeta
- **Then** recibo status 200 con métodos cuyo nombre contenga "Tarjeta"
- **And** búsqueda es case-insensitive
- **And** incluye: "Tarjeta de Crédito", "Tarjeta Débito"

#### Scenario 1.6: Actualizar método de pago
- **Given** existe método "CASH" con enabled=true
- **When** envío PUT /api/payment-methods/{uuid} con datos actualizados
- **Then** recibo status 200 con el método actualizado
- **And** se registra en AuditLog: action=PAYMENT_METHOD_UPDATED

#### Scenario 1.7: Desactivar método de pago sin transacciones
- **Given** método "CHECK" activo sin transacciones asociadas
- **When** envío PATCH /api/payment-methods/{uuid}/deactivate
- **Then** recibo status 200 con enabled=false
- **And** no aparece en listados con filtro enabled=true
- **And** se registra en AuditLog: action=PAYMENT_METHOD_DEACTIVATED

#### Scenario 1.8: Validación de eliminación con transacciones
- **Given** método "CASH" tiene 1000 transacciones asociadas
- **When** intento DELETE /api/payment-methods/{uuid}
- **Then** recibo status 409 con mensaje "Cannot delete payment method with associated transactions"

#### Scenario 1.9: Obtener método de pago por UUID
- **Given** existe método con UUID "550e8400-e29b-41d4-a716-446655440000"
- **When** envío GET /api/payment-methods/550e8400-e29b-41d4-a716-446655440000
- **Then** recibo status 200 con detalles completos del método

#### Scenario 1.10: Validación de porcentaje de comisión
- **Given** estoy creando método con hasCommission=true
- **When** envío commissionPercentage=-5.0
- **Then** recibo status 400 con mensaje "Commission percentage must be between 0 and 100"

---

### User Story 2: Filtrado por Tipo de Método (Priority: P2)

**Como** usuario del módulo de ventas  
**Quiero** consultar métodos de pago filtrados por tipo (efectivo, tarjeta, transferencia)  
**Para** mostrar solo opciones relevantes según el contexto

**Why this priority?** Mejora la experiencia de usuario al filtrar opciones según el tipo de transacción.

**Acceptance Criteria:**

1. ✅ Puedo filtrar métodos por tipo (CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CHECK, DIGITAL_WALLET, OTHER)
2. ✅ Los resultados están ordenados alfabéticamente
3. ✅ El filtrado respeta también el estado enabled

**Acceptance Scenarios:**

#### Scenario 2.1: Filtrar métodos de pago en efectivo
- **Given** existen los siguientes métodos:
  - CASH (type=CASH, enabled=true)
  - CC_VISA (type=CREDIT_CARD, enabled=true)
  - TRANSFER (type=BANK_TRANSFER, enabled=true)
  - CHECK (type=CHECK, enabled=false)
- **When** envío GET /api/payment-methods?type=CASH&enabled=true
- **Then** recibo status 200 con 1 método: CASH
- **And** NO incluye CC_VISA ni TRANSFER

#### Scenario 2.2: Filtrar métodos electrónicos (tarjetas)
- **Given** mismos métodos del scenario anterior
- **When** envío GET /api/payment-methods?type=CREDIT_CARD&enabled=true
- **Then** recibo status 200 con 1 método: CC_VISA

---

### User Story 3: Seed Data de Colombia (Priority: P1)

**Como** implementador del sistema  
**Quiero** que el sistema incluya datos iniciales de métodos de pago comunes en Colombia  
**Para** facilitar el despliegue y reducir configuración inicial

**Why this priority?** Reduce tiempo de configuración inicial y errores en setup.

**Acceptance Criteria:**

1. ✅ El sistema incluye migración con datos iniciales
2. ✅ Los datos iniciales son idempotentes
3. ✅ Incluye al menos:
   - Efectivo
   - Tarjeta de Crédito
   - Tarjeta Débito
   - Transferencia Bancaria
   - PSE (Pagos Seguros en Línea)
   - Cheque
   - Crédito (pago diferido)

**Acceptance Scenarios:**

#### Scenario 3.1: Primera instalación con seed data
- **Given** la base de datos está vacía
- **When** se ejecutan las migraciones Flyway
- **Then** la tabla payment_methods contiene 7 registros
- **And** todos están activos (enabled=true)
- **And** los códigos son únicos

#### Scenario 3.2: Re-ejecución es idempotente
- **Given** ya existen los métodos iniciales
- **When** se re-ejecuta la migración de seed data
- **Then** no se crean registros duplicados
- **And** los existentes no se modifican

---

## 🚫 Out of Scope

Lo siguiente NO está incluido en este módulo:

1. **Procesamiento de pagos con pasarelas**: Integración con Stripe, PayU, etc. (módulo Payments)
2. **Conciliación bancaria automática**: Matching de transacciones (módulo Banking)
3. **Gestión de cuentas bancarias**: CRUD de cuentas (módulo Banking)
4. **Gestión de terminales POS**: Configuración de datáfonos (módulo POS)
5. **Gestión de comisiones reales**: Cálculo y registro de comisiones (módulo Accounting)
6. **Reportes financieros**: Reportes complejos de flujo de caja (módulo Reports)
7. **Split payments**: Pagos divididos en múltiples métodos (módulo Sales/Purchases)

---

## 📊 Business Rules

### BR-PM-001: Código Único
- Cada método de pago debe tener un código único en el sistema
- El código es case-sensitive
- Formato recomendado: Letras mayúsculas + guiones bajos (ej: CASH, CC_VISA, BANK_TRANSFER)
- Longitud máxima: 30 caracteres

### BR-PM-002: Comisión Válida
- Si hasCommission=true, commissionPercentage debe estar entre 0.0000 y 100.0000
- Se permite precisión de 4 decimales
- Si hasCommission=false, commissionPercentage debe ser 0.0000

### BR-PM-003: Referencia Requerida
- requiresReference=true indica que el método requiere número de referencia/autorización
- Aplica típicamente para: tarjetas, transferencias, PSE, cheques
- No aplica para: efectivo, crédito directo

### BR-PM-004: Tipo de Método
- Valores permitidos: CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CHECK, DIGITAL_WALLET, OTHER
- El tipo no puede ser NULL
- Ayuda a categorizar métodos para reportes

### BR-PM-005: Soft Delete
- Los métodos de pago se desactivan (enabled=false), no se eliminan físicamente
- Un método desactivado no aparece en listados de activos
- Un método desactivado no puede ser usado en nuevas transacciones
- Las transacciones históricas mantienen referencia al método usado

### BR-PM-006: Validación de Eliminación
- No se puede eliminar (ni desactivar) un método si:
  - Tiene transacciones asociadas (ventas, compras, gastos)
- Se debe mostrar mensaje descriptivo indicando la restricción

### BR-PM-007: Auditoría Obligatoria
- Todas las operaciones (CREATE, UPDATE, DEACTIVATE) se registran en auditoría
- Campos obligatorios: createdBy, createdAt, updatedBy, updatedAt
- Para desactivaciones: deletedBy, deletedAt

---

## 🎯 Success Criteria

Este módulo se considera exitoso cuando:

1. ✅ Se pueden gestionar métodos de pago con CRUD completo
2. ✅ El sistema incluye 7 métodos de pago iniciales para Colombia
3. ✅ Los módulos de Sales, Purchases y Expenses pueden consultar y usar los métodos
4. ✅ Se previene la eliminación de métodos con transacciones
5. ✅ Todas las operaciones tienen auditoría completa
6. ✅ La cobertura de tests es >= 85%
7. ✅ La documentación API está completa
8. ✅ Los tiempos de respuesta cumplen SLA: < 100ms p95

---

## 📚 References

- Medios de pago comunes en Colombia (Superintendencia Financiera)
- PSE - Pagos Seguros en Línea
- Reglamentación de pagos electrónicos

---

## 📝 Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-01 | Development Team | Initial version |
