# Technical Specification: Módulo de Tipos de Impuestos

**Feature**: Tax Types Module  
**Created**: February 1, 2026  
**Updated**: February 13, 2026  
**Related**: [1-functional-spec.md](1-functional-spec.md)  
**Phase**: PHASE 2 - Specification  
**Arquitectura:** Hexagonal (Puertos y Adaptadores)

---

## 📐 Technical Overview

Implementación del módulo de Tipos de Impuestos siguiendo **Arquitectura Hexagonal** (Ports & Adapters) alineada con el scaffolding del proyecto, con separación estricta entre dominio, aplicación e infraestructura. Cada feature tiene su propia carpeta dentro de cada capa para mantener el código organizado y escalable.

### Architecture Style
- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Layers**: Domain → Application → Infrastructure
- **Communication**: Inbound/Outbound Ports
- **Organization**: Feature-based folders within each layer
- **CQRS**: Separación de operaciones de consulta (Compare) y comando (Manage)

### Stack Tecnológico
- **Lenguaje**: Java 21
- **Framework**: Spring Boot 3.x
- **ORM**: Spring Data JPA con Hibernate
- **Base de Datos**: MySQL 8.0+
- **Migraciones**: Flyway
- **Mapeo**: MapStruct 1.5.5
- **Validación**: Hibernate Validator (Bean Validation)
- **Pruebas**: JUnit 5, Mockito, AssertJ
- **Documentación de API**: SpringDoc OpenAPI 3

---

## 📦 Estructura de Paquetes (Organizada por Features)

**NOTA**: Cada feature tiene su propia carpeta dentro de cada capa para mantener el código organizado y escalable.

```
com.jcuadrado.erplitebackend/
│
├── domain/
│   ├── model/
│   │   └── taxtype/
│   │       ├── TaxType.java                         # Raíz Agregada
│   │       └── TaxApplicationType.java              # Enum (SALE, PURCHASE, BOTH)
│   ├── service/
│   │   └── taxtype/
│   │       ├── TaxTypeDomainService.java            # Reglas de negocio
│   │       └── TaxTypeValidationService.java        # Validación de dominio
│   ├── port/
│   │   └── out/
│   │       └── taxtype/
│   │           └── TaxTypeRepository.java           # Puerto de salida
│   └── exception/
│       └── taxtype/
│           ├── TaxTypeNotFoundException.java
│           ├── DuplicateTaxTypeCodeException.java
│           ├── InvalidTaxTypeCodeException.java
│           ├── InvalidTaxPercentageException.java
│           ├── InvalidTaxTypeDataException.java
│           └── TaxTypeConstraintException.java
│
├── application/
│   ├── port/
│   │   └── in/
│   │       └── taxtype/
│   │           ├── CompareTaxTypesUseCase.java      # Operaciones de consulta (CQRS - Query)
│   │           └── ManageTaxTypeUseCase.java        # Operaciones de comando (CQRS - Command)
│   └── usecase/
│       └── taxtype/
│           ├── CompareTaxTypesUseCaseImpl.java
│           └── ManageTaxTypeUseCaseImpl.java
│
└── infrastructure/
    ├── config/
    │   └── BeanConfiguration.java                   # Configuración compartida
    │
    ├── in/
    │   └── web/
    │       ├── controller/
    │       │   └── taxtype/
    │       │       └── TaxTypeController.java
    │       ├── dto/
    │       │   └── taxtype/
    │       │       ├── CreateTaxTypeRequestDto.java
    │       │       ├── UpdateTaxTypeRequestDto.java
    │       │       └── TaxTypeResponseDto.java
    │       ├── mapper/
    │       │   └── taxtype/
    │       │       └── TaxTypeDtoMapper.java         # MapStruct
    │       └── advice/
    │           └── GlobalExceptionHandler.java       # Compartido
    │
    └── out/
        └── persistence/
            ├── adapter/
            │   └── taxtype/
            │       └── TaxTypeRepositoryAdapter.java
            ├── entity/
            │   └── taxtype/
            │       └── TaxTypeEntity.java            # JPA Entity
            ├── repository/
            │   └── taxtype/
            │       └── TaxTypeJpaRepository.java
            ├── mapper/
            │   └── taxtype/
            │       └── TaxTypeEntityMapper.java      # MapStruct
            └── util/
                └── taxtype/
                    └── TaxTypeSpecificationUtil.java
```

---

## 🏗️ Capa de Dominio (Domain Layer)

### Modelos de Dominio

