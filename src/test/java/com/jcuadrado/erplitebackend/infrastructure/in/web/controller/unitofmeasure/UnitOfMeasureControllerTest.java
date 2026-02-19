package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.unitofmeasure;

import com.jcuadrado.erplitebackend.application.port.unitofmeasure.CompareUnitsOfMeasureUseCase;
import com.jcuadrado.erplitebackend.application.port.unitofmeasure.ManageUnitOfMeasureUseCase;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.CreateUnitOfMeasureRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UnitOfMeasureResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UpdateUnitOfMeasureRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.unitofmeasure.UnitOfMeasureDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureControllerTest {

    @Mock
    private CompareUnitsOfMeasureUseCase compareUseCase;

    @Mock
    private ManageUnitOfMeasureUseCase manageUseCase;

    @Mock
    private UnitOfMeasureDtoMapper mapper;

    private UnitOfMeasureController controller;

    @BeforeEach
    void setUp() {
        controller = new UnitOfMeasureController(manageUseCase, compareUseCase, mapper);
    }

    @Test
    void list_shouldReturnPagedResponse() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).name("Caja").abbreviation("CJ").enabled(true).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Caja", "CJ", true);
        Page<UnitOfMeasure> page = new PageImpl<>(List.of(unit), PageRequest.of(0, 10), 1);
        when(compareUseCase.findAll(eq(Map.of("enabled", true)), any(PageRequest.class))).thenReturn(page);
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<PagedResponseDto<UnitOfMeasureResponseDto>> response = controller.list(true, null, null, 0, 10, "name", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(responseDto);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1L);
    }

    @Test
    void list_shouldPrioritizeNameOverAbbreviationAndAllowDescDirection() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).name("Caja").abbreviation("CJ").enabled(true).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Caja", "CJ", true);
        Page<UnitOfMeasure> page = new PageImpl<>(List.of(unit), PageRequest.of(1, 5), 1);
        when(compareUseCase.findAll(eq(Map.of("enabled", true, "name", "ca")), any(PageRequest.class))).thenReturn(page);
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<PagedResponseDto<UnitOfMeasureResponseDto>> response = controller.list(null, " ca ", "CJ", 1, 5, "name", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCurrentPage()).isEqualTo(1);
        assertThat(response.getBody().getPageSize()).isEqualTo(5);
    }

    @Test
    void list_shouldUseAbbreviationWhenNameIsBlank() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).name("Kilogramo").abbreviation("KG").enabled(true).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Kilogramo", "KG", true);
        Page<UnitOfMeasure> page = new PageImpl<>(List.of(unit), PageRequest.of(0, 10), 1);
        when(compareUseCase.findAll(eq(Map.of("enabled", true, "abbreviation", "kg")), any(PageRequest.class))).thenReturn(page);
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<PagedResponseDto<UnitOfMeasureResponseDto>> response = controller.list(true, " ", " kg ", 0, 10, "name", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(responseDto);
    }

    @Test
    void list_shouldIgnoreBlankAbbreviationWhenNameIsNull() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).name("Unidad").abbreviation("UN").enabled(true).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Unidad", "UN", true);
        Page<UnitOfMeasure> page = new PageImpl<>(List.of(unit), PageRequest.of(0, 10), 1);
        when(compareUseCase.findAll(eq(Map.of("enabled", true)), any(PageRequest.class))).thenReturn(page);
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<PagedResponseDto<UnitOfMeasureResponseDto>> response = controller.list(null, null, " ", 0, 10, "name", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(responseDto);
    }

    @Test
    void getByUuid_shouldReturnOk() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(uuid).build();
        UnitOfMeasureResponseDto dto = responseDto(uuid, "Caja", "CJ", true);
        when(compareUseCase.getByUuid(uuid)).thenReturn(unit);
        when(mapper.toResponseDto(unit)).thenReturn(dto);

        ResponseEntity<UnitOfMeasureResponseDto> response = controller.getByUuid(uuid);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void create_shouldReturnCreated() {
        CreateUnitOfMeasureRequestDto request = new CreateUnitOfMeasureRequestDto();
        request.setName("Caja");
        request.setAbbreviation("CJ");
        UnitOfMeasure requestModel = UnitOfMeasure.builder().name("Caja").abbreviation("CJ").build();
        UnitOfMeasure saved = UnitOfMeasure.builder().uuid(UUID.randomUUID()).name("Caja").abbreviation("CJ").enabled(true).build();
        UnitOfMeasureResponseDto dto = responseDto(saved.getUuid(), "Caja", "CJ", true);

        when(mapper.toDomain(request)).thenReturn(requestModel);
        when(manageUseCase.create(requestModel)).thenReturn(saved);
        when(mapper.toResponseDto(saved)).thenReturn(dto);

        ResponseEntity<UnitOfMeasureResponseDto> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void update_shouldReturnOk() {
        UUID uuid = UUID.randomUUID();
        UpdateUnitOfMeasureRequestDto request = new UpdateUnitOfMeasureRequestDto();
        request.setName("Caja");
        request.setAbbreviation("CJ");
        UnitOfMeasure updates = UnitOfMeasure.builder().name("Caja").abbreviation("CJ").build();
        UnitOfMeasure updated = UnitOfMeasure.builder().uuid(uuid).name("Caja").abbreviation("CJ").enabled(true).build();
        UnitOfMeasureResponseDto dto = responseDto(uuid, "Caja", "CJ", true);

        when(mapper.toDomain(request)).thenReturn(updates);
        when(manageUseCase.update(uuid, updates)).thenReturn(updated);
        when(mapper.toResponseDto(updated)).thenReturn(dto);

        ResponseEntity<UnitOfMeasureResponseDto> response = controller.update(uuid, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void delete_shouldReturnNoContent() {
        UUID uuid = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(uuid);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(manageUseCase).delete(uuid);
    }

    @Test
    void activateAndDeactivate_shouldReturnOk() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure activated = UnitOfMeasure.builder().uuid(uuid).enabled(true).name("Caja").abbreviation("CJ").build();
        UnitOfMeasure deactivated = UnitOfMeasure.builder().uuid(uuid).enabled(false).name("Caja").abbreviation("CJ").build();
        UnitOfMeasureResponseDto activatedDto = responseDto(uuid, "Caja", "CJ", true);
        UnitOfMeasureResponseDto deactivatedDto = responseDto(uuid, "Caja", "CJ", false);

        when(manageUseCase.activate(uuid)).thenReturn(activated);
        when(manageUseCase.deactivate(uuid)).thenReturn(deactivated);
        when(mapper.toResponseDto(activated)).thenReturn(activatedDto);
        when(mapper.toResponseDto(deactivated)).thenReturn(deactivatedDto);

        ResponseEntity<UnitOfMeasureResponseDto> activateResponse = controller.activate(uuid);
        ResponseEntity<UnitOfMeasureResponseDto> deactivateResponse = controller.deactivate(uuid);

        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activateResponse.getBody()).isEqualTo(activatedDto);
        assertThat(deactivateResponse.getBody()).isEqualTo(deactivatedDto);
    }

    @Test
    void search_shouldPrioritizeNameAndDefaultEnabledTrueWhenNull() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Caja", "CJ", true);
        when(compareUseCase.searchByName("ca", true)).thenReturn(List.of(unit));
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<List<UnitOfMeasureResponseDto>> response = controller.search("ca", "x", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(responseDto);
        verify(compareUseCase).searchByName("ca", true);
        verify(compareUseCase, never()).searchByAbbreviation(anyString(), any());
    }

    @Test
    void search_shouldUseAbbreviationWhenNameIsBlank() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Kilo", "KG", false);
        when(compareUseCase.searchByAbbreviation("kg", false)).thenReturn(List.of(unit));
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<List<UnitOfMeasureResponseDto>> response = controller.search(" ", "kg", false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(responseDto);
        verify(compareUseCase).searchByAbbreviation("kg", false);
    }

    @Test
    void search_shouldReturnActiveWhenNoNameOrAbbreviation() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Unidad", "UN", true);
        when(compareUseCase.getAllActive()).thenReturn(List.of(unit));
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<List<UnitOfMeasureResponseDto>> response = controller.search(" ", null, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(responseDto);
        verify(compareUseCase).getAllActive();
    }

    @Test
    void search_shouldReturnActiveWhenAbbreviationIsBlankAndNameIsNull() {
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(UUID.randomUUID()).build();
        UnitOfMeasureResponseDto responseDto = responseDto(unit.getUuid(), "Unidad", "UN", true);
        when(compareUseCase.getAllActive()).thenReturn(List.of(unit));
        when(mapper.toResponseDto(unit)).thenReturn(responseDto);

        ResponseEntity<List<UnitOfMeasureResponseDto>> response = controller.search(null, " ", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(responseDto);
    }

    private UnitOfMeasureResponseDto responseDto(UUID uuid, String name, String abbreviation, boolean enabled) {
        LocalDateTime now = LocalDateTime.now();
        return new UnitOfMeasureResponseDto(
                1L,
                uuid,
                name,
                abbreviation,
                enabled,
                1L,
                null,
                null,
                now,
                null,
                null
        );
    }
}
