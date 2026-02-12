# Technical Specification: Módulo de Tipos de Impuestos

**Feature**: Tax Types Module  
**Created**: February 1, 2026  
**Related**: [functional-spec.md](functional-spec.md)  
**Phase**: PHASE 2 - Draft

---

## 📐 Technical Overview

Implementación del módulo de Tipos de Impuestos siguiendo **Arquitectura Hexagonal** (Ports & Adapters), con separación estricta entre dominio, aplicación e infraestructura. El módulo gestiona el catálogo de tipos de impuestos aplicables en transacciones comerciales sin dependencias externas.

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

#### Models (`domain/taxtype/model/`)
```java
// TaxType.java - Aggregate Root
package com.jcuadrado.erplitebackend.domain.taxtype.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxType {
    private Long id;
    private UUID uuid;
    private String code;                    // Unique, max 20 chars
    private String name;                    // max 100 chars
    private BigDecimal percentage;          // 0.0000 to 100.0000, 4 decimals
    private Boolean isIncluded;             // true=incluido, false=adicional
    private TaxApplicationType applicationType;  // SALE, PURCHASE, BOTH
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
    
    public boolean isApplicableForSales() {
        return applicationType == TaxApplicationType.SALE || 
               applicationType == TaxApplicationType.BOTH;
    }
    
    public boolean isApplicableForPurchases() {
        return applicationType == TaxApplicationType.PURCHASE || 
               applicationType == TaxApplicationType.BOTH;
    }
    
    public boolean isValidPercentage() {
        return percentage != null && 
               percentage.compareTo(BigDecimal.ZERO) >= 0 && 
               percentage.compareTo(new BigDecimal("100.0000")) <= 0;
    }
}

// TaxApplicationType.java - Enum
package com.jcuadrado.erplitebackend.domain.taxtype.model;

public enum TaxApplicationType {
    SALE,        // Solo para ventas
    PURCHASE,    // Solo para compras
    BOTH         // Aplicable a ambos
}
```

#### Services (`domain/taxtype/service/`)
```java
// TaxTypeDomainService.java
package com.jcuadrado.erplitebackend.domain.taxtype.service;

import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxType;
import com.jcuadrado.erplitebackend.domain.taxtype.exception.*;
import org.springframework.stereotype.Service;

@Service
public class TaxTypeDomainService {
    
    /**
     * Valida el formato del código de tipo de impuesto
     */
    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidTaxTypeCodeException("Tax type code cannot be empty");
        }
        if (code.length() > 20) {
            throw new InvalidTaxTypeCodeException("Tax type code cannot exceed 20 characters");
        }
        // Código debe contener solo letras, números, puntos y guiones
        if (!code.matches("^[A-Z0-9._-]+$")) {
            throw new InvalidTaxTypeCodeException(
                "Tax type code must contain only uppercase letters, numbers, dots, and hyphens"
            );
        }
    }
    
    /**
     * Valida el porcentaje
     */
    public void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new InvalidTaxPercentageException("Tax percentage cannot be null");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTaxPercentageException("Tax percentage cannot be negative");
        }
        if (percentage.compareTo(new BigDecimal("100.0000")) > 0) {
            throw new InvalidTaxPercentageException("Tax percentage cannot exceed 100");
        }
        // Validar máximo 4 decimales
        if (percentage.scale() > 4) {
            throw new InvalidTaxPercentageException("Tax percentage cannot have more than 4 decimal places");
        }
    }
    
    /**
     * Valida que el nombre no esté vacío
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidTaxTypeDataException("Tax type name cannot be empty");
        }
        if (name.length() > 100) {
            throw new InvalidTaxTypeDataException("Tax type name cannot exceed 100 characters");
        }
    }
    
    /**
     * Determina si un tipo de impuesto puede ser eliminado
     */
    public boolean canBeDeleted(TaxType taxType, long associatedProductsCount) {
        return associatedProductsCount == 0;
    }
}

// TaxTypeValidationService.java
package com.jcuadrado.erplitebackend.domain.taxtype.service;

import com.jcuadrado.erplitebackend.domain.taxtype.exception.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.application.port.out.TaxTypePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxTypeValidationService {
    
    private final TaxTypePort taxTypePort;
    
    /**
     * Asegura que el código sea único, excluyendo opcionalmente un UUID
     */
    public void ensureCodeIsUnique(String code, UUID excludeUuid) {
        boolean exists;
        if (excludeUuid != null) {
            exists = taxTypePort.existsByCodeAndUuidNot(code, excludeUuid);
        } else {
            exists = taxTypePort.existsByCode(code);
        }
        
        if (exists) {
            throw new DuplicateTaxTypeCodeException(
                "Tax type with code '" + code + "' already exists"
            );
        }
    }
}
```

