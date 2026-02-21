package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.port.warehouse.CompareWarehouseUseCase;
import com.jcuadrado.erplitebackend.application.port.warehouse.ManageWarehouseUseCase;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.CreateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.UpdateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.WarehouseResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.warehouse.WarehouseDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseControllerTest {

    @Mock private ManageWarehouseUseCase manageUseCase;
    @Mock private CompareWarehouseUseCase compareUseCase;
    @Mock private WarehouseDtoMapper mapper;

    private WarehouseController controller;

    @BeforeEach
    void setUp() {
        controller = new WarehouseController(manageUseCase, compareUseCase, mapper);
    }

    private Warehouse sampleWarehouse() {
        return Warehouse.builder().uuid(UUID.randomUUID()).code("BOD-001").name("Bodega")
                .type(WarehouseType.SUCURSAL).active(true).createdAt(LocalDateTime.now()).build();
    }

    private WarehouseResponseDto sampleResponseDto(Warehouse w) {
        return new WarehouseResponseDto(w.getUuid(), w.getCode(), w.getName(), null,
                w.getType(), null, null, null, null, null, w.isActive(), w.getCreatedAt(), null);
    }

    @Test
    @DisplayName("create should return 201 with location header")
    void create_shouldReturn201() {
        Warehouse warehouse = sampleWarehouse();
        CreateWarehouseRequestDto request = new CreateWarehouseRequestDto(
                "BOD-001", "Bodega", null, WarehouseType.SUCURSAL,
                null, null, null, null, null);
        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "BOD-001", "Bodega", null, WarehouseType.SUCURSAL,
                null, null, null, null, null);

        when(mapper.toCreateCommand(request)).thenReturn(command);
        when(manageUseCase.create(command)).thenReturn(warehouse);
        when(mapper.toResponseDto(warehouse)).thenReturn(sampleResponseDto(warehouse));

        ResponseEntity<WarehouseResponseDto> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("findByUuid should return 200 with warehouse")
    void findByUuid_shouldReturn200() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse();

        when(compareUseCase.findByUuid(uuid)).thenReturn(warehouse);
        when(mapper.toResponseDto(warehouse)).thenReturn(sampleResponseDto(warehouse));

        ResponseEntity<WarehouseResponseDto> response = controller.findByUuid(uuid);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("list should return 200 with PagedResponseDto")
    void list_shouldReturn200WithPagedResponse() {
        Warehouse warehouse = sampleWarehouse();
        WarehouseResponseDto dto = sampleResponseDto(warehouse);
        Page<Warehouse> page = new PageImpl<>(List.of(warehouse), PageRequest.of(0, 20), 1);

        when(compareUseCase.findAll(any(Map.class), any())).thenReturn(page);
        when(mapper.toResponseDto(warehouse)).thenReturn(dto);

        ResponseEntity<PagedResponseDto<WarehouseResponseDto>> response =
                controller.list(true, null, null, null, null, 0, 20, "name", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("list should build filters map with provided params")
    void list_shouldBuildFiltersMap() {
        UUID municipalityId = UUID.randomUUID();
        Page<Warehouse> emptyPage = new PageImpl<>(List.of());

        when(compareUseCase.findAll(any(Map.class), any())).thenReturn(emptyPage);

        controller.list(false, "SUCURSAL", municipalityId, "bodega", "BOD", 0, 20, "name", "asc");

        verify(compareUseCase).findAll(any(Map.class), any());
    }

    @Test
    @DisplayName("update should return 200 with updated warehouse")
    void update_shouldReturn200() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse();
        UpdateWarehouseRequestDto request = new UpdateWarehouseRequestDto(
                "Nuevo", WarehouseType.SUCURSAL, null, null, null, null, null, null);
        UpdateWarehouseCommand command = new UpdateWarehouseCommand(
                "Nuevo", null, WarehouseType.SUCURSAL, null, null, null, null, null);

        when(mapper.toUpdateCommand(request)).thenReturn(command);
        when(manageUseCase.update(uuid, command)).thenReturn(warehouse);
        when(mapper.toResponseDto(warehouse)).thenReturn(sampleResponseDto(warehouse));

        ResponseEntity<WarehouseResponseDto> response = controller.update(uuid, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("delete should return 204")
    void delete_shouldReturn204() {
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).delete(uuid);

        ResponseEntity<Void> response = controller.delete(uuid);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("activate should return 200 with activated warehouse")
    void activate_shouldReturn200() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse();

        when(manageUseCase.activate(uuid)).thenReturn(warehouse);
        when(mapper.toResponseDto(warehouse)).thenReturn(sampleResponseDto(warehouse));

        ResponseEntity<WarehouseResponseDto> response = controller.activate(uuid);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().active()).isTrue();
    }

    @Test
    @DisplayName("deactivate should return 200 with deactivated warehouse")
    void deactivate_shouldReturn200() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse();
        warehouse.deactivate();
        WarehouseResponseDto dto = sampleResponseDto(warehouse);

        when(manageUseCase.deactivate(uuid)).thenReturn(warehouse);
        when(mapper.toResponseDto(warehouse)).thenReturn(dto);

        ResponseEntity<WarehouseResponseDto> response = controller.deactivate(uuid);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("list should sort descending when direction is desc")
    void list_shouldSortDescending_whenDirectionIsDesc() {
        Page<Warehouse> emptyPage = new PageImpl<>(List.of());
        when(compareUseCase.findAll(any(Map.class), any())).thenReturn(emptyPage);

        ResponseEntity<PagedResponseDto<WarehouseResponseDto>> response =
                controller.list(null, null, null, null, null, 0, 20, "name", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
