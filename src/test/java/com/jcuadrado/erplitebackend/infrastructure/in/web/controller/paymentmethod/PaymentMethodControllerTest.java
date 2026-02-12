package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.paymentmethod;

import com.jcuadrado.erplitebackend.application.port.paymentmethod.ComparePaymentMethodsUseCase;
import com.jcuadrado.erplitebackend.application.port.paymentmethod.ManagePaymentMethodUseCase;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.DuplicatePaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.CreatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.PaymentMethodResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.UpdatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.paymentmethod.PaymentMethodDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentMethodController
 */
@ExtendWith(MockitoExtension.class)
class PaymentMethodControllerTest {

    @Mock
    private ManagePaymentMethodUseCase manageUseCase;

    @Mock
    private ComparePaymentMethodsUseCase compareUseCase;

    @Mock
    private PaymentMethodDtoMapper mapper;

    @InjectMocks
    private PaymentMethodController controller;

    private PaymentMethodResponseDto createResponseDto(PaymentMethod paymentMethod) {
        return PaymentMethodResponseDto.builder()
                .uuid(paymentMethod.getUuid())
                .code(paymentMethod.getCode())
                .name(paymentMethod.getName())
                .enabled(paymentMethod.getEnabled())
                .createdAt(paymentMethod.getCreatedAt())
                .updatedAt(paymentMethod.getUpdatedAt())
                .build();
    }

    // ==================== create Tests ====================

    @Test
    void create_shouldReturn201WithCreatedPaymentMethod() {
        // Given
        CreatePaymentMethodRequestDto request = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        UUID createdUuid = UUID.randomUUID();
        PaymentMethod domainModel = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        PaymentMethod created = PaymentMethod.builder()
                .id(1L)
                .uuid(createdUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.create(domainModel)).thenReturn(created);
        when(mapper.toResponseDto(created)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<PaymentMethodResponseDto> response = controller.create(request);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(createdUuid, response.getBody().getUuid());
        verify(manageUseCase).create(domainModel);
    }

    @Test
    void create_shouldReturn409WhenCodeAlreadyExists() {
        // Given
        CreatePaymentMethodRequestDto request = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        PaymentMethod domainModel = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.create(domainModel))
                .thenThrow(new DuplicatePaymentMethodCodeException("CASH"));

        // When & Then
        assertThrows(DuplicatePaymentMethodCodeException.class, () -> {
            controller.create(request);
        });
        verify(manageUseCase).create(domainModel);
    }

    // ==================== getByUuid Tests ====================