#### Exceptions (`domain/taxtype/exception/`)
```java
// TaxTypeNotFoundException.java
package com.jcuadrado.erplitebackend.domain.taxtype.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.DomainException;

public class TaxTypeNotFoundException extends DomainException {
    public TaxTypeNotFoundException(String message) {
        super(message);
    }
}

// DuplicateTaxTypeCodeException.java
package com.jcuadrado.erplitebackend.domain.taxtype.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class DuplicateTaxTypeCodeException extends BusinessRuleException {
    public DuplicateTaxTypeCodeException(String message) {
        super(message);
    }
}

// InvalidTaxTypeCodeException.java
package com.jcuadrado.erplitebackend.domain.taxtype.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class InvalidTaxTypeCodeException extends BusinessRuleException {
    public InvalidTaxTypeCodeException(String message) {
        super(message);
    }
}

// InvalidTaxPercentageException.java
package com.jcuadrado.erplitebackend.domain.taxtype.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class InvalidTaxPercentageException extends BusinessRuleException {
    public InvalidTaxPercentageException(String message) {
        super(message);
    }
}

// InvalidTaxTypeDataException.java
package com.jcuadrado.erplitebackend.domain.taxtype.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class InvalidTaxTypeDataException extends BusinessRuleException {
    public InvalidTaxTypeDataException(String message) {
        super(message);
    }
}

// TaxTypeConstraintException.java
package com.jcuadrado.erplitebackend.domain.taxtype.exception;

import com.jcuadrado.erplitebackend.domain.common.exception.BusinessRuleException;

public class TaxTypeConstraintException extends BusinessRuleException {
    public TaxTypeConstraintException(String message) {
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
// CreateTaxTypeUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.CreateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.TaxTypeResponseDto;

public interface CreateTaxTypeUseCase {
    TaxTypeResponseDto execute(CreateTaxTypeRequestDto request);
}

// GetTaxTypeUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.TaxTypeResponseDto;
import java.util.UUID;

public interface GetTaxTypeUseCase {
    TaxTypeResponseDto execute(UUID uuid);
}

// UpdateTaxTypeUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.UpdateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.TaxTypeResponseDto;
import java.util.UUID;

public interface UpdateTaxTypeUseCase {
    TaxTypeResponseDto execute(UUID uuid, UpdateTaxTypeRequestDto request);
}

// DeactivateTaxTypeUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import java.util.UUID;

public interface DeactivateTaxTypeUseCase {
    void execute(UUID uuid);
}

// ActivateTaxTypeUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import java.util.UUID;

public interface ActivateTaxTypeUseCase {
    void execute(UUID uuid);
}

// ListTaxTypesUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.TaxTypeResponseDto;
import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListTaxTypesUseCase {
    Page<TaxTypeResponseDto> execute(
        Boolean enabled, 
        TaxApplicationType applicationType, 
        Pageable pageable
    );
}

// SearchTaxTypesUseCase.java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.TaxTypeResponseDto;
import java.util.List;

public interface SearchTaxTypesUseCase {
    List<TaxTypeResponseDto> execute(String searchTerm);
}
```

#### Output Ports (Repository Interfaces)
```java
// TaxTypePort.java
package com.jcuadrado.erplitebackend.application.port.out;

import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxType;
import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxTypePort {
    TaxType save(TaxType taxType);
    Optional<TaxType> findByUuid(UUID uuid);
    Optional<TaxType> findByCode(String code);
    Page<TaxType> findAll(Pageable pageable);
    Page<TaxType> findByEnabled(Boolean enabled, Pageable pageable);
    Page<TaxType> findByEnabledAndApplicationType(
        Boolean enabled, 
        TaxApplicationType applicationType, 
        Pageable pageable
    );
    List<TaxType> searchByNameContainingIgnoreCase(String searchTerm);
    boolean existsByCode(String code);
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    long countProductsWithTaxType(UUID taxTypeUuid);
    void deleteByUuid(UUID uuid);
}
```

