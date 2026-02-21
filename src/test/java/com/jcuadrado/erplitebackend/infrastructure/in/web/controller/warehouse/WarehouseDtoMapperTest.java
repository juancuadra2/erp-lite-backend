package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.CreateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.UpdateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.WarehouseResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.warehouse.WarehouseDtoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseDtoMapperTest {

    private final WarehouseDtoMapper mapper = Mappers.getMapper(WarehouseDtoMapper.class);

    @Test
    @DisplayName("toCreateCommand should map all fields from CreateWarehouseRequestDto")
    void toCreateCommand_shouldMapAllFields() {
        UUID municipalityId = UUID.randomUUID();
        CreateWarehouseRequestDto dto = new CreateWarehouseRequestDto(
                "BOD-001", "Bodega", "Desc", WarehouseType.PRINCIPAL,
                "Calle 10", municipalityId, "Juan", "juan@mail.com", "3001234567");

        CreateWarehouseCommand command = mapper.toCreateCommand(dto);

        assertThat(command.code()).isEqualTo("BOD-001");
        assertThat(command.name()).isEqualTo("Bodega");
        assertThat(command.type()).isEqualTo(WarehouseType.PRINCIPAL);
        assertThat(command.municipalityId()).isEqualTo(municipalityId);
        assertThat(command.email()).isEqualTo("juan@mail.com");
    }

    @Test
    @DisplayName("toUpdateCommand should map all fields from UpdateWarehouseRequestDto")
    void toUpdateCommand_shouldMapAllFields() {
        UUID municipalityId = UUID.randomUUID();
        UpdateWarehouseRequestDto dto = new UpdateWarehouseRequestDto(
                "Nuevo Nombre", WarehouseType.SUCURSAL, "Desc actualizada",
                "Carrera 5", municipalityId, "Ana", "ana@mail.com", "3101234567");

        UpdateWarehouseCommand command = mapper.toUpdateCommand(dto);

        assertThat(command.name()).isEqualTo("Nuevo Nombre");
        assertThat(command.type()).isEqualTo(WarehouseType.SUCURSAL);
        assertThat(command.municipalityId()).isEqualTo(municipalityId);
    }

    @Test
    @DisplayName("toResponseDto should map all fields from Warehouse domain")
    void toResponseDto_shouldMapAllFields() {
        UUID uuid = UUID.randomUUID();
        UUID municipalityId = UUID.randomUUID();
        Warehouse domain = Warehouse.builder()
                .uuid(uuid).code("BOD-001").name("Bodega").description("Desc")
                .type(WarehouseType.PRINCIPAL).address("Calle 10")
                .municipalityId(municipalityId).responsible("Juan")
                .email("juan@mail.com").phone("3001234567")
                .active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        WarehouseResponseDto dto = mapper.toResponseDto(domain);

        assertThat(dto.uuid()).isEqualTo(uuid);
        assertThat(dto.code()).isEqualTo("BOD-001");
        assertThat(dto.type()).isEqualTo(WarehouseType.PRINCIPAL);
        assertThat(dto.municipalityId()).isEqualTo(municipalityId);
        assertThat(dto.active()).isTrue();
    }

    @Test
    @DisplayName("toResponseDto should handle null optional fields")
    void toResponseDto_shouldHandleNullOptionalFields() {
        Warehouse domain = Warehouse.builder()
                .uuid(UUID.randomUUID()).code("BOD-001").name("Bodega")
                .type(WarehouseType.TEMPORAL).active(false)
                .createdAt(LocalDateTime.now()).build();

        WarehouseResponseDto dto = mapper.toResponseDto(domain);

        assertThat(dto.description()).isNull();
        assertThat(dto.municipalityId()).isNull();
        assertThat(dto.email()).isNull();
    }
}
