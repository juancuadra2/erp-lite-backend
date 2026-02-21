package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.warehouse.WarehouseEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.jpa.warehouse.WarehouseJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.warehouse.WarehouseEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseRepositoryAdapterTest {

    @Mock
    private WarehouseJpaRepository jpaRepository;
    @Mock
    private WarehouseEntityMapper mapper;

    @InjectMocks
    private WarehouseRepositoryAdapter adapter;

    private WarehouseEntity sampleEntity(String uuidStr) {
        return WarehouseEntity.builder()
                .uuid(uuidStr).code("BOD-001").name("Bodega")
                .type(WarehouseType.SUCURSAL).active(true).createdAt(LocalDateTime.now()).build();
    }

    private Warehouse sampleDomain(UUID uuid) {
        return Warehouse.builder()
                .uuid(uuid).code("BOD-001").name("Bodega")
                .type(WarehouseType.SUCURSAL).active(true).createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("save should persist and return mapped domain")
    void save_shouldPersistAndReturnDomain() {
        UUID uuid = UUID.randomUUID();
        Warehouse domain = sampleDomain(uuid);
        WarehouseEntity entity = sampleEntity(uuid.toString());

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertThat(adapter.save(domain)).isEqualTo(domain);
    }

    @Test
    @DisplayName("findByUuid should return Optional with domain when found")
    void findByUuid_shouldReturnDomain_whenFound() {
        UUID uuid = UUID.randomUUID();
        WarehouseEntity entity = sampleEntity(uuid.toString());
        Warehouse domain = sampleDomain(uuid);

        when(jpaRepository.findByUuidAndDeletedAtIsNull(uuid.toString())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertThat(adapter.findByUuid(uuid)).contains(domain);
    }

    @Test
    @DisplayName("findByUuid should return empty Optional when not found")
    void findByUuid_shouldReturnEmpty_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(jpaRepository.findByUuidAndDeletedAtIsNull(uuid.toString())).thenReturn(Optional.empty());

        assertThat(adapter.findByUuid(uuid)).isEmpty();
    }

    @Test
    @DisplayName("findAll should return paged domain objects")
    void findAll_shouldReturnPagedDomain() {
        UUID uuid = UUID.randomUUID();
        WarehouseEntity entity = sampleEntity(uuid.toString());
        Warehouse domain = sampleDomain(uuid);
        Page<WarehouseEntity> entityPage = new PageImpl<>(List.of(entity));

        when(jpaRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<Warehouse> result = adapter.findAll(Map.of(), PageRequest.of(0, 10));

        assertThat(result.getContent()).containsExactly(domain);
    }

    @Test
    @DisplayName("findAllActive should return active non-deleted warehouses")
    void findAllActive_shouldReturnActiveDomain() {
        UUID uuid = UUID.randomUUID();
        WarehouseEntity entity = sampleEntity(uuid.toString());
        Warehouse domain = sampleDomain(uuid);

        when(jpaRepository.findByActiveTrueAndDeletedAtIsNull()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        assertThat(adapter.findAllActive()).containsExactly(domain);
    }

    @Test
    @DisplayName("existsByCodeIgnoreCase should delegate to jpa")
    void existsByCodeIgnoreCase_shouldDelegate() {
        when(jpaRepository.existsByCodeIgnoreCase("BOD-001")).thenReturn(true);
        assertThat(adapter.existsByCodeIgnoreCase("BOD-001")).isTrue();
    }

    @Test
    @DisplayName("existsByCodeIgnoreCaseAndUuidNot should convert UUID to String")
    void existsByCodeIgnoreCaseAndUuidNot_shouldConvertUuid() {
        UUID uuid = UUID.randomUUID();
        when(jpaRepository.existsByCodeIgnoreCaseAndUuidNot("BOD-001", uuid.toString())).thenReturn(false);
        assertThat(adapter.existsByCodeIgnoreCaseAndUuidNot("BOD-001", uuid)).isFalse();
    }

    @Test
    @DisplayName("existsActivePrincipalWarehouse should delegate with PRINCIPAL type")
    void existsActivePrincipalWarehouse_shouldDelegate() {
        when(jpaRepository.existsByTypeAndActiveTrueAndDeletedAtIsNull(WarehouseType.PRINCIPAL)).thenReturn(true);
        assertThat(adapter.existsActivePrincipalWarehouse()).isTrue();
    }

    @Test
    @DisplayName("existsActivePrincipalWarehouseAndUuidNot should convert UUID to String")
    void existsActivePrincipalWarehouseAndUuidNot_shouldConvertUuid() {
        UUID uuid = UUID.randomUUID();
        when(jpaRepository.existsByTypeAndActiveTrueAndDeletedAtIsNullAndUuidNot(
                WarehouseType.PRINCIPAL, uuid.toString())).thenReturn(false);
        assertThat(adapter.existsActivePrincipalWarehouseAndUuidNot(uuid)).isFalse();
    }

    @Test
    @DisplayName("existsByNameIgnoreCase should delegate to jpa")
    void existsByNameIgnoreCase_shouldDelegate() {
        when(jpaRepository.existsByNameIgnoreCase("Bodega Central")).thenReturn(true);
        assertThat(adapter.existsByNameIgnoreCase("Bodega Central")).isTrue();
    }

    @Test
    @DisplayName("existsByNameIgnoreCaseAndUuidNot should convert UUID to String")
    void existsByNameIgnoreCaseAndUuidNot_shouldConvertUuid() {
        UUID uuid = UUID.randomUUID();
        when(jpaRepository.existsByNameIgnoreCaseAndUuidNot("Bodega Central", uuid.toString())).thenReturn(false);
        assertThat(adapter.existsByNameIgnoreCaseAndUuidNot("Bodega Central", uuid)).isFalse();
    }
}