#### Services (Use Case Implementations)
```java
// CreateTaxTypeService.java
package com.jcuadrado.erplitebackend.application.service.taxtype;

import com.jcuadrado.erplitebackend.application.port.in.taxtype.CreateTaxTypeUseCase;
import com.jcuadrado.erplitebackend.application.port.out.TaxTypePort;
import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxType;
import com.jcuadrado.erplitebackend.domain.taxtype.service.TaxTypeDomainService;
import com.jcuadrado.erplitebackend.domain.taxtype.service.TaxTypeValidationService;
import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.CreateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.TaxTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.mapper.TaxTypeDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateTaxTypeService implements CreateTaxTypeUseCase {
    
    private final TaxTypePort taxTypePort;
    private final TaxTypeDomainService domainService;
    private final TaxTypeValidationService validationService;
    private final TaxTypeDtoMapper dtoMapper;
    
    @Override
    public TaxTypeResponseDto execute(CreateTaxTypeRequestDto request) {
        log.info("Creating tax type with code: {}", request.getCode());
        
        // Validaciones de dominio
        domainService.validateCode(request.getCode());
        domainService.validateName(request.getName());
        domainService.validatePercentage(request.getPercentage());
        
        // Validar unicidad de código
        validationService.ensureCodeIsUnique(request.getCode(), null);
        
        // Crear entidad de dominio
        TaxType taxType = TaxType.builder()
                .uuid(UUID.randomUUID())
                .code(request.getCode())
                .name(request.getName())
                .percentage(request.getPercentage())
                .isIncluded(request.getIsIncluded())
                .applicationType(request.getApplicationType())
                .description(request.getDescription())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Persistir
        TaxType savedTaxType = taxTypePort.save(taxType);
        
        log.info("Tax type created successfully with UUID: {}", savedTaxType.getUuid());
        
        // TODO: Registrar en AuditLog
        
        return dtoMapper.toResponseDto(savedTaxType);
    }
}

// Similar implementations for:
// - GetTaxTypeService
// - UpdateTaxTypeService
// - DeactivateTaxTypeService
// - ActivateTaxTypeService
// - ListTaxTypesService
// - SearchTaxTypesService
```

---

### Infrastructure Layer

#### Persistence (Output Adapters)

**Entity** (`infrastructure/out/taxtype/persistence/entity/`)
```java
// TaxTypeEntity.java
package com.jcuadrado.erplitebackend.infrastructure.out.taxtype.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tax_types", indexes = {
    @Index(name = "idx_tax_type_uuid", columnList = "uuid", unique = true),
    @Index(name = "idx_tax_type_code", columnList = "code", unique = true),
    @Index(name = "idx_tax_type_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxTypeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 36)
    private UUID uuid;
    
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal percentage;
    
    @Column(name = "is_included", nullable = false)
    private Boolean isIncluded;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false, length = 20)
    private TaxApplicationType applicationType;
    
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

**Repository** (`infrastructure/out/taxtype/persistence/repository/`)
```java
// TaxTypeJpaRepository.java
package com.jcuadrado.erplitebackend.infrastructure.out.taxtype.persistence.repository;

import com.jcuadrado.erplitebackend.infrastructure.out.taxtype.persistence.entity.TaxTypeEntity;
import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxTypeJpaRepository extends JpaRepository<TaxTypeEntity, Long> {
    
    Optional<TaxTypeEntity> findByUuid(UUID uuid);
    
    Optional<TaxTypeEntity> findByCode(String code);
    
    Page<TaxTypeEntity> findByEnabled(Boolean enabled, Pageable pageable);
    
    @Query("SELECT t FROM TaxTypeEntity t WHERE t.enabled = :enabled " +
           "AND (t.applicationType = :applicationType OR t.applicationType = 'BOTH')")
    Page<TaxTypeEntity> findByEnabledAndApplicationType(
        @Param("enabled") Boolean enabled,
        @Param("applicationType") TaxApplicationType applicationType,
        Pageable pageable
    );
    
    List<TaxTypeEntity> findByNameContainingIgnoreCaseAndEnabled(String name, Boolean enabled);
    
    boolean existsByCode(String code);
    
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.taxType.uuid = :taxTypeUuid")
    long countProductsWithTaxType(@Param("taxTypeUuid") UUID taxTypeUuid);
}
```

**Adapter** (`infrastructure/out/taxtype/persistence/adapter/`)
```java
// TaxTypeRepositoryAdapter.java
package com.jcuadrado.erplitebackend.infrastructure.out.taxtype.persistence.adapter;

