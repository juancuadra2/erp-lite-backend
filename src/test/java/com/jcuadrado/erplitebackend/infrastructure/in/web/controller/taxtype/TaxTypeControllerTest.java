package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.taxtype;

import com.jcuadrado.erplitebackend.application.port.taxtype.CompareTaxTypesUseCase;
import com.jcuadrado.erplitebackend.application.port.taxtype.ManageTaxTypeUseCase;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.CreateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.TaxTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.UpdateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.taxtype.TaxTypeDtoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TaxTypeController - Unit Tests")
@ExtendWith(MockitoExtension.class)
class TaxTypeControllerTest {

    @Mock
    private ManageTaxTypeUseCase manageUseCase;

    @Mock
    private CompareTaxTypesUseCase compareUseCase;

    @Mock
    private TaxTypeDtoMapper mapper;

    @InjectMocks
    private TaxTypeController controller;

    private TaxTypeResponseDto createResponseDto(TaxType taxType) {
        return new TaxTypeResponseDto(
                1L,
                taxType.getUuid(),
                taxType.getCode(),
                taxType.getName(),
                taxType.getPercentage(),
                taxType.getIsIncluded(),
                taxType.getApplicationType(),
                taxType.getEnabled(),
                1L,
                1L,
                null,
                taxType.getCreatedAt(),
                taxType.getUpdatedAt(),
                null
        );
    }

    @Test
    @DisplayName("create should return 201 with created tax type")
    void create_shouldReturn201WithCreatedTaxType() {
        CreateTaxTypeRequestDto request = CreateTaxTypeRequestDto.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .build();

        UUID createdUuid = UUID.randomUUID();
        TaxType domainModel = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .build();

        TaxType created = TaxType.builder()
                .id(1L)
                .uuid(createdUuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.create(domainModel)).thenReturn(created);
        when(mapper.toResponseDto(created)).thenAnswer(invocation ->
                createResponseDto(invocation.getArgument(0)));

        ResponseEntity<TaxTypeResponseDto> response = controller.create(request);

        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(createdUuid, response.getBody().uuid());
        verify(manageUseCase).create(domainModel);
    }

    @Test
    @DisplayName("create should throw exception when code already exists")
    void create_shouldThrowExceptionWhenCodeExists() {
        CreateTaxTypeRequestDto request = CreateTaxTypeRequestDto.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .build();

        TaxType domainModel = TaxType.builder().code("IVA19").build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.create(domainModel))
                .thenThrow(new DuplicateTaxTypeCodeException("Code already exists"));

