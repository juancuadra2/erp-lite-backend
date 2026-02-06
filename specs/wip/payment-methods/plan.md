# Implementation Plan: Módulo de Métodos de Pago

**Date**: February 1, 2026  
**Spec**: [functional-spec.md](functional-spec.md) | [technical-spec.md](technical-spec.md)

## Summary

Desarrollo del módulo de catálogo de métodos de pago que gestiona las formas de pago aceptadas en transacciones comerciales (Efectivo, Tarjeta de Crédito, Transferencia, PSE, etc.). Este módulo no tiene dependencias de otros módulos y es requerido por Sales, Purchases y Expenses para registro correcto de pagos. Incluye carga inicial de 7 métodos comunes en Colombia, endpoints REST CRUD completos, configuración de comisiones, y validaciones de integridad.

## Technical Context

**Languages/Versions**: Java 17+  
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, MySQL Connector, Lombok, MapStruct, Hibernate Validator, Flyway  
**Storage**: MySQL 8.0+  
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers  
**Target Platforms**: RESTful API  
**Performance Goals**: List operations < 100ms p95  
**Constraints**: Soft delete obligatorio, auditoría completa, precisión decimal para comisiones  
**Scale/Scope**: Single-tenant, catálogo base sin dependencias, ~10-15 métodos de pago

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/jcuadrado/erplitebackend/
│   │       ├── domain/
│   │       │   └── paymentmethod/
│   │       │       ├── model/
│   │       │       │   ├── PaymentMethod.java
│   │       │       │   └── PaymentMethodType.java
│   │       │       ├── service/
│   │       │       │   ├── PaymentMethodDomainService.java
│   │       │       │   └── PaymentMethodValidationService.java
│   │       │       └── exception/
│   │       │           ├── PaymentMethodNotFoundException.java
│   │       │           ├── DuplicatePaymentMethodCodeException.java
│   │       │           ├── InvalidPaymentMethodCodeException.java
│   │       │           ├── InvalidCommissionException.java
│   │       │           ├── InvalidPaymentMethodDataException.java
│   │       │           └── PaymentMethodConstraintException.java
│   │       │
│   │       ├── application/
│   │       │   ├── port/
│   │       │   │   ├── in/
│   │       │   │   │   └── paymentmethod/
│   │       │   │   │       ├── CreatePaymentMethodUseCase.java
│   │       │   │   │       ├── GetPaymentMethodUseCase.java
│   │       │   │   │       ├── UpdatePaymentMethodUseCase.java
│   │       │   │   │       ├── DeactivatePaymentMethodUseCase.java
│   │       │   │   │       ├── ActivatePaymentMethodUseCase.java
│   │       │   │   │       ├── ListPaymentMethodsUseCase.java
│   │       │   │   │       └── SearchPaymentMethodsUseCase.java
│   │       │   │   └── out/
│   │       │   │       └── PaymentMethodPort.java
│   │       │   └── service/
│   │       │       └── paymentmethod/
│   │       │           ├── CreatePaymentMethodService.java
│   │       │           ├── GetPaymentMethodService.java
│   │       │           ├── UpdatePaymentMethodService.java
│   │       │           ├── DeactivatePaymentMethodService.java
│   │       │           ├── ActivatePaymentMethodService.java
│   │       │           ├── ListPaymentMethodsService.java
│   │       │           └── SearchPaymentMethodsService.java
│   │       │
│   │       └── infrastructure/
│   │           ├── out/
│   │           │   └── paymentmethod/
│   │           │       ├── persistence/
│   │           │       │   ├── entity/
│   │           │       │   │   └── PaymentMethodEntity.java
│   │           │       │   ├── repository/
│   │           │       │   │   └── PaymentMethodJpaRepository.java
│   │           │       │   └── adapter/
│   │           │       │       └── PaymentMethodRepositoryAdapter.java
│   │           │       └── mapper/
│   │           │           └── PaymentMethodEntityMapper.java
│   │           │
│   │           └── in/
│   │               └── api/
│   │                   └── paymentmethod/
│   │                       ├── rest/
│   │                       │   └── PaymentMethodController.java
│   │                       ├── dto/
│   │                       │   ├── CreatePaymentMethodRequestDto.java
│   │                       │   ├── UpdatePaymentMethodRequestDto.java
│   │                       │   └── PaymentMethodResponseDto.java
│   │                       ├── mapper/
│   │                       │   └── PaymentMethodDtoMapper.java
│   │                       └── constant/
│   │                           └── PaymentMethodApiConstants.java
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V1.5__create_payment_methods_table.sql
│               └── V1.6__insert_colombia_payment_methods.sql
│
└── test/
    └── java/
        └── com/jcuadrado/erplitebackend/
            └── paymentmethod/
                ├── domain/
                │   ├── model/
                │   │   └── PaymentMethodTest.java
                │   └── service/
                │       ├── PaymentMethodDomainServiceTest.java
                │       └── PaymentMethodValidationServiceTest.java
                ├── application/
                │   └── service/
                │       ├── CreatePaymentMethodServiceTest.java
                │       ├── GetPaymentMethodServiceTest.java
                │       ├── UpdatePaymentMethodServiceTest.java
                │       ├── DeactivatePaymentMethodServiceTest.java
                │       ├── ListPaymentMethodsServiceTest.java
                │       └── SearchPaymentMethodsServiceTest.java
                └── infrastructure/
                    ├── in/
                    │   └── api/
                    │       └── rest/
                    │           └── PaymentMethodControllerTest.java
                    └── out/
                        └── persistence/
                            ├── repository/
                            │   └── PaymentMethodJpaRepositoryTest.java
                            └── adapter/
                                └── PaymentMethodRepositoryAdapterTest.java