import com.jcuadrado.erplitebackend.application.port.out.TaxTypePort;
import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxType;
import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.out.taxtype.persistence.entity.TaxTypeEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.taxtype.persistence.repository.TaxTypeJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.taxtype.mapper.TaxTypeEntityMapper;
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
public class TaxTypeRepositoryAdapter implements TaxTypePort {
    
    private final TaxTypeJpaRepository jpaRepository;
    private final TaxTypeEntityMapper entityMapper;
    
    @Override
    public TaxType save(TaxType taxType) {
        TaxTypeEntity entity = entityMapper.toEntity(taxType);
        TaxTypeEntity saved = jpaRepository.save(entity);
        return entityMapper.toDomain(saved);
    }
    
    @Override
    public Optional<TaxType> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Optional<TaxType> findByCode(String code) {
        return jpaRepository.findByCode(code)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Page<TaxType> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Page<TaxType> findByEnabled(Boolean enabled, Pageable pageable) {
        return jpaRepository.findByEnabled(enabled, pageable)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public Page<TaxType> findByEnabledAndApplicationType(
            Boolean enabled, 
            TaxApplicationType applicationType, 
            Pageable pageable) {
        return jpaRepository.findByEnabledAndApplicationType(enabled, applicationType, pageable)
                .map(entityMapper::toDomain);
    }
    
    @Override
    public List<TaxType> searchByNameContainingIgnoreCase(String searchTerm) {
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
    public long countProductsWithTaxType(UUID taxTypeUuid) {
        return jpaRepository.countProductsWithTaxType(taxTypeUuid);
    }
    
    @Override
    public void deleteByUuid(UUID uuid) {
        jpaRepository.findByUuid(uuid)
                .ifPresent(jpaRepository::delete);
    }
}
```

#### REST API (Input Adapters)

**Controller** (`infrastructure/in/api/taxtype/rest/`)
```java
// TaxTypeController.java
package com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.rest;

import com.jcuadrado.erplitebackend.application.port.in.taxtype.*;
import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto.*;
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
@RequestMapping("/api/tax-types")
@RequiredArgsConstructor
@Tag(name = "Tax Types", description = "Tax Types Management API")
public class TaxTypeController {
    
    private final CreateTaxTypeUseCase createTaxTypeUseCase;
    private final GetTaxTypeUseCase getTaxTypeUseCase;
    private final UpdateTaxTypeUseCase updateTaxTypeUseCase;
    private final DeactivateTaxTypeUseCase deactivateTaxTypeUseCase;
    private final ActivateTaxTypeUseCase activateTaxTypeUseCase;
    private final ListTaxTypesUseCase listTaxTypesUseCase;
    private final SearchTaxTypesUseCase searchTaxTypesUseCase;
    
    @PostMapping
    @Operation(summary = "Create new tax type")
    public ResponseEntity<TaxTypeResponseDto> create(
            @Valid @RequestBody CreateTaxTypeRequestDto request) {
        TaxTypeResponseDto response = createTaxTypeUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{uuid}")
    @Operation(summary = "Get tax type by UUID")
    public ResponseEntity<TaxTypeResponseDto> getByUuid(@PathVariable UUID uuid) {
        TaxTypeResponseDto response = getTaxTypeUseCase.execute(uuid);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{uuid}")
    @Operation(summary = "Update tax type")
    public ResponseEntity<TaxTypeResponseDto> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateTaxTypeRequestDto request) {
        TaxTypeResponseDto response = updateTaxTypeUseCase.execute(uuid, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{uuid}/deactivate")
    @Operation(summary = "Deactivate tax type (soft delete)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        deactivateTaxTypeUseCase.execute(uuid);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{uuid}/activate")
    @Operation(summary = "Activate tax type")
    public ResponseEntity<Void> activate(@PathVariable UUID uuid) {
        activateTaxTypeUseCase.execute(uuid);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    @Operation(summary = "List tax types with filters and pagination")
    public ResponseEntity<Page<TaxTypeResponseDto>> list(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) TaxApplicationType applicationType,
            @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        Page<TaxTypeResponseDto> response = listTaxTypesUseCase.execute(
            enabled, applicationType, pageable
        );
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search tax types by name")
    public ResponseEntity<List<TaxTypeResponseDto>> search(
            @RequestParam String name) {
        List<TaxTypeResponseDto> response = searchTaxTypesUseCase.execute(name);
        return ResponseEntity.ok(response);
    }
}
```

**DTOs** (`infrastructure/in/api/taxtype/dto/`)
```java
// CreateTaxTypeRequestDto.java
package com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto;

import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaxTypeRequestDto {
    
    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9._-]+$", message = "Code must contain only uppercase letters, numbers, dots, and hyphens")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 4, message = "Percentage must have at most 3 integer digits and 4 decimal digits")
    private BigDecimal percentage;
    
    @NotNull(message = "isIncluded is required")
    private Boolean isIncluded;
    
    @NotNull(message = "Application type is required")
    private TaxApplicationType applicationType;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}

// UpdateTaxTypeRequestDto.java
package com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto;

import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaxTypeRequestDto {
    
    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9._-]+$", message = "Code must contain only uppercase letters, numbers, dots, and hyphens")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
    @Digits(integer = 3, fraction = 4, message = "Percentage must have at most 3 integer digits and 4 decimal digits")
    private BigDecimal percentage;
    
    @NotNull(message = "isIncluded is required")
    private Boolean isIncluded;
    
    @NotNull(message = "Application type is required")
    private TaxApplicationType applicationType;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}

// TaxTypeResponseDto.java
package com.jcuadrado.erplitebackend.infrastructure.in.api.taxtype.dto;

import com.jcuadrado.erplitebackend.domain.taxtype.model.TaxApplicationType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxTypeResponseDto {
    private UUID uuid;
    private String code;
    private String name;
    private BigDecimal percentage;
    private Boolean isIncluded;
    private TaxApplicationType applicationType;
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
-- V1.3__create_tax_types_table.sql

CREATE TABLE tax_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    percentage DECIMAL(7,4) NOT NULL,
    is_included BOOLEAN NOT NULL DEFAULT FALSE,
    application_type VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    INDEX idx_tax_type_uuid (uuid),
    INDEX idx_tax_type_code (code),
    INDEX idx_tax_type_enabled (enabled),
    INDEX idx_tax_type_application_type (application_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment
ALTER TABLE tax_types COMMENT = 'Catálogo de tipos de impuestos (IVA, ReteFuente, etc.)';
```

### Seed Data (Flyway Migration)
```sql
-- V1.4__insert_colombia_tax_types.sql

INSERT INTO tax_types (uuid, code, name, percentage, is_included, application_type, description, enabled, created_at, updated_at)
VALUES
    (UUID(), 'IVA19', 'IVA 19%', 19.0000, FALSE, 'BOTH', 'Impuesto sobre el valor agregado tarifa general', TRUE, NOW(), NOW()),
    (UUID(), 'IVA5', 'IVA 5%', 5.0000, FALSE, 'BOTH', 'Impuesto sobre el valor agregado tarifa reducida', TRUE, NOW(), NOW()),
    (UUID(), 'IVA0', 'IVA 0%', 0.0000, FALSE, 'BOTH', 'IVA para bienes exentos', TRUE, NOW(), NOW()),
    (UUID(), 'RETE2.5', 'ReteFuente 2.5%', 2.5000, FALSE, 'PURCHASE', 'Retención en la fuente servicios', TRUE, NOW(), NOW()),
    (UUID(), 'RETE10', 'ReteFuente 10%', 10.0000, FALSE, 'PURCHASE', 'Retención en la fuente honorarios', TRUE, NOW(), NOW()),
    (UUID(), 'RETEIVA15', 'ReteIVA 15%', 15.0000, FALSE, 'PURCHASE', 'Retención de IVA', TRUE, NOW(), NOW()),
    (UUID(), 'ICA', 'ICA Variable', 1.0000, FALSE, 'BOTH', 'Impuesto de Industria y Comercio (varía por municipio)', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    percentage = VALUES(percentage),
    is_included = VALUES(is_included),
    application_type = VALUES(application_type),
    description = VALUES(description),
    updated_at = NOW();
```

---

## 🔗 API Endpoints

### POST /api/tax-types
Create new tax type

**Request Body:**
```json
{
  "code": "IVA19",
  "name": "IVA 19%",
  "percentage": 19.0,
  "isIncluded": false,
  "applicationType": "BOTH",
  "description": "Impuesto sobre el valor agregado tarifa general"
}
```

**Response (201 Created):**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "code": "IVA19",
  "name": "IVA 19%",
  "percentage": 19.0000,
  "isIncluded": false,
  "applicationType": "BOTH",
  "description": "Impuesto sobre el valor agregado tarifa general",
  "enabled": true,
  "createdAt": "2026-02-01T10:30:00",
  "updatedAt": "2026-02-01T10:30:00"
}
```

### GET /api/tax-types/{uuid}
Get tax type by UUID

**Response (200 OK):** Same as POST response

### PUT /api/tax-types/{uuid}
Update tax type

**Request Body:** Same as POST

**Response (200 OK):** Updated tax type

### PATCH /api/tax-types/{uuid}/deactivate
Deactivate tax type (soft delete)

**Response (200 OK):** Empty body

### PATCH /api/tax-types/{uuid}/activate
Activate tax type

**Response (200 OK):** Empty body

### GET /api/tax-types
List tax types with filters and pagination

**Query Parameters:**
- `enabled` (optional): Filter by active/inactive
- `applicationType` (optional): SALE, PURCHASE, BOTH
- `page` (optional, default=0)
- `size` (optional, default=20)
- `sort` (optional, default=code,asc)

**Response (200 OK):**
```json
{
  "content": [
    {
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "code": "IVA19",
      "name": "IVA 19%",
      "percentage": 19.0000,
      "isIncluded": false,
      "applicationType": "BOTH",
      "description": "Impuesto sobre el valor agregado tarifa general",
      "enabled": true,
      "createdAt": "2026-02-01T10:30:00",
      "updatedAt": "2026-02-01T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 7,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 7,
  "empty": false
}
```

### GET /api/tax-types/search
Search tax types by name

**Query Parameters:**
- `name` (required): Search term (case-insensitive)

**Response (200 OK):**
```json
[
  {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "code": "IVA19",
    "name": "IVA 19%",
    ...
  },
  {
    "uuid": "550e8400-e29b-41d4-a716-446655440001",
    "code": "IVA5",
    "name": "IVA 5%",
    ...
  }
]
```

---

## ⚙️ Configuration

### Application Properties
```properties
# Tax Types Module Configuration
erplite.taxtype.default-page-size=20
erplite.taxtype.max-page-size=100
```

---

## 🧪 Testing Strategy

### Unit Tests
- Domain model tests
- Domain service tests (validation logic)
- Use case tests with mocked ports
- Mapper tests

### Integration Tests
- Repository tests with Testcontainers
- Controller tests with MockMvc
- End-to-end API tests

### Test Coverage Target
- Overall: >= 85%
- Domain layer: >= 95%
- Application layer: >= 90%
- Infrastructure layer: >= 75%

---

## 📊 Performance Considerations

### Indexing Strategy
- UUID index for primary key lookups
- Code index for uniqueness validation
- Enabled index for filtering
- Application type index for filtering

### Caching Strategy
- Cache tax types list (TTL: 1 hour)
- Cache individual tax types by UUID (TTL: 1 hour)
- Invalidate cache on create/update/delete

### Query Optimization
- Use pagination for all list operations
- Limit search results to enabled tax types by default
- Use covering indexes for common queries

---

## 🔒 Security Considerations

### Authorization
- CREATE, UPDATE, DELETE: Requires `ADMIN` or `TAX_MANAGER` role
- READ operations: Requires `USER` role (any authenticated user)

### Audit
- All CUD operations logged to AuditLog
- Include user, timestamp, old/new values

---

##  Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-01 | Development Team | Initial version |
