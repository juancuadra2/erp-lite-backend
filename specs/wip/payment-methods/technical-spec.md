# Technical Specification: Módulo de Métodos de Pago

**Feature**: Payment Methods Module  
**Created**: February 1, 2026  
**Related**: [functional-spec.md](functional-spec.md)  
**Phase**: PHASE 2 - Draft

---

## 📐 Technical Overview

Implementación del módulo de Métodos de Pago siguiendo **Arquitectura Hexagonal** (Ports & Adapters), con separación estricta entre dominio, aplicación e infraestructura. El módulo gestiona el catálogo de formas de pago aceptadas en transacciones comerciales sin dependencias externas.

### Architecture Style
- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Layers**: Domain → Application → Infrastructure
- **Communication**: Inbound/Outbound Ports
- **Database**: MySQL 8.0+ con JPA/Hibernate
- **API Style**: RESTful

---

## 🏗️ Architecture Layers

### Domain Layer (Core Business Logic)

**Purpose**: Lógica de negocio pura, sin dependencias externas

**Components**:

#### Models (`domain/paymentmethod/model/`)
```java
// PaymentMethod.java - Aggregate Root
package com.jcuadrado.erplitebackend.domain.paymentmethod.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    private Long id;
    private UUID uuid;
    private String code;                    // Unique, max 30 chars
    private String name;                    // max 100 chars
    private PaymentMethodType type;         // CASH, CREDIT_CARD, etc.
    private Boolean requiresReference;      // true=requiere número de referencia
    private Boolean hasCommission;          // true=aplica comisión
    private BigDecimal commissionPercentage; // 0.0000 to 100.0000, 4 decimals
    private String description;             // max 500 chars
    private Boolean enabled;
    private User createdBy;
    private User updatedBy;
    private User deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // Business methods
    public void activate() {
        this.enabled = true;
        this.deletedBy = null;
        this.deletedAt = null;
    }
    
    public void deactivate(User user) {
        this.enabled = false;
        this.deletedBy = user;
        this.deletedAt = LocalDateTime.now();
    }
    
    public boolean requiresTransactionReference() {
        return requiresReference != null && requiresReference;
    }
    
    public boolean hasCommissionApplied() {
        return hasCommission != null && hasCommission && 
               commissionPercentage != null && 
               commissionPercentage.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isValidCommissionPercentage() {
        if (hasCommission != null && hasCommission) {
            return commissionPercentage != null && 
                   commissionPercentage.compareTo(BigDecimal.ZERO) >= 0 && 
                   commissionPercentage.compareTo(new BigDecimal("100.0000")) <= 0;
        }
        return commissionPercentage == null || 
               commissionPercentage.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isElectronic() {
        return type == PaymentMethodType.CREDIT_CARD || 
               type == PaymentMethodType.DEBIT_CARD || 
               type == PaymentMethodType.BANK_TRANSFER ||
               type == PaymentMethodType.DIGITAL_WALLET;
    }
}

// PaymentMethodType.java - Enum
package com.jcuadrado.erplitebackend.domain.paymentmethod.model;

public enum PaymentMethodType {
    CASH,               // Efectivo
    CREDIT_CARD,        // Tarjeta de Crédito
    DEBIT_CARD,         // Tarjeta Débito
    BANK_TRANSFER,      // Transferencia Bancaria
    CHECK,              // Cheque
    DIGITAL_WALLET,     // Billetera Digital (PSE, Nequi, etc.)
    OTHER               // Otros
}
```