#### `TaxType.java` - Aggregate Root
```java
package com.jcuadrado.erplitebackend.domain.model.taxtype;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TaxType - Raíz de Agregado (Aggregate Root)
 * 
 * Representa un tipo de impuesto en el sistema (IVA, ReteFuente, ReteIVA, ICA, etc.).
 * Es el punto de entrada para todas las operaciones relacionadas con tipos de impuestos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxType {
    private Long id;
    private UUID uuid;
    private String code;                     // Unique, max 20 chars, uppercase + dots + hyphens + underscores
    private String name;                     // max 100 chars
    private BigDecimal percentage;           // 0.0000 to 100.0000, 4 decimals precision
    private Boolean isIncluded;              // true=incluido en precio, false=adicional
    private TaxApplicationType applicationType;  // SALE, PURCHASE, BOTH
    private Boolean enabled;
    
    // Auditoría (se modelan como Long en el dominio, se resuelven en capas externas)
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // ========== Métodos de Negocio ==========
    
    /**
     * Activa el tipo de impuesto
     */
    public void activate() {
        this.enabled = true;
        this.deletedBy = null;
        this.deletedAt = null;
    }
    
    /**
     * Desactiva el tipo de impuesto (soft delete)
     */
    public void deactivate(Long userId) {
        this.enabled = false;
        this.deletedBy = userId;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica si está activo
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(enabled);
    }
    
    /**
     * Verifica si es aplicable para ventas
     */
    public boolean isApplicableForSales() {
        return applicationType == TaxApplicationType.SALE || 
               applicationType == TaxApplicationType.BOTH;
    }
    
    /**
     * Verifica si es aplicable para compras
     */
    public boolean isApplicableForPurchases() {
        return applicationType == TaxApplicationType.PURCHASE || 
               applicationType == TaxApplicationType.BOTH;
    }
    
    /**
     * Valida si el porcentaje está en rango válido
     */
    public boolean isValidPercentage() {
        return percentage != null && 
               percentage.compareTo(BigDecimal.ZERO) >= 0 && 
               percentage.compareTo(new BigDecimal("100.0000")) <= 0;
    }
}
```

#### `TaxApplicationType.java` - Enum
```java
package com.jcuadrado.erplitebackend.domain.model.taxtype;

/**
 * TaxApplicationType - Enum de tipo de aplicación
 * 
 * Define en qué contexto se aplica el impuesto.
 */
public enum TaxApplicationType {
    SALE,        // Solo para ventas
    PURCHASE,    // Solo para compras
    BOTH         // Aplicable a ambos contextos
}
```

---

### Servicios de Dominio

#### `TaxTypeDomainService.java`
```java
package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.*;
import java.math.BigDecimal;

/**
 * TaxTypeDomainService - Servicio de dominio (POJO)
 * 
 * Contiene reglas de negocio que no pertenecen a una entidad específica.
 * Se registra como Bean en BeanConfiguration.
 */
public class TaxTypeDomainService {
    
    /**
     * Valida el formato del código (BR-TT-001)
     * - No puede ser vacío
     * - Máximo 20 caracteres
     * - Solo letras mayúsculas, números, puntos, guiones y guiones bajos
     */
    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidTaxTypeCodeException("Tax type code cannot be empty");
        }
        if (code.length() > 20) {
            throw new InvalidTaxTypeCodeException("Tax type code cannot exceed 20 characters");
        }
        if (!code.matches("^[A-Z0-9._-]+$")) {
            throw new InvalidTaxTypeCodeException(
                "Tax type code must contain only uppercase letters, numbers, dots, and hyphens"
            );
        }
    }
    
    /**
     * Valida el porcentaje (BR-TT-002)
     * - No puede ser null
     * - Debe estar entre 0.0000 y 100.0000
     * - Máximo 4 decimales de precisión
     */
    public void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new InvalidTaxPercentageException("Tax percentage cannot be null");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTaxPercentageException("Percentage must be between 0 and 100");
        }
        if (percentage.compareTo(new BigDecimal("100.0000")) > 0) {
            throw new InvalidTaxPercentageException("Percentage must be between 0 and 100");
        }
        if (percentage.scale() > 4) {
            throw new InvalidTaxPercentageException("Percentage cannot have more than 4 decimal places");
        }
    }
    
    /**
     * Valida el nombre
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
     * Determina si un tipo de impuesto puede ser eliminado (BR-TT-005)
     * No puede eliminarse si tiene productos o transacciones asociadas
     */
    public boolean canBeDeleted(TaxType taxType, long associatedProductsCount, long associatedTransactionsCount) {
        return associatedProductsCount == 0 && associatedTransactionsCount == 0;
    }
}
```

#### `TaxTypeValidationService.java`
```java
package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.port.out.taxtype.TaxTypeRepository;
import java.util.UUID;

/**
 * TaxTypeValidationService - Validaciones que requieren acceso a repositorio
 * 
 * Se registra como Bean en BeanConfiguration y recibe el repository port inyectado.
 */
public class TaxTypeValidationService {
    
    private final TaxTypeRepository repository;
    
    public TaxTypeValidationService(TaxTypeRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Asegura que el código sea único (BR-TT-001)
     * 
     * @param code Código a validar
     * @param excludeUuid UUID a excluir (en caso de actualización)
     */
    public void ensureCodeIsUnique(String code, UUID excludeUuid) {
        boolean exists;
        if (excludeUuid != null) {
            exists = repository.existsByCodeAndUuidNot(code, excludeUuid);
        } else {
            exists = repository.existsByCode(code);
        }
        
        if (exists) {
            throw new DuplicateTaxTypeCodeException(
                "Tax type with code '" + code + "' already exists"
            );
        }
    }
}
```

---

### Excepciones de Dominio

```java
package com.jcuadrado.erplitebackend.domain.exception.taxtype;

import java.util.UUID;

// TaxTypeNotFoundException.java
public class TaxTypeNotFoundException extends RuntimeException {
    public TaxTypeNotFoundException(String message) {
        super(message);
    }
    
    public TaxTypeNotFoundException(UUID uuid) {
        super("Tax type not found with UUID: " + uuid);
    }
}

// DuplicateTaxTypeCodeException.java
public class DuplicateTaxTypeCodeException extends RuntimeException {
    public DuplicateTaxTypeCodeException(String message) {
        super(message);
    }
}

// InvalidTaxTypeCodeException.java
public class InvalidTaxTypeCodeException extends RuntimeException {
    public InvalidTaxTypeCodeException(String message) {
        super(message);
    }
}

// InvalidTaxPercentageException.java
public class InvalidTaxPercentageException extends RuntimeException {
    public InvalidTaxPercentageException(String message) {
        super(message);
    }
}

// InvalidTaxTypeDataException.java
public class InvalidTaxTypeDataException extends RuntimeException {
    public InvalidTaxTypeDataException(String message) {
        super(message);
    }
}

// TaxTypeConstraintException.java
public class TaxTypeConstraintException extends RuntimeException {
    public TaxTypeConstraintException(String message) {
        super(message);
    }
}
```