```

---

## Implementation Phases

### Phase 1: Foundation & Domain Models (3 tasks)

- [ ] **T001**: Crear entidad de dominio `PaymentMethod`
  - Atributos: id, uuid, code, name, type, requiresReference, hasCommission, commissionPercentage, description, enabled, createdBy, updatedBy, deletedBy, timestamps
  - Métodos: activate(), deactivate(), requiresTransactionReference(), hasCommissionApplied(), isValidCommissionPercentage(), isElectronic()
  - Usar BigDecimal para commissionPercentage (4 decimales)

- [ ] **T002**: Crear enum `PaymentMethodType`
  - Valores: CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CHECK, DIGITAL_WALLET, OTHER

- [ ] **T003**: Crear excepciones del dominio
  - PaymentMethodNotFoundException, DuplicatePaymentMethodCodeException, InvalidPaymentMethodCodeException, InvalidCommissionException, InvalidPaymentMethodDataException, PaymentMethodConstraintException

### Phase 2: Domain Services (2 tasks)

- [ ] **T004**: Implementar `PaymentMethodDomainService`
  - validateCode(): formato, max 30 chars, uppercase+nums+underscores
  - validateName(): no vacío, max 100 chars
  - validateCommission(): lógica hasCommission vs commissionPercentage, rango 0-100, max 4 decimales
  - canBeDeleted(): validar transacciones asociadas

- [ ] **T005**: Implementar `PaymentMethodValidationService`
  - ensureCodeIsUnique(): validar código único excluyendo UUID opcional
  - Inyectar PaymentMethodPort

### Phase 3: Application Ports (2 tasks)

- [ ] **T006**: Crear Input Ports (Use Cases)
  - CreatePaymentMethodUseCase, GetPaymentMethodUseCase, UpdatePaymentMethodUseCase, DeactivatePaymentMethodUseCase, ActivatePaymentMethodUseCase, ListPaymentMethodsUseCase, SearchPaymentMethodsUseCase

- [ ] **T007**: Crear Output Port (Repository Interface)
  - PaymentMethodPort con métodos: save, findByUuid, findByCode, findAll, findByEnabled, findByEnabledAndType, searchByNameContainingIgnoreCase, existsByCode, existsByCodeAndUuidNot, countTransactionsWithPaymentMethod, deleteByUuid

### Phase 4: Application Services (7 tasks)

- [ ] **T008**: Implementar `CreatePaymentMethodService`
  - Validar código, nombre y comisión
  - Validar unicidad de código
  - Crear PaymentMethod con UUID
  - Persistir, mapear a DTO
  - TODO: AuditLog

- [ ] **T009**: Implementar `GetPaymentMethodService`
- [ ] **T010**: Implementar `UpdatePaymentMethodService`
- [ ] **T011**: Implementar `DeactivatePaymentMethodService`
  - Validar que no haya transacciones asociadas
- [ ] **T012**: Implementar `ActivatePaymentMethodService`
- [ ] **T013**: Implementar `ListPaymentMethodsService`
  - Filtros: enabled, type
  - Paginación, ordenar por nombre
- [ ] **T014**: Implementar `SearchPaymentMethodsService`

### Phase 5: Infrastructure - Persistence (4 tasks)

- [ ] **T015**: Crear `PaymentMethodEntity`
  - Anotaciones JPA, índices
  - @PrePersist, @PreUpdate
- [ ] **T016**: Crear `PaymentMethodJpaRepository`
  - Métodos custom según spec
  - Queries: countSalesWithPaymentMethod, countPurchasesWithPaymentMethod
- [ ] **T017**: Crear `PaymentMethodEntityMapper` (MapStruct)
- [ ] **T018**: Implementar `PaymentMethodRepositoryAdapter`

### Phase 6: Infrastructure - API REST (4 tasks)

- [ ] **T019**: Crear DTOs con validaciones Jakarta
- [ ] **T020**: Crear `PaymentMethodDtoMapper` (MapStruct)
- [ ] **T021**: Crear `PaymentMethodApiConstants`
- [ ] **T022**: Implementar `PaymentMethodController`
  - 7 endpoints REST
  - Documentación Swagger

### Phase 7: Database Migrations (2 tasks)

- [ ] **T023**: Crear migración `V1.5__create_payment_methods_table.sql`
  - Tabla con índices, DECIMAL(7,4) para commission_percentage
- [ ] **T024**: Crear migración `V1.6__insert_colombia_payment_methods.sql`
  - INSERT de 7 métodos de pago
  - ON DUPLICATE KEY UPDATE para idempotencia

### Phase 8: Testing - Unit Tests (9 tasks)

- [ ] **T025**: Test de `PaymentMethod` domain model
- [ ] **T026**: Test de `PaymentMethodDomainService`
- [ ] **T027**: Test de `PaymentMethodValidationService`
- [ ] **T028**: Test de `CreatePaymentMethodService`
- [ ] **T029**: Test de `GetPaymentMethodService`
- [ ] **T030**: Test de `UpdatePaymentMethodService`
- [ ] **T031**: Test de `DeactivatePaymentMethodService`
- [ ] **T032**: Test de `ListPaymentMethodsService`
- [ ] **T033**: Test de `SearchPaymentMethodsService`

### Phase 9: Testing - Integration Tests (3 tasks)

- [ ] **T034**: Test de `PaymentMethodJpaRepository` (Testcontainers)
- [ ] **T035**: Test de `PaymentMethodRepositoryAdapter`
- [ ] **T036**: Test de `PaymentMethodController` (MockMvc)

### Phase 10: Documentation & Review (2 tasks)

- [ ] **T037**: Actualizar documentación Swagger
- [ ] **T038**: Code Review y Cleanup
  - Verificar cobertura >= 85%
  - Actualizar STATUS.md

---

## Dependencies

### Internal Dependencies
- **None**: Módulo independiente

### External Dependencies
- Spring Boot Starter Web, Data JPA
- MySQL Connector
- Lombok, MapStruct
- Hibernate Validator, Flyway Core
- JUnit 5, Mockito, Testcontainers

---

## Risks & Mitigations

### Risk 1: Cambios en Comisiones
**Impact**: Medio  
**Mitigation**: Auditoría completa, histórico de cambios

### Risk 2: Eliminación con Transacciones
**Impact**: Medio  
**Mitigation**: Validación estricta, soft delete por defecto

### Risk 3: Precisión Decimal en Comisiones
**Impact**: Medio  
**Mitigation**: BigDecimal con 4 decimales, validaciones

---

## Estimation

### Time Estimates
- Phase 1-10: **27 story points** (~27 horas / ~3.5 días)

### Timeline
- Start: Pending approval
- End: ~1 semana después de aprobaciones

---

## Success Criteria

- [ ] 38 tareas completadas
- [ ] Cobertura >= 85%
- [ ] Tests pasando
- [ ] API documentada
- [ ] Seed data cargado
- [ ] Performance < 100ms p95
- [ ] Code review aprobado

---

## Definition of Done

- [ ] Código implementado
- [ ] Tests >= 85%
- [ ] Migraciones ejecutadas
- [ ] Swagger completo
- [ ] Code review aprobado
- [ ] STATUS.md actualizado

---

## References

- [Functional Spec](functional-spec.md)
- [Technical Spec](technical-spec.md)
- [Framework SDD](../../framework/proyecto-framework-sdd.md)
