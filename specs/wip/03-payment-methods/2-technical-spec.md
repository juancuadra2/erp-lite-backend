# Technical Specification: Módulo de Métodos de Pago

**Feature**: Payment Methods Module  
**Created**: February 1, 2026  
**Updated**: February 11, 2026  
**Related**: [1-functional-spec.md](1-functional-spec.md)  
**Phase**: PHASE 2 - Specification  
**Arquitectura:** Hexagonal (Puertos y Adaptadores)

---

## 📐 Technical Overview

Implementación del módulo de Métodos de Pago siguiendo **Arquitectura Hexagonal** (Ports & Adapters) alineada con el nuevo scaffolding, con separación estricta entre dominio, aplicación e infraestructura. Cada feature tiene su propia carpeta dentro de cada capa para mantener el código organizado y escalable.

### Architecture Style
- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Layers**: Domain → Application → Infrastructure
- **Communication**: Inbound/Outbound Ports
- **Organization**: Feature-based folders within each layer

### Stack Tecnológico
- **Lenguaje**: Java 21
- **Framework**: Spring Boot 3.x
- **ORM**: Spring Data JPA con Hibernate
- **Base de Datos**: MySQL 8.0+
- **Migraciones**: Flyway
- **Mapeo**: MapStruct 1.5.5
- **Validación**: Hibernate Validator (Bean Validation)
- **Pruebas**: JUnit 5, Mockito
- **Documentación de API**: SpringDoc OpenAPI 3

---

## 📦 Estructura de Paquetes (Organizada por Features)

**NOTA**: Cada feature tiene su propia carpeta dentro de cada capa para mantener el código organizado y escalable.

```
com.jcuadrado.erplitebackend/
│
├── domain/
│   ├── model/
│   │   └── paymentmethod/
│   │       ├── PaymentMethod.java                   # Raíz Agregada
│   │       └── PaymentMethodType.java               # Enum
│   ├── service/
│   │   └── paymentmethod/
│   │       ├── PaymentMethodDomainService.java      # Reglas de negocio
│   │       └── PaymentMethodValidationService.java  # Validación de dominio
│   ├── port/
│   │   └── out/
│   │       └── paymentmethod/
│   │           └── PaymentMethodRepository.java     # Puerto de salida
│   └── exception/
│       └── paymentmethod/
│           ├── PaymentMethodNotFoundException.java
│           ├── DuplicatePaymentMethodCodeException.java
│           ├── InvalidPaymentMethodCodeException.java
│           ├── InvalidCommissionException.java
│           ├── InvalidPaymentMethodDataException.java
│           └── PaymentMethodConstraintException.java
│
├── application/
│   ├── port/
│   │   └── in/
│   │       └── paymentmethod/
│   │           ├── ComparePaymentMethodsUseCase.java      # Operaciones de consulta (CQRS - Query)
│   │           └── ManagePaymentMethodUseCase.java        # Operaciones de comando (CQRS - Command)
│   └── usecase/
│       └── paymentmethod/
│           ├── ComparePaymentMethodsUseCaseImpl.java
│           └── ManagePaymentMethodUseCaseImpl.java
│
└── infrastructure/
    ├── config/
    │   └── BeanConfiguration.java                   # Configuración compartida
    │
    ├── in/
    │   └── web/
    │       ├── controller/
    │       │   └── paymentmethod/
    │       │       └── PaymentMethodController.java
    │       ├── dto/
    │       │   └── paymentmethod/
    │       │       ├── CreatePaymentMethodRequestDto.java
    │       │       ├── UpdatePaymentMethodRequestDto.java
    │       │       └── PaymentMethodResponseDto.java
    │       ├── mapper/
    │       │   └── paymentmethod/
    │       │       └── PaymentMethodDtoMapper.java   # MapStruct
    │       └── advice/
    │           └── GlobalExceptionHandler.java       # Compartido
    │
    └── out/
        └── persistence/
            ├── adapter/
            │   └── paymentmethod/
            │       └── PaymentMethodRepositoryAdapter.java
            ├── entity/
            │   └── paymentmethod/
            │       └── PaymentMethodEntity.java      # JPA Entity
            ├── repository/
            │   └── paymentmethod/
            │       └── PaymentMethodJpaRepository.java
            ├── mapper/
            │   └── paymentmethod/
            │       └── PaymentMethodEntityMapper.java # MapStruct
            └── util/
                └── paymentmethod/
                    └── PaymentMethodSpecificationUtil.java
```