---

### Puertos de Salida (Output Ports)

#### `TaxTypeRepository.java`
```java
package com.jcuadrado.erplitebackend.domain.port.out.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * TaxTypeRepository - Puerto de salida (Output Port)
 * 
 * Define las operaciones de persistencia sin implementación.
 * Se implementa en la capa de infraestructura (TaxTypeRepositoryAdapter).
 */
public interface TaxTypeRepository {
    
    // Operaciones básicas
    TaxType save(TaxType taxType);
    Optional<TaxType> findByUuid(UUID uuid);
    Optional<TaxType> findByCode(String code);
    void delete(TaxType taxType);
    
    // Consultas con filtros
    Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable);
    Page<TaxType> findByEnabled(Boolean enabled, Pageable pageable);
    List<TaxType> findByNameContaining(String searchTerm, Boolean enabled);
    List<TaxType> findByApplicationType(TaxApplicationType applicationType, Boolean enabled);
    
    // Validaciones
    boolean existsByCode(String code);
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    
    // Consultas para validaciones de negocio
    long countProductsWithTaxType(UUID taxTypeUuid);
    long countTransactionsWithTaxType(UUID taxTypeUuid);
}
```

---

## 🎯 Capa de Aplicación (Application Layer)

### Puertos de Entrada (Input Ports - Use Cases)

Seguimos el patrón **CQRS** (Command Query Responsibility Segregation):
- **CompareTaxTypesUseCase**: Operaciones de consulta (Query)
- **ManageTaxTypeUseCase**: Operaciones de comando (Command)

#### `CompareTaxTypesUseCase.java` - Queries
```java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CompareTaxTypesUseCase - Casos de uso de consulta (CQRS - Query Side)
 */
public interface CompareTaxTypesUseCase {
    
    /**
     * Obtiene un tipo de impuesto por UUID
     */
    TaxType getByUuid(UUID uuid);
    
    /**
     * Obtiene un tipo de impuesto por código
     */
    TaxType getByCode(String code);
    
    /**
     * Obtiene todos los tipos de impuestos activos
     */
    List<TaxType> getAllActive();
    
    /**
     * Busca tipos de impuestos con filtros y paginación
     * Filtros soportados: enabled (Boolean), applicationType (TaxApplicationType), name (String)
     * El filtro 'name' realiza búsqueda case-insensitive parcial
     */
    Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable);
}
```

#### `ManageTaxTypeUseCase.java` - Commands
```java
package com.jcuadrado.erplitebackend.application.port.in.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import java.util.UUID;

/**
 * ManageTaxTypeUseCase - Casos de uso de comando (CQRS - Command Side)
 */
public interface ManageTaxTypeUseCase {
    
    /**
     * Crea un nuevo tipo de impuesto
     */
    TaxType create(TaxType taxType);
    
    /**
     * Actualiza un tipo de impuesto existente
     */
    TaxType update(UUID uuid, TaxType updates);
    
    /**
     * Activa un tipo de impuesto
     */
    void activate(UUID uuid);
    
    /**
     * Desactiva un tipo de impuesto (soft delete)
     */
    void deactivate(UUID uuid);
    
    /**
     * Elimina un tipo de impuesto (solo si no tiene productos ni transacciones)
     */
    void delete(UUID uuid);
}
```

---

### Implementaciones de Casos de Uso

#### `CompareTaxTypesUseCaseImpl.java`
```java
package com.jcuadrado.erplitebackend.application.usecase.taxtype;

import com.jcuadrado.erplitebackend.application.port.in.taxtype.CompareTaxTypesUseCase;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.port.out.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompareTaxTypesUseCaseImpl implements CompareTaxTypesUseCase {
    
    private final TaxTypeRepository repository;
    
    @Override
    @Transactional(readOnly = true)
    public TaxType getByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));
    }
    
    @Override
    @Transactional(readOnly = true)
    public TaxType getByCode(String code) {
        return repository.findByCode(code)
            .orElseThrow(() -> new TaxTypeNotFoundException("Tax type not found with code: " + code));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TaxType> getAllActive() {
        return repository.findByEnabled(true);
    }
    
    /**
     * Busca tipos de impuestos con múltiples filtros combinables.
     * Los filtros se construyen dinámicamente en el repository adapter usando Specifications.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable) {
        return repository.findAll(filters, pageable);
    }
}
```