#### Services (`domain/paymentmethod/service/`)
```java
// PaymentMethodDomainService.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.service;

import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.paymentmethod.exception.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PaymentMethodDomainService {
    
    /**
     * Valida el formato del código
     */
    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidPaymentMethodCodeException("Payment method code cannot be empty");
        }
        if (code.length() > 30) {
            throw new InvalidPaymentMethodCodeException("Payment method code cannot exceed 30 characters");
        }
        // Código debe contener solo letras mayúsculas, números y guiones bajos
        if (!code.matches("^[A-Z0-9_]+$")) {
            throw new InvalidPaymentMethodCodeException(
                "Payment method code must contain only uppercase letters, numbers, and underscores"
            );
        }
    }
    
    /**
     * Valida el nombre
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidPaymentMethodDataException("Payment method name cannot be empty");
        }
        if (name.length() > 100) {
            throw new InvalidPaymentMethodDataException("Payment method name cannot exceed 100 characters");
        }
    }
    
    /**
     * Valida la comisión
     */
    public void validateCommission(Boolean hasCommission, BigDecimal commissionPercentage) {
        if (hasCommission != null && hasCommission) {
            if (commissionPercentage == null) {
                throw new InvalidCommissionException("Commission percentage is required when hasCommission is true");
            }
            if (commissionPercentage.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidCommissionException("Commission percentage cannot be negative");
            }
            if (commissionPercentage.compareTo(new BigDecimal("100.0000")) > 0) {
                throw new InvalidCommissionException("Commission percentage cannot exceed 100");
            }
            if (commissionPercentage.scale() > 4) {
                throw new InvalidCommissionException("Commission percentage cannot have more than 4 decimal places");
            }
        } else {
            // Si hasCommission es false, la comisión debe ser 0 o null
            if (commissionPercentage != null && commissionPercentage.compareTo(BigDecimal.ZERO) != 0) {
                throw new InvalidCommissionException("Commission percentage must be 0 when hasCommission is false");
            }
        }
    }
    
    /**
     * Determina si puede ser eliminado
     */
    public boolean canBeDeleted(PaymentMethod paymentMethod, long transactionsCount) {
        return transactionsCount == 0;
    }
}

// PaymentMethodValidationService.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.service;

import com.jcuadrado.erplitebackend.domain.paymentmethod.exception.DuplicatePaymentMethodCodeException;
import com.jcuadrado.erplitebackend.application.port.out.PaymentMethodPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodValidationService {
    
    private final PaymentMethodPort paymentMethodPort;
    
    /**
     * Asegura que el código sea único
     */
    public void ensureCodeIsUnique(String code, UUID excludeUuid) {
        boolean exists;
        if (excludeUuid != null) {
            exists = paymentMethodPort.existsByCodeAndUuidNot(code, excludeUuid);
        } else {
            exists = paymentMethodPort.existsByCode(code);
        }
        
        if (exists) {
            throw new DuplicatePaymentMethodCodeException(
                "Payment method with code '" + code + "' already exists"
            );
        }
    }
}
```

#### Exceptions (`domain/paymentmethod/exception/`)
```java
// PaymentMethodNotFoundException.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.DomainException;

public class PaymentMethodNotFoundException extends DomainException {
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
}

// DuplicatePaymentMethodCodeException.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class DuplicatePaymentMethodCodeException extends BusinessRuleException {
    public DuplicatePaymentMethodCodeException(String message) {
        super(message);
    }
}

// InvalidPaymentMethodCodeException.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class InvalidPaymentMethodCodeException extends BusinessRuleException {
    public InvalidPaymentMethodCodeException(String message) {
        super(message);
    }
}

// InvalidCommissionException.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class InvalidCommissionException extends BusinessRuleException {
    public InvalidCommissionException(String message) {
        super(message);
    }
}

// InvalidPaymentMethodDataException.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class InvalidPaymentMethodDataException extends BusinessRuleException {
    public InvalidPaymentMethodDataException(String message) {
        super(message);
    }
}

// PaymentMethodConstraintException.java
package com.jcuadrado.erplitebackend.domain.paymentmethod.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class PaymentMethodConstraintException extends BusinessRuleException {
    public PaymentMethodConstraintException(String message) {
        super(message);
    }
}
```

---

### Application Layer (Use Cases)

**Purpose**: Orquestación de casos de uso, lógica de aplicación

**Ports** (`application/port/`):

#### Input Ports (Use Cases)
```java
// CreatePaymentMethodUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.CreatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.PaymentMethodResponseDto;

public interface CreatePaymentMethodUseCase {
    PaymentMethodResponseDto execute(CreatePaymentMethodRequestDto request);
}

// GetPaymentMethodUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.PaymentMethodResponseDto;
import java.util.UUID;

public interface GetPaymentMethodUseCase {
    PaymentMethodResponseDto execute(UUID uuid);
}

// UpdatePaymentMethodUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.UpdatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.PaymentMethodResponseDto;
import java.util.UUID;

public interface UpdatePaymentMethodUseCase {
    PaymentMethodResponseDto execute(UUID uuid, UpdatePaymentMethodRequestDto request);
}

// DeactivatePaymentMethodUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import java.util.UUID;

public interface DeactivatePaymentMethodUseCase {
    void execute(UUID uuid);
}

// ActivatePaymentMethodUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import java.util.UUID;

public interface ActivatePaymentMethodUseCase {
    void execute(UUID uuid);
}

// ListPaymentMethodsUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.PaymentMethodResponseDto;
import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListPaymentMethodsUseCase {
    Page<PaymentMethodResponseDto> execute(
        Boolean enabled, 
        PaymentMethodType type, 
        Pageable pageable
    );
}

// SearchPaymentMethodsUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.PaymentMethodResponseDto;
import java.util.List;

public interface SearchPaymentMethodsUseCase {
    List<PaymentMethodResponseDto> execute(String searchTerm);
}
```