        assertThrows(DuplicateTaxTypeCodeException.class, () -> controller.create(request));
        verify(manageUseCase).create(domainModel);
    }

    @Test
    @DisplayName("getByUuid should return 200 with tax type")
    void getByUuid_shouldReturn200WithTaxType() {
        UUID uuid = UUID.randomUUID();
        TaxType taxType = TaxType.builder()
                .uuid(uuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .enabled(true)
                .build();

        when(compareUseCase.getByUuid(uuid)).thenReturn(taxType);
        when(mapper.toResponseDto(taxType)).thenAnswer(invocation ->
                createResponseDto(invocation.getArgument(0)));

        ResponseEntity<TaxTypeResponseDto> response = controller.getByUuid(uuid);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(uuid, response.getBody().uuid());
        verify(compareUseCase).getByUuid(uuid);
    }

    @Test
    @DisplayName("getByUuid should throw exception when not found")
    void getByUuid_shouldThrowExceptionWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(compareUseCase.getByUuid(uuid)).thenThrow(new TaxTypeNotFoundException(uuid));

        assertThrows(TaxTypeNotFoundException.class, () -> controller.getByUuid(uuid));
        verify(compareUseCase).getByUuid(uuid);
    }

    @Test
    @DisplayName("list should return 200 with paged tax types")
    void list_shouldReturn200WithPagedTaxTypes() {
        List<TaxType> taxTypes = Arrays.asList(
                TaxType.builder().uuid(UUID.randomUUID()).code("IVA19").build(),
                TaxType.builder().uuid(UUID.randomUUID()).code("IVA5").build()
        );
        Page<TaxType> page = new PageImpl<>(taxTypes);
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        filters.put("applicationType", TaxApplicationType.BOTH);
        filters.put("name", "IVA");

        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDto(any())).thenAnswer(invocation ->
                createResponseDto(invocation.getArgument(0)));

        ResponseEntity<PagedResponseDto<TaxTypeResponseDto>> response = controller.list(
                true, TaxApplicationType.BOTH, "IVA",
                0, 10, "code", "asc"
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    @DisplayName("update should return 200 with updated tax type")
    void update_shouldReturn200WithUpdatedTaxType() {
        UUID uuid = UUID.randomUUID();
        UpdateTaxTypeRequestDto request = UpdateTaxTypeRequestDto.builder()
                .name("IVA 19% Actualizado")
                .build();

        TaxType updates = TaxType.builder().name("IVA 19% Actualizado").build();
        TaxType updated = TaxType.builder()
                .uuid(uuid)
                .code("IVA19")
                .name("IVA 19% Actualizado")
                .build();

        when(mapper.toDomain(request)).thenReturn(updates);
        when(manageUseCase.update(uuid, updates)).thenReturn(updated);
        when(mapper.toResponseDto(updated)).thenAnswer(invocation ->
                createResponseDto(invocation.getArgument(0)));

        ResponseEntity<TaxTypeResponseDto> response = controller.update(uuid, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("IVA 19% Actualizado", response.getBody().name());
        verify(manageUseCase).update(uuid, updates);
    }

    @Test
        @DisplayName("activate should return 200 with tax type")
        void activate_shouldReturn200WithTaxType() {
        UUID uuid = UUID.randomUUID();
                TaxType activated = TaxType.builder()
                                .uuid(uuid)
                                .code("IVA19")
                                .name("IVA 19%")
                                .enabled(true)
                                .build();

        doNothing().when(manageUseCase).activate(uuid);
                when(compareUseCase.getByUuid(uuid)).thenReturn(activated);
                when(mapper.toResponseDto(activated)).thenAnswer(invocation ->
                                createResponseDto(invocation.getArgument(0)));

                ResponseEntity<TaxTypeResponseDto> response = controller.activate(uuid);

        assertNotNull(response);
                assertEquals(200, response.getStatusCode().value());
                assertNotNull(response.getBody());
                assertEquals(uuid, response.getBody().uuid());
        verify(manageUseCase).activate(uuid);
                verify(compareUseCase).getByUuid(uuid);
    }

    @Test
        @DisplayName("deactivate should return 200 with tax type")
        void deactivate_shouldReturn200WithTaxType() {
        UUID uuid = UUID.randomUUID();
                TaxType deactivated = TaxType.builder()
                                .uuid(uuid)
                                .code("IVA19")
                                .name("IVA 19%")
                                .enabled(false)
                                .build();

        doNothing().when(manageUseCase).deactivate(uuid);
                when(compareUseCase.getByUuid(uuid)).thenReturn(deactivated);
                when(mapper.toResponseDto(deactivated)).thenAnswer(invocation ->
                                createResponseDto(invocation.getArgument(0)));

                ResponseEntity<TaxTypeResponseDto> response = controller.deactivate(uuid);

        assertNotNull(response);
                assertEquals(200, response.getStatusCode().value());
                assertNotNull(response.getBody());
                assertEquals(uuid, response.getBody().uuid());
        verify(manageUseCase).deactivate(uuid);
                verify(compareUseCase).getByUuid(uuid);
    }

    @Test
    @DisplayName("delete should return 204")
    void delete_shouldReturn204() {
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).delete(uuid);

        ResponseEntity<Void> response = controller.delete(uuid);

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
        verify(manageUseCase).delete(uuid);
    }
}