#### `ManageTaxTypeUseCaseImpl.java`
```java
package com.jcuadrado.erplitebackend.application.usecase.taxtype;

import com.jcuadrado.erplitebackend.application.port.in.taxtype.ManageTaxTypeUseCase;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.port.out.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeValidationService;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageTaxTypeUseCaseImpl implements ManageTaxTypeUseCase {
    
    private final TaxTypeRepository repository;
    private final TaxTypeDomainService domainService;
    private final TaxTypeValidationService validationService;
    
    @Override
    @Transactional
    public TaxType create(TaxType taxType) {
        domainService.validateCode(taxType.getCode());
        domainService.validateName(taxType.getName());
        domainService.validatePercentage(taxType.getPercentage());
        validationService.ensureCodeIsUnique(taxType.getCode(), null);
        
        taxType.setUuid(UUID.randomUUID());
        taxType.setCreatedAt(LocalDateTime.now());
        taxType.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(taxType);
    }
    
    @Override
    @Transactional
    public TaxType update(UUID uuid, TaxType updates) {
        TaxType existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));
        
        domainService.validateName(updates.getName());
        domainService.validatePercentage(updates.getPercentage());
        
        if (!existing.getCode().equals(updates.getCode())) {
            domainService.validateCode(updates.getCode());
            validationService.ensureCodeIsUnique(updates.getCode(), uuid);
            existing.setCode(updates.getCode());
        }
        
        existing.setName(updates.getName());
        existing.setPercentage(updates.getPercentage());
        existing.setIsIncluded(updates.getIsIncluded());
        existing.setApplicationType(updates.getApplicationType());
        existing.setEnabled(updates.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(existing);
    }
    
    @Override
    @Transactional
    public void activate(UUID uuid) {
        TaxType taxType = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));
        
        taxType.activate();
        repository.save(taxType);
    }
    
    @Override
    @Transactional
    public void deactivate(UUID uuid) {
        TaxType taxType = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));
        
        // Verificar si puede desactivarse (BR-TT-005)
        long productsCount = repository.countProductsWithTaxType(uuid);
        long transactionsCount = repository.countTransactionsWithTaxType(uuid);
        
        if (!domainService.canBeDeleted(taxType, productsCount, transactionsCount)) {
            throw new TaxTypeConstraintException(
                "Cannot deactivate tax type with " + productsCount + " products and " + 
                transactionsCount + " transactions associated"
            );
        }
        
        taxType.deactivate(null); // TODO: Get current user ID
        repository.save(taxType);
    }
    
    @Override
    @Transactional
    public void delete(UUID uuid) {
        TaxType taxType = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));
        
        // Verificar regla de negocio (BR-TT-005)
        long productsCount = repository.countProductsWithTaxType(uuid);
        long transactionsCount = repository.countTransactionsWithTaxType(uuid);
        
        if (!domainService.canBeDeleted(taxType, productsCount, transactionsCount)) {
            throw new TaxTypeConstraintException(
                "Cannot delete tax type with associated products or transactions"
            );
        }
        
        repository.delete(taxType);
    }
}
```

---

## 🏢 Capa de Infraestructura (Infrastructure Layer)

### Adaptadores de Entrada (Input Adapters - REST API)

#### `TaxTypeController.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.taxtype;

import com.jcuadrado.erplitebackend.application.port.in.taxtype.*;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.*;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.taxtype.TaxTypeDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tax-types")
@RequiredArgsConstructor
@Tag(name = "Tax Types", description = "API de gestión de tipos de impuestos")
public class TaxTypeController {
    
    private final CompareTaxTypesUseCase compareUseCase;
    private final ManageTaxTypeUseCase manageUseCase;
    private final TaxTypeDtoMapper dtoMapper;
    