---

## 🏗️ Capa de Dominio (Domain Layer)

### Modelos de Dominio

#### `PaymentMethod.java` - Aggregate Root
```java
package com.jcuadrado.erplitebackend.domain.model.paymentmethod;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * PaymentMethod - Raíz de Agregado (Aggregate Root)
 * 
 * Representa un método de pago en el sistema (efectivo, tarjeta, transferencia, etc.).
 * Es el punto de entrada para todas las operaciones relacionadas con métodos de pago.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    private Long id;
    private UUID uuid;
    private String code;                     // Unique, max 30 chars, uppercase + underscores
    private String name;                     // max 100 chars
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
     * Activa el método de pago
     */
    public void activate() {
        this.enabled = true;
        this.deletedBy = null;
        this.deletedAt = null;
    }
    
    /**
     * Desactiva el método de pago (soft delete)
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
}
```


---

### Servicios de Dominio

#### `PaymentMethodDomainService.java`
```java
package com.jcuadrado.erplitebackend.domain.service.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.*;
import java.math.BigDecimal;

/**
 * PaymentMethodDomainService - Servicio de dominio (POJO)
 * 
 * Contiene reglas de negocio que no pertenecen a una entidad específica.
 * Se registra como Bean en BeanConfiguration.
 */
public class PaymentMethodDomainService {
    
    /**
     * Valida el formato del código (BR-PM-001)
     * - No puede ser vacío
     * - Máximo 30 caracteres
     * - Solo letras mayúsculas, números y guiones bajos
     */
    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidPaymentMethodCodeException("Payment method code cannot be empty");
        }
        if (code.length() > 30) {
            throw new InvalidPaymentMethodCodeException("Payment method code cannot exceed 30 characters");
        }
        // Patrón: Solo mayúsculas, números y _
        if (!code.matches("^[A-Z0-9_]+$")) {
            throw new InvalidPaymentMethodCodeException(
                "Payment method code must contain only uppercase letters, numbers, and underscores"
            );
        }
    }
    
    /**
     * Valida el nombre (obligatorio, max 100 chars)
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
     * Determina si un método de pago puede ser eliminado (BR-PM-003)
     * No puede eliminarse si tiene transacciones asociadas
     */
    public boolean canBeDeleted(PaymentMethod paymentMethod, long transactionsCount) {
        return transactionsCount == 0;
    }
}
```

#### `PaymentMethodValidationService.java`
```java
package com.jcuadrado.erplitebackend.domain.service.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.DuplicatePaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.port.out.paymentmethod.PaymentMethodRepository;
import java.util.UUID;

/**
 * PaymentMethodValidationService - Validaciones que requieren acceso a repositorio
 * 
 * Se registra como Bean en BeanConfiguration y recibe el repository port inyectado.
 */
public class PaymentMethodValidationService {
    
    private final PaymentMethodRepository repository;
    
    public PaymentMethodValidationService(PaymentMethodRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Asegura que el código sea único (BR-PM-001)
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
            throw new DuplicatePaymentMethodCodeException(
                "Payment method with code '" + code + "' already exists"
            );
        }
    }
}
```

---

### Excepciones de Dominio

```java
package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

// PaymentMethodNotFoundException.java
public class PaymentMethodNotFoundException extends RuntimeException {
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
    
    public PaymentMethodNotFoundException(UUID uuid) {
        super("Payment method not found with UUID: " + uuid);
    }
}

// DuplicatePaymentMethodCodeException.java
public class DuplicatePaymentMethodCodeException extends RuntimeException {
    public DuplicatePaymentMethodCodeException(String message) {
        super(message);
    }
}

// InvalidPaymentMethodCodeException.java
public class InvalidPaymentMethodCodeException extends RuntimeException {
    public InvalidPaymentMethodCodeException(String message) {
        super(message);
    }
}

// InvalidPaymentMethodDataException.java
public class InvalidPaymentMethodDataException extends RuntimeException {
    public InvalidPaymentMethodDataException(String message) {
        super(message);
    }
}

// PaymentMethodConstraintException.java
public class PaymentMethodConstraintException extends RuntimeException {
    public PaymentMethodConstraintException(String message) {
        super(message);
    }
}
```

