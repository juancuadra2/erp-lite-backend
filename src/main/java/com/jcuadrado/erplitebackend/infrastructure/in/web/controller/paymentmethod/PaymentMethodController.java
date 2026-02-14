package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.paymentmethod;

import com.jcuadrado.erplitebackend.application.port.paymentmethod.ComparePaymentMethodsUseCase;
import com.jcuadrado.erplitebackend.application.port.paymentmethod.ManagePaymentMethodUseCase;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.CreatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.PaymentMethodResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.UpdatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.paymentmethod.PaymentMethodDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Methods", description = "API for managing payment methods (Cash, Credit Card, Transfer, etc.)")
public class PaymentMethodController {

    private final ManagePaymentMethodUseCase manageUseCase;
    private final ComparePaymentMethodsUseCase compareUseCase;
    private final PaymentMethodDtoMapper mapper;

    @Operation(summary = "Create payment method", description = "Creates a new payment method with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment method created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Payment method code already exists")
    })
    @PostMapping
    public ResponseEntity<PaymentMethodResponseDto> create(@Valid @RequestBody CreatePaymentMethodRequestDto request) {
        log.info("Creating payment method with code: {}", request.getCode());

        PaymentMethod domain = mapper.toDomain(request);
        PaymentMethod created = manageUseCase.create(domain);
        PaymentMethodResponseDto response = mapper.toResponseDto(created);

        log.info("Payment method created successfully with UUID: {}", created.getUuid());

        URI location = URI.create("/api/v1/payment-methods/" + created.getUuid());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get payment method by UUID", description = "Retrieves a payment method by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment method found"),
        @ApiResponse(responseCode = "404", description = "Payment method not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<PaymentMethodResponseDto> getByUuid(
            @Parameter(description = "UUID of the payment method", required = true)
            @PathVariable UUID uuid) {
        log.debug("Retrieving payment method by UUID: {}", uuid);

        PaymentMethod paymentMethod = compareUseCase.getByUuid(uuid);

        log.debug("Payment method found: {}", paymentMethod.getCode());
        return ResponseEntity.ok(mapper.toResponseDto(paymentMethod));
    }

    @Operation(summary = "Get payment method by code", description = "Retrieves a payment method by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment method found"),
        @ApiResponse(responseCode = "404", description = "Payment method not found")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<PaymentMethodResponseDto> getByCode(
            @Parameter(description = "Code of the payment method", required = true)
            @PathVariable String code) {
        PaymentMethod paymentMethod = compareUseCase.getByCode(code);
        return ResponseEntity.ok(mapper.toResponseDto(paymentMethod));
    }

    @Operation(summary = "List payment methods", description = "Lists payment methods with filtering, searching, pagination and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment methods retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<PaymentMethodResponseDto>> list(
            @Parameter(description = "Filter by enabled status")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Global search in code and name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Items per page")
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "Sort field")
            @RequestParam(name = "sort", required = false, defaultValue = "name") String sortField,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String sortDirection) {

        Map<String, Object> filters = new HashMap<>();
        if (enabled != null) {
            filters.put("enabled", enabled);
        }
        if (search != null && !search.trim().isEmpty()) {
            filters.put("search", search.trim());
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<PaymentMethod> domainPage = compareUseCase.findAll(filters, pageable);

        List<PaymentMethodResponseDto> dtoList = domainPage.getContent().stream()
            .map(mapper::toResponseDto)
            .collect(Collectors.toList());

        PagedResponseDto<PaymentMethodResponseDto> response = PagedResponseDto.<PaymentMethodResponseDto>builder()
            .content(dtoList)
            .totalElements(domainPage.getTotalElements())
            .totalPages(domainPage.getTotalPages())
            .currentPage(domainPage.getNumber())
            .pageSize(domainPage.getSize())
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active payment methods", description = "Retrieves all active payment methods without pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active payment methods retrieved successfully")
    })
    @GetMapping("/active")
    public ResponseEntity<List<PaymentMethodResponseDto>> getAllActive() {
        List<PaymentMethod> activePaymentMethods = compareUseCase.getAllActive();
        List<PaymentMethodResponseDto> response = activePaymentMethods.stream()
            .map(mapper::toResponseDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search payment methods by name", description = "Search payment methods by name containing the search term (case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment methods found")
    })
    @GetMapping("/search")
    public ResponseEntity<List<PaymentMethodResponseDto>> searchByName(
            @Parameter(description = "Name search term", required = true)
            @RequestParam String name) {
        List<PaymentMethod> found = compareUseCase.searchByName(name);
        List<PaymentMethodResponseDto> response = found.stream()
            .map(mapper::toResponseDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update payment method", description = "Updates an existing payment method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment method updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Payment method not found"),
        @ApiResponse(responseCode = "409", description = "Payment method code already exists")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<PaymentMethodResponseDto> update(
            @Parameter(description = "UUID of the payment method", required = true)
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdatePaymentMethodRequestDto request) {
        PaymentMethod domain = mapper.toDomain(request);
        PaymentMethod updated = manageUseCase.update(uuid, domain);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Delete payment method", description = "Soft deletes a payment method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Payment method deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Payment method not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete payment method with associated transactions")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the payment method", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate payment method", description = "Activates a payment method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment method activated successfully"),
        @ApiResponse(responseCode = "404", description = "Payment method not found")
    })
    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<PaymentMethodResponseDto> activate(
            @Parameter(description = "UUID of the payment method", required = true)
            @PathVariable UUID uuid) {
        PaymentMethod activated = manageUseCase.activate(uuid);
        return ResponseEntity.ok(mapper.toResponseDto(activated));
    }

    @Operation(summary = "Deactivate payment method", description = "Deactivates a payment method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment method deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Payment method not found")
    })
    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<PaymentMethodResponseDto> deactivate(
            @Parameter(description = "UUID of the payment method", required = true)
            @PathVariable UUID uuid) {
        PaymentMethod deactivated = manageUseCase.deactivate(uuid);
        return ResponseEntity.ok(mapper.toResponseDto(deactivated));
    }
}