    @PostMapping
    @Operation(summary = "Crear nuevo tipo de impuesto", description = "Crea un nuevo tipo de impuesto en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tipo de impuesto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Código duplicado")
    })
    public ResponseEntity<TaxTypeResponseDto> create(
            @Valid @RequestBody CreateTaxTypeRequestDto request) {
        TaxType domain = dtoMapper.toDomain(request);
        TaxType created = manageUseCase.create(domain);
        TaxTypeResponseDto response = dtoMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{uuid}")
    @Operation(summary = "Obtener tipo de impuesto por UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipo de impuesto encontrado"),
        @ApiResponse(responseCode = "404", description = "Tipo de impuesto no encontrado")
    })
    public ResponseEntity<TaxTypeResponseDto> getByUuid(
            @Parameter(description = "UUID del tipo de impuesto") 
            @PathVariable UUID uuid) {
        TaxType taxType = compareUseCase.getByUuid(uuid);
        TaxTypeResponseDto response = dtoMapper.toResponseDto(taxType);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{uuid}")
    @Operation(summary = "Actualizar tipo de impuesto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipo de impuesto actualizado"),
        @ApiResponse(responseCode = "404", description = "Tipo de impuesto no encontrado"),
        @ApiResponse(responseCode = "409", description = "Código duplicado")
    })
    public ResponseEntity<TaxTypeResponseDto> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateTaxTypeRequestDto request) {
        TaxType updates = dtoMapper.toDomain(request);
        TaxType updated = manageUseCase.update(uuid, updates);
        TaxTypeResponseDto response = dtoMapper.toResponseDto(updated);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{uuid}/activate")
    @Operation(summary = "Activar tipo de impuesto")
    public ResponseEntity<Void> activate(@PathVariable UUID uuid) {
        manageUseCase.activate(uuid);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{uuid}/deactivate")
    @Operation(summary = "Desactivar tipo de impuesto (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tipo de impuesto desactivado"),
        @ApiResponse(responseCode = "409", description = "No puede desactivarse (tiene productos/transacciones)")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        manageUseCase.deactivate(uuid);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{uuid}")
    @Operation(summary = "Eliminar tipo de impuesto (solo si no tiene productos ni transacciones)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tipo de impuesto eliminado"),
        @ApiResponse(responseCode = "409", description = "No puede eliminarse (tiene productos/transacciones)")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    @Operation(summary = "Listar y buscar tipos de impuestos con filtros y paginación",
               description = "Endpoint consolidado que soporta búsqueda por nombre, filtros por estado y tipo de aplicación, con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tipos de impuestos"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<Page<TaxTypeResponseDto>> list(
            @Parameter(description = "Filtrar por estado activo/inactivo")
            @RequestParam(required = false) Boolean enabled,
            
            @Parameter(description = "Filtrar por tipo de aplicación (SALE, PURCHASE, BOTH)")
            @RequestParam(required = false) TaxApplicationType applicationType,
            
            @Parameter(description = "Buscar por nombre (case-insensitive, búsqueda parcial)")
            @RequestParam(required = false) String name,
            
            @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Map<String, Object> filters = new HashMap<>();
        if (enabled != null) filters.put("enabled", enabled);
        if (applicationType != null) filters.put("applicationType", applicationType);
        if (name != null && !name.isBlank()) filters.put("name", name.trim());
        
        Page<TaxType> page = compareUseCase.findAll(filters, pageable);
        Page<TaxTypeResponseDto> response = page.map(dtoMapper::toResponseDto);
        return ResponseEntity.ok(response);
    }
}
```

---

### DTOs (Data Transfer Objects)

#### `CreateTaxTypeRequestDto.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaxTypeRequestDto {
    
    @NotBlank(message = "Tax type code is required")
    @Size(max = 20, message = "Tax type code cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9._-]+$", 
             message = "Tax type code must contain only uppercase letters, numbers, dots, and hyphens")
    private String code;
    
    @NotBlank(message = "Tax type name is required")
    @Size(max = 100, message = "Tax type name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0000", message = "Percentage must be between 0 and 100")
    @DecimalMax(value = "100.0000", message = "Percentage must be between 0 and 100")
    @Digits(integer = 3, fraction = 4, message = "Percentage cannot have more than 4 decimal places")
    private BigDecimal percentage;
    
    @NotNull(message = "isIncluded is required")
    private Boolean isIncluded;
    
    @NotNull(message = "Application type is required")
    private TaxApplicationType applicationType;
}
```

#### `UpdateTaxTypeRequestDto.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaxTypeRequestDto {
    
    @NotBlank(message = "Tax type code is required")
    @Size(max = 20, message = "Tax type code cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9._-]+$", 
             message = "Tax type code must contain only uppercase letters, numbers, dots, and hyphens")
    private String code;
    
    @NotBlank(message = "Tax type name is required")
    @Size(max = 100, message = "Tax type name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0000", message = "Percentage must be between 0 and 100")
    @DecimalMax(value = "100.0000", message = "Percentage must be between 0 and 100")
    @Digits(integer = 3, fraction = 4, message = "Percentage cannot have more than 4 decimal places")
    private BigDecimal percentage;
    
    @NotNull(message = "isIncluded is required")
    private Boolean isIncluded;
    
    @NotNull(message = "Application type is required")
    private TaxApplicationType applicationType;
    
    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
```

#### `TaxTypeResponseDto.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
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
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### Mappers

#### `TaxTypeDtoMapper.java` - MapStruct
```java
package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaxTypeDtoMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TaxType toDomain(CreateTaxTypeRequestDto dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TaxType toDomain(UpdateTaxTypeRequestDto dto);
    
    TaxTypeResponseDto toResponseDto(TaxType domain);
}
```

---

### Adaptadores de Salida (Output Adapters - Persistence)

#### `TaxTypeEntity.java` - JPA Entity
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tax_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxTypeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;
    
    @Column(nullable = false, unique = true, length = 20)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal percentage;
    
    @Column(nullable = false, name = "is_included")
    private Boolean isIncluded;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "application_type", length = 20)
    private TaxApplicationType applicationType;
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "deleted_by")
    private Long deletedBy;
    
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### `TaxTypeJpaRepository.java` - Spring Data JPA
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.repository.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxTypeJpaRepository extends JpaRepository<TaxTypeEntity, Long>, 
                                               JpaSpecificationExecutor<TaxTypeEntity> {
    
    Optional<TaxTypeEntity> findByUuid(UUID uuid);
    Optional<TaxTypeEntity> findByCode(String code);
    
    Page<TaxTypeEntity> findByEnabled(Boolean enabled, Pageable pageable);
    
    List<TaxTypeEntity> findByNameContainingIgnoreCaseAndEnabled(String name, Boolean enabled);
    
    List<TaxTypeEntity> findByApplicationTypeInAndEnabled(List<TaxApplicationType> types, Boolean enabled);
    
    boolean existsByCode(String code);
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
}
```

#### `TaxTypeRepositoryAdapter.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.port.out.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.taxtype.TaxTypeEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.repository.taxtype.TaxTypeJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.taxtype.TaxTypeSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TaxTypeRepositoryAdapter implements TaxTypeRepository {
    
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
    public void delete(TaxType taxType) {
        TaxTypeEntity entity = entityMapper.toEntity(taxType);
        jpaRepository.delete(entity);
    }
    
    @Override
    public Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec, pageable)
            .map(entityMapper::toDomain);
    }
    
    @Override
    public Page<TaxType> findByEnabled(Boolean enabled, Pageable pageable) {
        return jpaRepository.findByEnabled(enabled, pageable)
            .map(entityMapper::toDomain);
    }
    
    @Override
    public List<TaxType> findByNameContaining(String searchTerm, Boolean enabled) {
        return jpaRepository.findByNameContainingIgnoreCaseAndEnabled(searchTerm, enabled)
            .stream()
            .map(entityMapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TaxType> findByApplicationType(TaxApplicationType applicationType, Boolean enabled) {
        // Tipos que coinciden: específico o BOTH
        List<TaxApplicationType> types = Arrays.asList(applicationType, TaxApplicationType.BOTH);
        return jpaRepository.findByApplicationTypeInAndEnabled(types, enabled)
            .stream()
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
        // TODO: Implementar cuando exista el módulo de Products
        return 0;
    }
    
    @Override
    public long countTransactionsWithTaxType(UUID taxTypeUuid) {
        // TODO: Implementar cuando existan los módulos de Sales/Purchases
        return 0;
    }
}
```

#### `TaxTypeEntityMapper.java` - MapStruct
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaxTypeEntityMapper {
    
    TaxType toDomain(TaxTypeEntity entity);
    
    TaxTypeEntity toEntity(TaxType domain);
}
```

#### `TaxTypeSpecificationUtil.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TaxTypeSpecificationUtil - Construcción dinámica de consultas JPA
 */
public class TaxTypeSpecificationUtil {
    
    public static Specification<TaxTypeEntity> buildSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (filters.containsKey("enabled")) {
                Boolean enabled = (Boolean) filters.get("enabled");
                predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
            }
            
            if (filters.containsKey("applicationType")) {
                TaxApplicationType applicationType = (TaxApplicationType) filters.get("applicationType");
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("applicationType"), applicationType),
                        criteriaBuilder.equal(root.get("applicationType"), TaxApplicationType.BOTH)
                    )
                );
            }
            
            if (filters.containsKey("name")) {
                String name = (String) filters.get("name");
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

---

### Global Exception Handler

El manejo de excepciones se realiza en `GlobalExceptionHandler.java` (compartido), agregando casos específicos para tax-types:

```java
@ExceptionHandler(TaxTypeNotFoundException.class)
public ResponseEntity<ErrorResponse> handleTaxTypeNotFound(
        TaxTypeNotFoundException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.builder()
        .status(HttpStatus.NOT_FOUND.value())
        .message(ex.getMessage())
        .path(request.getDescription(false))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler(DuplicateTaxTypeCodeException.class)
public ResponseEntity<ErrorResponse> handleDuplicateTaxTypeCode(
        DuplicateTaxTypeCodeException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.builder()
        .status(HttpStatus.CONFLICT.value())
        .message(ex.getMessage())
        .path(request.getDescription(false))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}

@ExceptionHandler({InvalidTaxTypeCodeException.class, 
                   InvalidTaxPercentageException.class,
                   InvalidTaxTypeDataException.class})
public ResponseEntity<ErrorResponse> handleInvalidTaxTypeData(
        RuntimeException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .message(ex.getMessage())
        .path(request.getDescription(false))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}

@ExceptionHandler(TaxTypeConstraintException.class)
public ResponseEntity<ErrorResponse> handleTaxTypeConstraint(
        TaxTypeConstraintException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.builder()
        .status(HttpStatus.CONFLICT.value())
        .message(ex.getMessage())
        .path(request.getDescription(false))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}
```

---

### Bean Configuration

Registro de servicios de dominio en `BeanConfiguration.java`:

```java
@Bean
public TaxTypeDomainService taxTypeDomainService() {
    return new TaxTypeDomainService();
}

@Bean
public TaxTypeValidationService taxTypeValidationService(TaxTypeRepository taxTypeRepository) {
    return new TaxTypeValidationService(taxTypeRepository);
}
```

---

## 💾 Capa de Persistencia - Base de Datos

### Migración Flyway - V7: Schema

**Archivo**: `V7__create_tax_types_table.sql`

```sql
-- ==========================================
-- Migration V7: Tax Types Table
-- Created: 2026-02-13
-- Description: Crear tabla de tipos de impuestos
-- ==========================================

CREATE TABLE tax_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    percentage DECIMAL(7,4) NOT NULL DEFAULT 0.0000,
    is_included BOOLEAN NOT NULL DEFAULT FALSE,
    application_type VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Auditoría
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    
    -- Índices
    INDEX idx_tax_types_enabled (enabled),
    INDEX idx_tax_types_application_type (application_type),
    INDEX idx_tax_types_code (code),
    INDEX idx_tax_types_name (name)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comentarios
ALTER TABLE tax_types COMMENT = 'Tipos de impuestos (IVA, ReteFuente, ReteIVA, ICA, etc.)';
```

---

### Migración Flyway - V8: Seed Data

**Archivo**: `V8__insert_colombia_tax_types.sql`

```sql
-- ==========================================
-- Migration V8: Seed Data - Tax Types (Colombia)
-- Created: 2026-02-13
-- Description: Datos iniciales de tipos de impuestos para Colombia
-- ==========================================

-- Insertar tipos de impuestos si no existen (idempotente)
INSERT INTO tax_types (uuid, code, name, percentage, is_included, application_type, enabled, created_at, updated_at)
SELECT * FROM (
    -- IVA (Impuesto al Valor Agregado)
    SELECT UUID() as uuid, 'IVA19' as code, 'IVA 19%' as name, 19.0000 as percentage, 
           FALSE as is_included, 'BOTH' as application_type, TRUE as enabled, 
           NOW() as created_at, NOW() as updated_at
    UNION ALL
    SELECT UUID(), 'IVA5', 'IVA 5%', 5.0000, FALSE, 'BOTH', TRUE, NOW(), NOW()
    UNION ALL
    SELECT UUID(), 'IVA0', 'IVA 0%', 0.0000, FALSE, 'BOTH', TRUE, NOW(), NOW()
    
    -- ReteFuente (Retención en la Fuente)
    UNION ALL
    SELECT UUID(), 'RETE_SERV_2.5', 'ReteFuente Servicios 2.5%', 2.5000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()
    UNION ALL
    SELECT UUID(), 'RETE_SERV_4.0', 'ReteFuente Servicios 4%', 4.0000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()
    UNION ALL
    SELECT UUID(), 'RETE_HON_10.0', 'ReteFuente Honorarios 10%', 10.0000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()
    UNION ALL
    SELECT UUID(), 'RETE_COMP_2.5', 'ReteFuente Compras 2.5%', 2.5000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()
    
    -- ReteIVA
    UNION ALL
    SELECT UUID(), 'RETEIVA_15', 'ReteIVA 15%', 15.0000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()
    
    -- ICA (Impuesto de Industria y Comercio)
    UNION ALL
    SELECT UUID(), 'ICA_BOG_SERV', 'ICA Bogotá Servicios 0.414%', 0.4140, FALSE, 'BOTH', TRUE, NOW(), NOW()
    UNION ALL
    SELECT UUID(), 'ICA_BOG_IND', 'ICA Bogotá Industrial 0.966%', 0.9660, FALSE, 'BOTH', TRUE, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM tax_types WHERE code = tmp.code
);
```

---

### Migración Docker - Schema

**Archivo**: `docker/mysql-init/07_create_tax_types_table.sql`

```sql
-- Docker Init Script - Tax Types Table
-- Equivalente a V7__create_tax_types_table.sql

CREATE TABLE IF NOT EXISTS tax_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid CHAR(36) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    percentage DECIMAL(7,4) NOT NULL DEFAULT 0.0000,
    is_included BOOLEAN NOT NULL DEFAULT FALSE,
    application_type VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    INDEX idx_tax_types_enabled (enabled),
    INDEX idx_tax_types_application_type (application_type),
    INDEX idx_tax_types_code (code),
    INDEX idx_tax_types_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### Migración Docker - Seed Data

**Archivo**: `docker/mysql-init/08_insert_colombia_tax_types.sql`

```sql
-- Docker Init Script - Tax Types Seed Data
-- Equivalente a V8__insert_colombia_tax_types.sql

INSERT IGNORE INTO tax_types (uuid, code, name, percentage, is_included, application_type, enabled, created_at, updated_at) VALUES
(UUID(), 'IVA19', 'IVA 19%', 19.0000, FALSE, 'BOTH', TRUE, NOW(), NOW()),
(UUID(), 'IVA5', 'IVA 5%', 5.0000, FALSE, 'BOTH', TRUE, NOW(), NOW()),
(UUID(), 'IVA0', 'IVA 0%', 0.0000, FALSE, 'BOTH', TRUE, NOW(), NOW()),
(UUID(), 'RETE_SERV_2.5', 'ReteFuente Servicios 2.5%', 2.5000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()),
(UUID(), 'RETE_SERV_4.0', 'ReteFuente Servicios 4%', 4.0000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()),
(UUID(), 'RETE_HON_10.0', 'ReteFuente Honorarios 10%', 10.0000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()),
(UUID(), 'RETE_COMP_2.5', 'ReteFuente Compras 2.5%', 2.5000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()),
(UUID(), 'RETEIVA_15', 'ReteIVA 15%', 15.0000, FALSE, 'PURCHASE', TRUE, NOW(), NOW()),
(UUID(), 'ICA_BOG_SERV', 'ICA Bogotá Servicios 0.414%', 0.4140, FALSE, 'BOTH', TRUE, NOW(), NOW()),
(UUID(), 'ICA_BOG_IND', 'ICA Bogotá Industrial 0.966%', 0.9660, FALSE, 'BOTH', TRUE, NOW(), NOW());
```

---

## 🧪 Testing

### Estructura de Tests

```
src/test/java/com/jcuadrado/erplitebackend/
├── domain/
│   ├── model/taxtype/
│   │   └── TaxTypeTest.java
│   ├── service/taxtype/
│   │   ├── TaxTypeDomainServiceTest.java
│   │   └── TaxTypeValidationServiceTest.java
│   └── exception/taxtype/
│       └── TaxTypeExceptionsTest.java
├── application/
│   └── usecase/taxtype/
│       ├── CompareTaxTypesUseCaseImplTest.java
│       └── ManageTaxTypeUseCaseImplTest.java
└── infrastructure/
    ├── in/web/controller/taxtype/
    │   └── TaxTypeControllerTest.java
    ├── in/web/mapper/taxtype/
    │   └── TaxTypeDtoMapperTest.java
    └── out/persistence/
        ├── adapter/taxtype/
        │   └── TaxTypeRepositoryAdapterTest.java
        ├── entity/taxtype/
        │   └── TaxTypeEntityTest.java
        └── mapper/taxtype/
            └── TaxTypeEntityMapperTest.java
```

### Ejemplo: Test de Dominio

```java
package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@DisplayName("TaxTypeDomainService - Unit Tests")
class TaxTypeDomainServiceTest {
    
    private TaxTypeDomainService service;
    
    @BeforeEach
    void setUp() {
        service = new TaxTypeDomainService();
    }
    
    @Test
    @DisplayName("should validate code successfully when format is correct")
    void shouldValidateCodeSuccessfullyWhenFormatIsCorrect() {
        assertThatNoException().isThrownBy(() -> service.validateCode("IVA19"));
        assertThatNoException().isThrownBy(() -> service.validateCode("RETE_SERV_2.5"));
        assertThatNoException().isThrownBy(() -> service.validateCode("ICA-BOG"));
    }
    
    @Test
    @DisplayName("should throw exception when code is blank")
    void shouldThrowExceptionWhenCodeIsBlank() {
        assertThatThrownBy(() -> service.validateCode(""))
            .isInstanceOf(InvalidTaxTypeCodeException.class)
            .hasMessageContaining("cannot be empty");
    }
    
    @Test
    @DisplayName("should throw exception when code exceeds 20 characters")
    void shouldThrowExceptionWhenCodeExceeds20Characters() {
        String longCode = "A".repeat(21);
        assertThatThrownBy(() -> service.validateCode(longCode))
            .isInstanceOf(InvalidTaxTypeCodeException.class)
            .hasMessageContaining("cannot exceed 20 characters");
    }
    
    @Test
    @DisplayName("should throw exception when code contains lowercase")
    void shouldThrowExceptionWhenCodeContainsLowercase() {
        assertThatThrownBy(() -> service.validateCode("iva19"))
            .isInstanceOf(InvalidTaxTypeCodeException.class)
            .hasMessageContaining("must contain only uppercase");
    }
    
    @Test
    @DisplayName("should validate percentage successfully when in valid range")
    void shouldValidatePercentageSuccessfullyWhenInValidRange() {
        assertThatNoException().isThrownBy(() -> service.validatePercentage(new BigDecimal("0.0000")));
        assertThatNoException().isThrownBy(() -> service.validatePercentage(new BigDecimal("19.0000")));
        assertThatNoException().isThrownBy(() -> service.validatePercentage(new BigDecimal("100.0000")));
    }
    
    @Test
    @DisplayName("should throw exception when percentage is negative")
    void shouldThrowExceptionWhenPercentageIsNegative() {
        assertThatThrownBy(() -> service.validatePercentage(new BigDecimal("-5.0000")))
            .isInstanceOf(InvalidTaxPercentageException.class)
            .hasMessageContaining("must be between 0 and 100");
    }
    
    @Test
    @DisplayName("should throw exception when percentage exceeds 100")
    void shouldThrowExceptionWhenPercentageExceeds100() {
        assertThatThrownBy(() -> service.validatePercentage(new BigDecimal("150.0000")))
            .isInstanceOf(InvalidTaxPercentageException.class)
            .hasMessageContaining("must be between 0 and 100");
    }
    
    @Test
    @DisplayName("should throw exception when percentage has more than 4 decimals")
    void shouldThrowExceptionWhenPercentageHasMoreThan4Decimals() {
        assertThatThrownBy(() -> service.validatePercentage(new BigDecimal("19.123456")))
            .isInstanceOf(InvalidTaxPercentageException.class)
            .hasMessageContaining("cannot have more than 4 decimal places");
    }
}
```

---

## 📊 API Endpoints

### Resumen de Endpoints

| Método | Endpoint | Descripción | Query Params | Auth |
|--------|----------|-------------|--------------|------|
| POST | `/api/v1/tax-types` | Crear tipo de impuesto | - | ADMIN |
| GET | `/api/v1/tax-types/{uuid}` | Obtener por UUID | - | USER |
| GET | `/api/v1/tax-types` | Listar/buscar con filtros y paginación | `enabled`, `applicationType`, `name`, `page`, `size`, `sort` | USER |
| PUT | `/api/v1/tax-types/{uuid}` | Actualizar | - | ADMIN |
| PATCH | `/api/v1/tax-types/{uuid}/activate` | Activar | - | ADMIN |
| PATCH | `/api/v1/tax-types/{uuid}/deactivate` | Desactivar | - | ADMIN |
| DELETE | `/api/v1/tax-types/{uuid}` | Eliminar | - | ADMIN |

**Total: 7 endpoints REST**

### Ejemplos de Uso del Endpoint de Búsqueda/Listado

```bash
# Listar todos (paginado)
GET /api/v1/tax-types?page=0&size=20&sort=code,asc

# Listar solo activos
GET /api/v1/tax-types?enabled=true

# Buscar por nombre
GET /api/v1/tax-types?name=IVA

# Filtrar por tipo de aplicación
GET /api/v1/tax-types?applicationType=SALE

# Combinación de filtros
GET /api/v1/tax-types?enabled=true&applicationType=PURCHASE&name=RETE&page=0&size=10
```

---

## 📝 Documentación API (OpenAPI)

La documentación completa estará disponible en:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 🔄 Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-01 | Development Team | Initial draft |
| 2.0 | 2026-02-13 | GitHub Copilot | Spec técnica completa basada en payment-methods |

---

## 📚 References

- [Functional Specification](1-functional-spec.md)
- [Payment Methods Technical Spec](../../03-payment-methods/2-technical-spec.md) (referencia)
- [Scaffolding Base del Proyecto](../../scaffolding.md)
- [Project Info](../../PROJECT_INFO.md)