---

### Puertos de Salida (Output Ports)

#### `PaymentMethodRepository.java`
```java
package com.jcuadrado.erplitebackend.domain.port.out.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * PaymentMethodRepository - Puerto de salida (Output Port)
 * 
 * Define las operaciones de persistencia sin implementación.
 * Se implementa en la capa de infraestructura (PaymentMethodRepositoryAdapter).
 */
public interface PaymentMethodRepository {
    
    // Operaciones básicas
    PaymentMethod save(PaymentMethod paymentMethod);
    Optional<PaymentMethod> findByUuid(UUID uuid);
    Optional<PaymentMethod> findByCode(String code);
    void delete(PaymentMethod paymentMethod);
    
    // Consultas con filtros
    Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable);
    Page<PaymentMethod> findByEnabled(Boolean enabled, Pageable pageable);
    List<PaymentMethod> findByNameContaining(String searchTerm, Boolean enabled);
    
    // Validaciones
    boolean existsByCode(String code);
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    
    // Consultas para validaciones de negocio
    long countTransactionsWithPaymentMethod(UUID paymentMethodUuid);
}
```

---

## 🎯 Capa de Aplicación (Application Layer)

### Puertos de Entrada (Input Ports - Use Cases)

Seguimos el patrón **CQRS** (Command Query Responsibility Segregation):
- **ComparePaymentMethodsUseCase**: Operaciones de consulta (Query)
- **ManagePaymentMethodUseCase**: Operaciones de comando (Command)

#### `ComparePaymentMethodsUseCase.java` - Queries
```java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ComparePaymentMethodsUseCase - Casos de uso de consulta (CQRS - Query Side)
 */
public interface ComparePaymentMethodsUseCase {
    
    /**
     * Obtiene un método de pago por UUID
     */
    PaymentMethod getByUuid(UUID uuid);
    
    /**
     * Obtiene un método de pago por código
     */
    PaymentMethod getByCode(String code);
    
    /**
     * Obtiene todos los métodos de pago activos
     */
    List<PaymentMethod> getAllActive();
    
    /**
     * Busca métodos de pago con filtros y paginación
     * Filtros soportados: enabled, name
     */
    Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable);
    
    /**
     * Busca métodos de pago por nombre (case-insensitive)
     */
    List<PaymentMethod> searchByName(String searchTerm);
}
```

#### `ManagePaymentMethodUseCase.java` - Commands
```java
package com.jcuadrado.erplitebackend.application.port.in.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import java.util.UUID;

/**
 * ManagePaymentMethodUseCase - Casos de uso de comando (CQRS - Command Side)
 */
public interface ManagePaymentMethodUseCase {
    
    /**
     * Crea un nuevo método de pago
     */
    PaymentMethod create(PaymentMethod paymentMethod);
    
    /**
     * Actualiza un método de pago existente
     */
    PaymentMethod update(UUID uuid, PaymentMethod updates);
    
    /**
     * Activa un método de pago
     */
    void activate(UUID uuid);
    
    /**
     * Desactiva un método de pago (soft delete)
     */
    void deactivate(UUID uuid);
    
    /**
     * Elimina un método de pago (solo si no tiene transacciones)
     */
    void delete(UUID uuid);
}
```

---

### Implementaciones de Casos de Uso

#### `ComparePaymentMethodsUseCaseImpl.java`
```java
package com.jcuadrado.erplitebackend.application.usecase.paymentmethod;

import com.jcuadrado.erplitebackend.application.port.in.paymentmethod.ComparePaymentMethodsUseCase;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethodType;
import com.jcuadrado.erplitebackend.domain.port.out.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodNotFoundException;
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
public class ComparePaymentMethodsUseCaseImpl implements ComparePaymentMethodsUseCase {
    
    private final PaymentMethodRepository repository;
    
    @Override
    @Transactional(readOnly = true)
    public PaymentMethod getByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException(uuid));
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaymentMethod getByCode(String code) {
        return repository.findByCode(code)
            .orElseThrow(() -> new PaymentMethodNotFoundException("Payment method not found with code: " + code));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> getAllActive() {
        return repository.findByNameContaining("", true); // Empty search = all active
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable) {
        Boolean enabled = (Boolean) filters.get("enabled");
        
        if (enabled != null) {
            return repository.findByEnabled(enabled, pageable);
        } else {
            return repository.findAll(filters, pageable);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethod> searchByName(String searchTerm) {
        return repository.findByNameContaining(searchTerm, true);
    }
}
```