    @Test
    void getByUuid_shouldReturn200WithPaymentMethod() {
        // Given
        UUID uuid = UUID.randomUUID();
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .uuid(uuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        when(compareUseCase.getByUuid(uuid)).thenReturn(paymentMethod);
        when(mapper.toResponseDto(paymentMethod)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<PaymentMethodResponseDto> response = controller.getByUuid(uuid);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(uuid, response.getBody().getUuid());
        verify(compareUseCase).getByUuid(uuid);
    }

    @Test
    void getByUuid_shouldReturn404WhenNotFound() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(compareUseCase.getByUuid(uuid)).thenThrow(new PaymentMethodNotFoundException("uuid", uuid.toString()));

        // When & Then
        assertThrows(PaymentMethodNotFoundException.class, () -> {
            controller.getByUuid(uuid);
        });
        verify(compareUseCase).getByUuid(uuid);
    }

    // ==================== getByCode Tests ====================

    @Test
    void getByCode_shouldReturn200WithPaymentMethod() {
        // Given
        String code = "CASH";
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code(code)
                .name("Efectivo")
                .enabled(true)
                .build();

        when(compareUseCase.getByCode(code)).thenReturn(paymentMethod);
        when(mapper.toResponseDto(paymentMethod)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<PaymentMethodResponseDto> response = controller.getByCode(code);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(code, response.getBody().getCode());
        verify(compareUseCase).getByCode(code);
    }

    // ==================== listAll Tests ====================

    @Test
    void listAll_shouldReturn200WithPagedPaymentMethods() {
        // Given
        PaymentMethod paymentMethod1 = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        PaymentMethod paymentMethod2 = PaymentMethod.builder()
                .code("CC")
                .name("Tarjeta de Crédito")
                .enabled(true)
                .build();

        List<PaymentMethod> paymentMethods = Arrays.asList(paymentMethod1, paymentMethod2);
        Page<PaymentMethod> page = new PageImpl<>(paymentMethods);

        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDto(any(PaymentMethod.class))).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<?> response = controller.list(null, null, 0, 10, "code", "asc");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(compareUseCase).findAll(anyMap(), any(Pageable.class));
    }

    @Test
    void list_withEnabledFilter_shouldApplyFilter() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        Page<PaymentMethod> page = new PageImpl<>(Collections.singletonList(paymentMethod));

        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDto(any(PaymentMethod.class))).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<?> response = controller.list(Boolean.TRUE, null, 0, 10, "code", "asc");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(compareUseCase).findAll(argThat(filters -> 
            filters.containsKey("enabled") && Boolean.TRUE.equals(filters.get("enabled"))), 
            any(Pageable.class));
    }

    @Test
    void list_withSearchFilter_shouldApplyFilter() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        Page<PaymentMethod> page = new PageImpl<>(Collections.singletonList(paymentMethod));

        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDto(any(PaymentMethod.class))).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<?> response = controller.list(null, "efectivo", 0, 10, "code", "asc");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(compareUseCase).findAll(argThat(filters -> 
            filters.containsKey("search") && "efectivo".equals(filters.get("search"))), 
            any(Pageable.class));
    }

    @Test
    void list_withEmptySearch_shouldNotApplySearchFilter() {
        // Given
        Page<PaymentMethod> page = new PageImpl<>(Collections.emptyList());

        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<?> response = controller.list(null, "  ", 0, 10, "code", "asc");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(compareUseCase).findAll(argThat(filters -> !filters.containsKey("search")), 
            any(Pageable.class));
    }

    @Test
    void list_withDescSortDirection_shouldSortDescending() {
        // Given
        Page<PaymentMethod> page = new PageImpl<>(Collections.emptyList());

        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<?> response = controller.list(null, null, 0, 10, "name", "desc");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(compareUseCase).findAll(anyMap(), any(Pageable.class));
    }

    // ==================== getAllActive Tests ====================

    @Test
    void getAllActive_shouldReturn200WithActivePaymentMethods() {
        // Given
        List<PaymentMethod> activePaymentMethods = Arrays.asList(
                PaymentMethod.builder().code("CASH").name("Efectivo").enabled(true).build(),
                PaymentMethod.builder().code("CC").name("Tarjeta").enabled(true).build()
        );

        when(compareUseCase.getAllActive()).thenReturn(activePaymentMethods);
        when(mapper.toResponseDto(any(PaymentMethod.class))).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<List<PaymentMethodResponseDto>> response = controller.getAllActive();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(compareUseCase).getAllActive();
    }

    // ==================== searchByName Tests ====================

    @Test
    void searchByName_shouldReturn200WithMatchingPaymentMethods() {
        // Given
        String searchTerm = "Tarjeta";
        List<PaymentMethod> searchResults = Arrays.asList(
                PaymentMethod.builder().code("CC").name("Tarjeta de Crédito").enabled(true).build()
        );

        when(compareUseCase.searchByName(searchTerm)).thenReturn(searchResults);
        when(mapper.toResponseDto(any(PaymentMethod.class))).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<List<PaymentMethodResponseDto>> response = controller.searchByName(searchTerm);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(compareUseCase).searchByName(searchTerm);
    }

    // ==================== update Tests ====================

    @Test
    void update_shouldReturn200WithUpdatedPaymentMethod() {
        // Given
        UUID uuid = UUID.randomUUID();
        UpdatePaymentMethodRequestDto request = UpdatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo Actualizado")
                .enabled(true)
                .build();

        PaymentMethod domainModel = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo Actualizado")
                .enabled(true)
                .build();

        PaymentMethod updated = PaymentMethod.builder()
                .uuid(uuid)
                .code("CASH")
                .name("Efectivo Actualizado")
                .enabled(true)
                .updatedAt(LocalDateTime.now())
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.update(uuid, domainModel)).thenReturn(updated);
        when(mapper.toResponseDto(updated)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When
        ResponseEntity<PaymentMethodResponseDto> response = controller.update(uuid, request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Efectivo Actualizado", response.getBody().getName());
        verify(manageUseCase).update(uuid, domainModel);
    }

    // ==================== delete Tests ====================

    @Test
    void delete_shouldReturn204() {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).delete(uuid);

        // When
        ResponseEntity<Void> response = controller.delete(uuid);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(manageUseCase).delete(uuid);
    }

    // ==================== activate Tests ====================

    @Test
    void activate_shouldReturn204() {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).activate(uuid);

        // When
        ResponseEntity<Void> response = controller.activate(uuid);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(manageUseCase).activate(uuid);
    }

    // ==================== deactivate Tests ====================

    @Test
    void deactivate_shouldReturn204() {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).deactivate(uuid);

        // When
        ResponseEntity<Void> response = controller.deactivate(uuid);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(manageUseCase).deactivate(uuid);
    }
}