#### Output Ports (Repository Interfaces)
```java
// PaymentMethodPort.java
package com.jcuadrado.erplitebackend.application.port.out;

import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodPort {
    PaymentMethod save(PaymentMethod paymentMethod);
    Optional<PaymentMethod> findByUuid(UUID uuid);
    Optional<PaymentMethod> findByCode(String code);
    Page<PaymentMethod> findAll(Pageable pageable);
    Page<PaymentMethod> findByEnabled(Boolean enabled, Pageable pageable);
    Page<PaymentMethod> findByEnabledAndType(Boolean enabled, PaymentMethodType type, Pageable pageable);
    List<PaymentMethod> searchByNameContainingIgnoreCase(String searchTerm);
    boolean existsByCode(String code);
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    long countTransactionsWithPaymentMethod(UUID paymentMethodUuid);
    void deleteByUuid(UUID uuid);
}
```

---

### Infrastructure Layer

#### Persistence (Output Adapters)

**Entity** (`infrastructure/out/paymentmethod/persistence/entity/`)
```java
// PaymentMethodEntity.java
package com.jcuadrado.erplitebackend.infrastructure.out.paymentmethod.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_methods", indexes = {
    @Index(name = "idx_payment_method_uuid", columnList = "uuid", unique = true),
    @Index(name = "idx_payment_method_code", columnList = "code", unique = true),
    @Index(name = "idx_payment_method_enabled", columnList = "enabled"),
    @Index(name = "idx_payment_method_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 36)
    private UUID uuid;
    
    @Column(nullable = false, unique = true, length = 30)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethodType type;
    
    @Column(name = "requires_reference", nullable = false)
    private Boolean requiresReference;
    
    @Column(name = "has_commission", nullable = false)
    private Boolean hasCommission;
    
    @Column(name = "commission_percentage", precision = 7, scale = 4)
    private BigDecimal commissionPercentage;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "deleted_by")
    private Long deletedBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Repository** (`infrastructure/out/paymentmethod/persistence/repository/`)
```java
// PaymentMethodJpaRepository.java
package com.jcuadrado.erplitebackend.infrastructure.out.paymentmethod.persistence.repository;

import com.jcuadrado.erplitebackend.infrastructure.out.paymentmethod.persistence.entity.PaymentMethodEntity;
import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodEntity, Long> {
    
    Optional<PaymentMethodEntity> findByUuid(UUID uuid);
    
    Optional<PaymentMethodEntity> findByCode(String code);
    
    Page<PaymentMethodEntity> findByEnabled(Boolean enabled, Pageable pageable);
    
    Page<PaymentMethodEntity> findByEnabledAndType(Boolean enabled, PaymentMethodType type, Pageable pageable);
    
    List<PaymentMethodEntity> findByNameContainingIgnoreCaseAndEnabled(String name, Boolean enabled);
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    
    @Query("SELECT COUNT(s) FROM SaleEntity s WHERE s.paymentMethod.uuid = :paymentMethodUuid")
    long countSalesWithPaymentMethod(@Param("paymentMethodUuid") UUID paymentMethodUuid);
    
    @Query("SELECT COUNT(p) FROM PurchaseEntity p WHERE p.paymentMethod.uuid = :paymentMethodUuid")
    long countPurchasesWithPaymentMethod(@Param("paymentMethodUuid") UUID paymentMethodUuid);
}
```

**Adapter** (`infrastructure/out/paymentmethod/persistence/adapter/`)
```java
// PaymentMethodRepositoryAdapter.java
package com.jcuadrado.erplitebackend.infrastructure.out.paymentmethod.persistence.adapter;