#### `ManagePaymentMethodUseCaseImpl.java`
```java
package com.jcuadrado.erplitebackend.application.usecase.paymentmethod;

import com.jcuadrado.erplitebackend.application.port.in.paymentmethod.ManagePaymentMethodUseCase;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.out.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodDomainService;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodValidationService;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagePaymentMethodUseCaseImpl implements ManagePaymentMethodUseCase {
    
    private final PaymentMethodRepository repository;
    private final PaymentMethodDomainService domainService;
    private final PaymentMethodValidationService validationService;
    
    @Override
    @Transactional
    public PaymentMethod create(PaymentMethod paymentMethod) {
        // Validar reglas de dominio
        domainService.validateCode(paymentMethod.getCode());
        domainService.validateName(paymentMethod.getName());
        
        // Validar unicidad del código
        validationService.ensureCodeIsUnique(paymentMethod.getCode(), null);
        
        // Asignar UUID y timestamps
        paymentMethod.setUuid(UUID.randomUUID());
        paymentMethod.setCreatedAt(LocalDateTime.now());
        paymentMethod.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(paymentMethod);
    }
    
    @Override
    @Transactional
    public PaymentMethod update(UUID uuid, PaymentMethod updates) {
        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException(uuid));
        
        // Validar datos actualizados
        domainService.validateName(updates.getName());
        
        // Validar unicidad del código si cambió
        if (!existing.getCode().equals(updates.getCode())) {
            domainService.validateCode(updates.getCode());
            validationService.ensureCodeIsUnique(updates.getCode(), uuid);
            existing.setCode(updates.getCode());
        }
        
        // Actualizar campos
        existing.setName(updates.getName());
        existing.setEnabled(updates.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(existing);
    }
    
    @Override
    @Transactional
    public void activate(UUID uuid) {
        PaymentMethod paymentMethod = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException(uuid));
        
        paymentMethod.activate();
        repository.save(paymentMethod);
    }
    
    @Override
    @Transactional
    public void deactivate(UUID uuid) {
        PaymentMethod paymentMethod = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException(uuid));
        
        // Verificar si puede desactivarse
        long transactionsCount = repository.countTransactionsWithPaymentMethod(uuid);
        if (!domainService.canBeDeleted(paymentMethod, transactionsCount)) {
            throw new PaymentMethodConstraintException(
                "Cannot deactivate payment method with " + transactionsCount + " associated transactions"
            );
        }
        
        paymentMethod.deactivate(null); // TODO: Get current user ID
        repository.save(paymentMethod);
    }
    
    @Override
    @Transactional
    public void delete(UUID uuid) {
        PaymentMethod paymentMethod = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException(uuid));
        
        // Verificar regla de negocio: no puede eliminarse si tiene transacciones
        long transactionsCount = repository.countTransactionsWithPaymentMethod(uuid);
        if (!domainService.canBeDeleted(paymentMethod, transactionsCount)) {
            throw new PaymentMethodConstraintException(
                "Cannot delete payment method with associated transactions"
            );
        }
        
        repository.delete(paymentMethod);
    }
}
```

---

## 🏢 Capa de Infraestructura (Infrastructure Layer)

### Adaptadores de Entrada (Input Adapters - REST API)

#### `PaymentMethodController.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.paymentmethod;

import com.jcuadrado.erplitebackend.application.port.in.paymentmethod.*;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethodType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.*;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.paymentmethod.PaymentMethodDtoMapper;
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
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Methods", description = "API de gestión de métodos de pago")
public class PaymentMethodController {
    
