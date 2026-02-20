package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.warehouse.WarehouseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseEntityMapperTest {

    private final WarehouseEntityMapper mapper = Mappers.getMapper(WarehouseEntityMapper.class);

    @Test
    @DisplayName("toDomain should map all fields including municipalityUuid to municipalityId")
    void toDomain_shouldMapAllFields() {
        String uuidStr = UUID.randomUUID().toString();
        String municipalityUuid = UUID.randomUUID().toString();

        WarehouseEntity entity = WarehouseEntity.builder()
                .id(1L)
                .uuid(uuidStr)
                .code("BOD-001")
                .name("Bodega Principal")
                .description("Desc")
                .type(WarehouseType.PRINCIPAL)
                .address("Calle 10")
                .municipalityUuid(municipalityUuid)
                .responsible("Juan")
                .email("juan@mail.com")
                .phone("3001234567")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Warehouse domain = mapper.toDomain(entity);

        assertThat(domain.getUuid()).hasToString(uuidStr);
        assertThat(domain.getCode()).isEqualTo("BOD-001");
        assertThat(domain.getName()).isEqualTo("Bodega Principal");
        assertThat(domain.getType()).isEqualTo(WarehouseType.PRINCIPAL);
        assertThat(domain.getMunicipalityId()).hasToString(municipalityUuid);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("toDomain should handle null municipalityUuid")
    void toDomain_shouldHandleNullMunicipalityUuid() {
        WarehouseEntity entity = WarehouseEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .code("BOD-002")
                .name("Sucursal")
                .type(WarehouseType.SUCURSAL)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Warehouse domain = mapper.toDomain(entity);

        assertThat(domain.getMunicipalityId()).isNull();
    }

    @Test
    @DisplayName("toEntity should map all fields including municipalityId to municipalityUuid")
    void toEntity_shouldMapAllFields() {
        UUID uuid = UUID.randomUUID();
        UUID municipalityId = UUID.randomUUID();

        Warehouse domain = Warehouse.builder()
                .id(1L)
                .uuid(uuid)
                .code("BOD-001")
                .name("Bodega Principal")
                .description("Desc")
                .type(WarehouseType.PRINCIPAL)
                .address("Calle 10")
                .municipalityId(municipalityId)
                .responsible("Juan")
                .email("juan@mail.com")
                .phone("3001234567")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        WarehouseEntity entity = mapper.toEntity(domain);

        assertThat(entity.getUuid()).isEqualTo(uuid.toString());
        assertThat(entity.getCode()).isEqualTo("BOD-001");
        assertThat(entity.getMunicipalityUuid()).isEqualTo(municipalityId.toString());
        assertThat(entity.getType()).isEqualTo(WarehouseType.PRINCIPAL);
    }

    @Test
    @DisplayName("toEntity should handle null municipalityId")
    void toEntity_shouldHandleNullMunicipalityId() {
        Warehouse domain = Warehouse.builder()
                .uuid(UUID.randomUUID())
                .code("BOD-002")
                .name("Sucursal")
                .type(WarehouseType.SUCURSAL)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        WarehouseEntity entity = mapper.toEntity(domain);

        assertThat(entity.getMunicipalityUuid()).isNull();
    }
}
