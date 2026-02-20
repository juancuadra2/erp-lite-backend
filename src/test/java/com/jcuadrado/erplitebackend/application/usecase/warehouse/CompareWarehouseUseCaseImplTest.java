package com.jcuadrado.erplitebackend.application.usecase.warehouse;

import com.jcuadrado.erplitebackend.domain.exception.warehouse.WarehouseNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.domain.port.warehouse.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareWarehouseUseCaseImplTest {

    @Mock
    private WarehouseRepository repository;

    @InjectMocks
    private CompareWarehouseUseCaseImpl useCase;

    private Warehouse sampleWarehouse(UUID uuid) {
        return Warehouse.builder()
                .uuid(uuid).code("BOD-001").name("Bodega").type(WarehouseType.SUCURSAL)
                .active(true).createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("findByUuid should return warehouse when found")
    void findByUuid_shouldReturnWarehouse_whenFound() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse(uuid);
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(warehouse));

        assertThat(useCase.findByUuid(uuid)).isEqualTo(warehouse);
    }

    @Test
    @DisplayName("findByUuid should throw WarehouseNotFoundException when not found")
    void findByUuid_shouldThrow_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findByUuid(uuid))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    @DisplayName("findAll should return paged results")
    void findAll_shouldReturnPagedResults() {
        Map<String, Object> filters = Map.of("active", true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Warehouse> page = new PageImpl<>(List.of(sampleWarehouse(UUID.randomUUID())));

        when(repository.findAll(filters, pageable)).thenReturn(page);

        Page<Warehouse> result = useCase.findAll(filters, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAllActive should return active warehouses")
    void findAllActive_shouldReturnActiveWarehouses() {
        List<Warehouse> active = List.of(
                sampleWarehouse(UUID.randomUUID()),
                sampleWarehouse(UUID.randomUUID()));
        when(repository.findAllActive()).thenReturn(active);

        assertThat(useCase.findAllActive()).hasSize(2);
    }
}