    private final ComparePaymentMethodsUseCase compareUseCase;
    private final ManagePaymentMethodUseCase manageUseCase;
    private final PaymentMethodDtoMapper dtoMapper;
    
    @PostMapping
    @Operation(summary = "Crear nuevo método de pago", description = "Crea un nuevo método de pago en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Método de pago creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Código duplicado")
    })
    public ResponseEntity<PaymentMethodResponseDto> create(
            @Valid @RequestBody CreatePaymentMethodRequestDto request) {
        PaymentMethod domain = dtoMapper.toDomain(request);
        PaymentMethod created = manageUseCase.create(domain);
        PaymentMethodResponseDto response = dtoMapper.toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{uuid}")
    @Operation(summary = "Obtener método de pago por UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Método encontrado"),
        @ApiResponse(responseCode = "404", description = "Método no encontrado")
    })
    public ResponseEntity<PaymentMethodResponseDto> getByUuid(
            @Parameter(description = "UUID del método de pago") 
            @PathVariable UUID uuid) {
        PaymentMethod paymentMethod = compareUseCase.getByUuid(uuid);
        PaymentMethodResponseDto response = dtoMapper.toResponseDto(paymentMethod);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{uuid}")
    @Operation(summary = "Actualizar método de pago")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Método actualizado"),
        @ApiResponse(responseCode = "404", description = "Método no encontrado"),
        @ApiResponse(responseCode = "409", description = "Código duplicado")
    })
    public ResponseEntity<PaymentMethodResponseDto> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdatePaymentMethodRequestDto request) {
        PaymentMethod updates = dtoMapper.toDomain(request);
        PaymentMethod updated = manageUseCase.update(uuid, updates);
        PaymentMethodResponseDto response = dtoMapper.toResponseDto(updated);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{uuid}/activate")
    @Operation(summary = "Activar método de pago")
    public ResponseEntity<Void> activate(@PathVariable UUID uuid) {
        manageUseCase.activate(uuid);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{uuid}/deactivate")
    @Operation(summary = "Desactivar método de pago (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Método desactivado"),
        @ApiResponse(responseCode = "409", description = "No puede desactivarse (tiene transacciones)")
    })
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        manageUseCase.deactivate(uuid);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{uuid}")
    @Operation(summary = "Eliminar método de pago (solo si no tiene transacciones)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Método eliminado"),
        @ApiResponse(responseCode = "409", description = "No puede eliminarse (tiene transacciones)")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    @Operation(summary = "Listar métodos de pago con filtros y paginación")
    public ResponseEntity<Page<PaymentMethodResponseDto>> list(
            @Parameter(description = "Filtrar por estado activo/inactivo")
            @RequestParam(required = false) Boolean enabled,
            
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        Map<String, Object> filters = new HashMap<>();
        if (enabled != null) filters.put("enabled", enabled);
        
        Page<PaymentMethod> page = compareUseCase.findAll(filters, pageable);
        Page<PaymentMethodResponseDto> response = page.map(dtoMapper::toResponseDto);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Buscar métodos de pago por nombre (case-insensitive)")
    public ResponseEntity<List<PaymentMethodResponseDto>> search(
            @Parameter(description = "Término de búsqueda")
            @RequestParam String name) {
        List<PaymentMethod> results = compareUseCase.searchByName(name);
        List<PaymentMethodResponseDto> response = results.stream()
            .map(dtoMapper::toResponseDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
```

#### DTOs

```java
// CreatePaymentMethodRequestDto.java
package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod;

import jakarta.validation.constraints.*;
import lombok.*;

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
}

// UpdatePaymentMethodRequestDto.java
// Similar structure...

// PaymentMethodResponseDto.java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponseDto {
    private UUID uuid;
    private String code;
    private String name;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### `PaymentMethodDtoMapper.java` (MapStruct)
```java
package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMethodDtoMapper {
    
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    PaymentMethod toDomain(CreatePaymentMethodRequestDto dto);
    
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    PaymentMethod toDomain(UpdatePaymentMethodRequestDto dto);
    
    PaymentMethodResponseDto toResponseDto(PaymentMethod domain);
}
```

---

### Adaptadores de Salida (Output Adapters - Persistence)

#### `PaymentMethodEntity.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_methods", indexes = {
    @Index(name = "idx_payment_method_uuid", columnList = "uuid", unique = true),
    @Index(name = "idx_payment_method_code", columnList = "code", unique = true),
    @Index(name = "idx_payment_method_enabled", columnList = "enabled")
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
    
    @Column(nullable = false)
    private Boolean enabled;
    
    // Auditoría
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

#### `PaymentMethodJpaRepository.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.repository.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodJpaRepository extends 
        JpaRepository<PaymentMethodEntity, Long>,
        JpaSpecificationExecutor<PaymentMethodEntity> {
    
    Optional<PaymentMethodEntity> findByUuid(UUID uuid);
    Optional<PaymentMethodEntity> findByCode(String code);
    Page<PaymentMethodEntity> findByEnabled(Boolean enabled, Pageable pageable);
    List<PaymentMethodEntity> findByNameContainingIgnoreCaseAndEnabled(String name, Boolean enabled);
    boolean existsByCode(String code);
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    
    // Contadores para validaciones (se implementarán cuando existan los módulos Sales/Purchases)
    @Query("SELECT 0")
    long countSalesWithPaymentMethod(@Param("paymentMethodUuid") UUID paymentMethodUuid);
    
    @Query("SELECT 0")
    long countPurchasesWithPaymentMethod(@Param("paymentMethodUuid") UUID paymentMethodUuid);
}
```

#### `PaymentMethodEntityMapper.java` (MapStruct)
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMethodEntityMapper {
    
    PaymentMethodEntity toEntity(PaymentMethod domain);
    
    PaymentMethod toDomain(PaymentMethodEntity entity);
}
```

#### `PaymentMethodRepositoryAdapter.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.out.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.repository.paymentmethod.PaymentMethodJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.paymentmethod.PaymentMethodEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.paymentmethod.PaymentMethodSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {
    
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
    public void delete(PaymentMethod paymentMethod) {
        PaymentMethodEntity entity = entityMapper.toEntity(paymentMethod);
        jpaRepository.delete(entity);
    }
    
    @Override
    public Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec, pageable)
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
    public List<PaymentMethod> findByNameContaining(String searchTerm, Boolean enabled) {
        return jpaRepository.findByNameContainingIgnoreCaseAndEnabled(searchTerm, enabled).stream()
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
}
```

#### `PaymentMethodSpecificationUtil.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethodType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PaymentMethodSpecificationUtil - Builder de especificaciones JPA
 * 
 * Construye filtros dinámicos usando Criteria API de JPA.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentMethodSpecificationUtil {
    
    public static Specification<PaymentMethodEntity> buildSpecification(Map<String, Object> filters) {
        List<Specification<PaymentMethodEntity>> specs = new ArrayList<>();
        
        if (filters.containsKey("enabled")) {
            Boolean enabled = (Boolean) filters.get("enabled");
            specs.add(hasEnabled(enabled));
        }
        
        if (filters.containsKey("type")) {
            PaymentMethodType type = (PaymentMethodType) filters.get("type");
            specs.add(hasType(type));
        }
        
        if (filters.containsKey("name")) {
            String name = (String) filters.get("name");
            specs.add(nameContains(name));
        }
        
        return specs.stream()
            .reduce(Specification::and)
            .orElse(null);
    }
    
    public static Specification<PaymentMethodEntity> hasEnabled(Boolean enabled) {
        return (root, query, cb) -> cb.equal(root.get("enabled"), enabled);
    }
    
    public static Specification<PaymentMethodEntity> nameContains(String name) {
        return (root, query, cb) -> 
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
```

---

## 🗄️ Base de Datos

### Migración Flyway - V1.5__create_payment_methods_table.sql

```sql
-- V1.5__create_payment_methods_table.sql

CREATE TABLE payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    INDEX idx_payment_method_uuid (uuid),
    INDEX idx_payment_method_code (code),
    INDEX idx_payment_method_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE payment_methods COMMENT = 'Catálogo de métodos de pago (Efectivo, Tarjeta, Transferencia, PSE, etc.)';
```

### Migración Flyway - V1.6__insert_colombia_payment_methods.sql

```sql
-- V1.6__insert_colombia_payment_methods.sql

INSERT INTO payment_methods (uuid, code, name, enabled, created_at, updated_at)
VALUES
    (UUID(), 'CASH', 'Efectivo', TRUE, NOW(), NOW()),
    (UUID(), 'CC', 'Tarjeta de Crédito', TRUE, NOW(), NOW()),
    (UUID(), 'DC', 'Tarjeta Débito', TRUE, NOW(), NOW()),
    (UUID(), 'TRANSFER', 'Transferencia Bancaria', TRUE, NOW(), NOW()),
    (UUID(), 'PSE', 'PSE - Pagos Seguros en Línea', TRUE, NOW(), NOW()),
    (UUID(), 'CHECK', 'Cheque', TRUE, NOW(), NOW()),
    (UUID(), 'CREDIT', 'Crédito', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    updated_at = NOW();
```

---

## 🧪 Testing Strategy

### Estructura de Tests
```
test/
└── java/
    └── com/jcuadrado/erplitebackend/
        ├── domain/
        │   ├── model/
        │   │   └── paymentmethod/
        │   │       └── PaymentMethodTest.java
        │   └── service/
        │       └── paymentmethod/
        │           ├── PaymentMethodDomainServiceTest.java
        │           └── PaymentMethodValidationServiceTest.java
        ├── application/
        │   └── usecase/
        │       └── paymentmethod/
        │           ├── ComparePaymentMethodsUseCaseTest.java
        │           └── ManagePaymentMethodUseCaseTest.java
        └── infrastructure/
            ├── in/web/
            │   ├── controller/
            │   │   └── paymentmethod/
            │   │       └── PaymentMethodControllerTest.java
            │   └── mapper/
            │       └── paymentmethod/
            │           └── PaymentMethodDtoMapperTest.java
            └── out/persistence/
                ├── adapter/
                │   └── paymentmethod/
                │       └── PaymentMethodRepositoryAdapterTest.java
                ├── mapper/
                │   └── paymentmethod/
                │       └── PaymentMethodEntityMapperTest.java
                └── util/
                    └── paymentmethod/
                        └── PaymentMethodSpecificationUtilTest.java
```

### Test Coverage Targets
- **Domain Layer**: >= 95%
- **Application Layer**: >= 90%
- **Infrastructure Layer**: >= 85%
- **Overall**: >= 85%

### Test Technologies
- **JUnit 5**: Framework de pruebas
- **Mockito**: Mocking
- **AssertJ**: Assertions fluidas
- **Spring Boot Test**: Tests de integración (FUERA DEL SCOPE)

---

## 📋 Configuración de Beans

### `BeanConfiguration.java`
```java
package com.jcuadrado.erplitebackend.infrastructure.config;

import com.jcuadrado.erplitebackend.domain.port.out.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodDomainService;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    
    // ... beans existentes ...
    
    @Bean
    public PaymentMethodDomainService paymentMethodDomainService() {
        return new PaymentMethodDomainService();
    }
    
    @Bean
    public PaymentMethodValidationService paymentMethodValidationService(
            PaymentMethodRepository paymentMethodRepository) {
        return new PaymentMethodValidationService(paymentMethodRepository);
    }
}
```

---

## 🔗 API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payment-methods` | Crear método de pago |
| GET | `/api/payment-methods/{uuid}` | Obtener por UUID |
| PUT | `/api/payment-methods/{uuid}` | Actualizar método |
| PATCH | `/api/payment-methods/{uuid}/activate` | Activar método |
| PATCH | `/api/payment-methods/{uuid}/deactivate` | Desactivar método |
| DELETE | `/api/payment-methods/{uuid}` | Eliminar método |
| GET | `/api/payment-methods` | Listar con filtros |
| GET | `/api/payment-methods/search` | Buscar por nombre |

---

## 📝 Change Log

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-01 | Development Team | Initial version |
| 1.1 | 2026-02-11 | GitHub Copilot | Updated to match geography format, added MapStruct, detailed architecture |

---

## 📚 References

- [Scaffolding Base del Proyecto](../../scaffolding.md)
- [Functional Specification](1-functional-spec.md)
- [Geography Module](../geography/) - Módulo de referencia
- [Document Types Module](../document-types/) - Módulo similiar