import com.jcuadrado.erplitebackend.application.port.out.PaymentMethodPort;
import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethodType;
import com.jcuadrado.erplitebackend.infrastructure.out.paymentmethod.persistence.entity.PaymentMethodEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.paymentmethod.persistence.repository.PaymentMethodJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.paymentmethod.mapper.PaymentMethodEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentMethodRepositoryAdapter implements PaymentMethodPort {
    
    private final PaymentMethodJpaRepository jpaRepository;
    private final PaymentMethodEntityMapper entityMapper;
    
    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) {
        PaymentMethodEntity entity = entityMapper.toEntity(paymentMethod);
        PaymentMethodEntity saved = jpaRepository.save(entity);
        return entityMapper.toDomain(saved);
    }
    
    @Override
    public Optional<PaymentMethod> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Optional<PaymentMethod> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Page<PaymentMethod> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Page<PaymentMethod> findByEnabled(Boolean enabled, Pageable pageable) {
        return jpaRepository.findByEnabled(enabled, pageable)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Page<PaymentMethod> findByEnabledAndType(Boolean enabled, PaymentMethodType type, Pageable pageable) {
        return jpaRepository.findByEnabledAndType(enabled, type, pageable)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public List<PaymentMethod> searchByNameContainingIgnoreCase(String searchTerm) {
        return jpaRepository.findByNameContainingIgnoreCaseAndEnabled(searchTerm, true).stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }
    
    @Override
    public boolean existsByCodeAndUuidNot(String code, UUID uuid) {
        return jpaRepository.existsByCodeAndUuidNot(code, uuid);
    }
    
    @Override
    public long countTransactionsWithPaymentMethod(UUID paymentMethodUuid) {
        long salesCount = jpaRepository.countSalesWithPaymentMethod(paymentMethodUuid);
        long purchasesCount = jpaRepository.countPurchasesWithPaymentMethod(paymentMethodUuid);
        return salesCount + purchasesCount;
    }
    
    @Override
    public void deleteByUuid(UUID uuid) {
        jpaRepository.findByUuid(uuid)
                .ifPresent(jpaRepository::delete);
    }
}
```

#### REST API (Input Adapters)

**Controller** (`infrastructure/in/api/paymentmethod/rest/`)
```java
// PaymentMethodController.java
package com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.rest;

import com.jcuadrado.erplitebackend.application.port.in.paymentmethod.*;
import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethodType;
import com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Methods", description = "Payment Methods Management API")
public class PaymentMethodController {
    
    private final CreatePaymentMethodUseCase createPaymentMethodUseCase;
    private final GetPaymentMethodUseCase getPaymentMethodUseCase;
    private final UpdatePaymentMethodUseCase updatePaymentMethodUseCase;
    private final DeactivatePaymentMethodUseCase deactivatePaymentMethodUseCase;
    private final ActivatePaymentMethodUseCase activatePaymentMethodUseCase;
    private final ListPaymentMethodsUseCase listPaymentMethodsUseCase;
    private final SearchPaymentMethodsUseCase searchPaymentMethodsUseCase;
    
    @PostMapping
    @Operation(summary = "Create new payment method")
    public ResponseEntity<PaymentMethodResponseDto> create(
            @Valid @RequestBody CreatePaymentMethodRequestDto request) {
        PaymentMethodResponseDto response = createPaymentMethodUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{uuid}")
    @Operation(summary = "Get payment method by UUID")
    public ResponseEntity<PaymentMethodResponseDto> getByUuid(@PathVariable UUID uuid) {
        PaymentMethodResponseDto response = getPaymentMethodUseCase.execute(uuid);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{uuid}")
    @Operation(summary = "Update payment method")
    public ResponseEntity<PaymentMethodResponseDto> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdatePaymentMethodRequestDto request) {
        PaymentMethodResponseDto response = updatePaymentMethodUseCase.execute(uuid, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{uuid}/deactivate")
    @Operation(summary = "Deactivate payment method (soft delete)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        deactivatePaymentMethodUseCase.execute(uuid);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{uuid}/activate")
    @Operation(summary = "Activate payment method")
    public ResponseEntity<Void> activate(@PathVariable UUID uuid) {
        activatePaymentMethodUseCase.execute(uuid);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "List payment methods with filters and pagination")
    public ResponseEntity<Page<PaymentMethodResponseDto>> list(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) PaymentMethodType type,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PaymentMethodResponseDto> response = listPaymentMethodsUseCase.execute(
            enabled, type, pageable
        );
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search payment methods by name")
    public ResponseEntity<List<PaymentMethodResponseDto>> search(
            @RequestParam String name) {
        List<PaymentMethodResponseDto> response = searchPaymentMethodsUseCase.execute(name);
        return ResponseEntity.ok(response);
    }
}
```

**DTOs** (`infrastructure/in/api/paymentmethod/dto/`)
```java
// CreatePaymentMethodRequestDto.java
package com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto;

import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethodType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentMethodRequestDto {
    
    @NotBlank(message = "Code is required")
    @Size(max = 30, message = "Code cannot exceed 30 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code must contain only uppercase letters, numbers, and underscores")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Type is required")
    private PaymentMethodType type;
    
    @NotNull(message = "requiresReference is required")
    private Boolean requiresReference;
    
    @NotNull(message = "hasCommission is required")
    private Boolean hasCommission;
    
    @DecimalMin(value = "0.0", message = "Commission percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Commission percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 4, message = "Commission percentage must have at most 3 integer digits and 4 decimal digits")
    private BigDecimal commissionPercentage;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}

// UpdatePaymentMethodRequestDto.java - Similar structure

// PaymentMethodResponseDto.java
package com.jcuadrado.erplitebackend.infrastructure.in.api.paymentmethod.dto;

import com.jcuadrado.erplitebackend.domain.paymentmethod.model.PaymentMethodType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponseDto {
    private UUID uuid;
    private String code;
    private String name;
    private PaymentMethodType type;
    private Boolean requiresReference;
    private Boolean hasCommission;
    private BigDecimal commissionPercentage;
    private String description;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 🗄️ Database Schema

### DDL Script (Flyway Migration)
```sql
-- V1.5__create_payment_methods_table.sql

CREATE TABLE payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    requires_reference BOOLEAN NOT NULL DEFAULT FALSE,
    has_commission BOOLEAN NOT NULL DEFAULT FALSE,
    commission_percentage DECIMAL(7,4) DEFAULT 0.0000,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    INDEX idx_payment_method_uuid (uuid),
    INDEX idx_payment_method_code (code),
    INDEX idx_payment_method_enabled (enabled),
    INDEX idx_payment_method_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE payment_methods COMMENT = 'Catálogo de métodos de pago';
```

### Seed Data (Flyway Migration)
```sql
-- V1.6__insert_colombia_payment_methods.sql

INSERT INTO payment_methods (uuid, code, name, type, requires_reference, has_commission, commission_percentage, description, enabled, created_at, updated_at)
VALUES
    (UUID(), 'CASH', 'Efectivo', 'CASH', FALSE, FALSE, 0.0000, 'Pago en efectivo', TRUE, NOW(), NOW()),
    (UUID(), 'CC', 'Tarjeta de Crédito', 'CREDIT_CARD', TRUE, TRUE, 2.5000, 'Pago con tarjeta de crédito', TRUE, NOW(), NOW()),
    (UUID(), 'DC', 'Tarjeta Débito', 'DEBIT_CARD', TRUE, TRUE, 1.5000, 'Pago con tarjeta débito', TRUE, NOW(), NOW()),
    (UUID(), 'TRANSFER', 'Transferencia Bancaria', 'BANK_TRANSFER', TRUE, FALSE, 0.0000, 'Transferencia bancaria', TRUE, NOW(), NOW()),
    (UUID(), 'PSE', 'PSE', 'DIGITAL_WALLET', TRUE, TRUE, 1.0000, 'Pagos Seguros en Línea', TRUE, NOW(), NOW()),
    (UUID(), 'CHECK', 'Cheque', 'CHECK', TRUE, FALSE, 0.0000, 'Pago con cheque', TRUE, NOW(), NOW()),
    (UUID(), 'CREDIT', 'Crédito', 'OTHER', FALSE, FALSE, 0.0000, 'Pago diferido (crédito)', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    type = VALUES(type),
    requires_reference = VALUES(requires_reference),
    has_commission = VALUES(has_commission),
    commission_percentage = VALUES(commission_percentage),
    description = VALUES(description),
    updated_at = NOW();
```

---

## 🔗 API Endpoints

### POST /api/payment-methods
Create new payment method

### GET /api/payment-methods/{uuid}
Get payment method by UUID

### PUT /api/payment-methods/{uuid}
Update payment method

### PATCH /api/payment-methods/{uuid}/deactivate
Deactivate payment method

### PATCH /api/payment-methods/{uuid}/activate
Activate payment method

### GET /api/payment-methods
List payment methods with filters

**Query Parameters:**
- `enabled` (optional)
- `type` (optional)
- `page`, `size`, `sort`

### GET /api/payment-methods/search
Search by name

---

## 🧪 Testing Strategy

### Unit Tests
- Domain model tests
- Domain service tests
- Use case tests with mocked ports

### Integration Tests
- Repository tests with Testcontainers
- Controller tests with MockMvc

### Test Coverage Target
- Overall: >= 85%
- Domain layer: >= 95%
- Application layer: >= 90%

---

## 📝 Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-01 | Development Team | Initial version |
