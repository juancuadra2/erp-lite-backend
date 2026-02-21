package com.jcuadrado.erplitebackend.domain.model.warehouse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseTest {

    private Warehouse buildWarehouse(boolean active) {
        return Warehouse.builder()
                .uuid(UUID.randomUUID())
                .code("BOD-001")
                .name("Bodega Principal")
                .type(WarehouseType.PRINCIPAL)
                .active(active)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("activate should set active=true and update updatedAt")
    void activate_shouldSetActiveTrue_andUpdateTimestamp() {
        Warehouse warehouse = buildWarehouse(false);

        warehouse.activate();

        assertThat(warehouse.isActive()).isTrue();
        assertThat(warehouse.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("deactivate should set active=false and update updatedAt")
    void deactivate_shouldSetActiveFalse_andUpdateTimestamp() {
        Warehouse warehouse = buildWarehouse(true);

        warehouse.deactivate();

        assertThat(warehouse.isActive()).isFalse();
        assertThat(warehouse.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("softDelete should set deletedAt and active=false")
    void softDelete_shouldSetDeletedAt_andDeactivate() {
        Warehouse warehouse = buildWarehouse(true);

        warehouse.softDelete();

        assertThat(warehouse.getDeletedAt()).isNotNull();
        assertThat(warehouse.isActive()).isFalse();
    }

    @Test
    @DisplayName("isDeleted should return true when deletedAt is set")
    void isDeleted_shouldReturnTrue_whenDeletedAtIsSet() {
        Warehouse warehouse = buildWarehouse(true);
        warehouse.softDelete();

        assertThat(warehouse.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("isDeleted should return false when deletedAt is null")
    void isDeleted_shouldReturnFalse_whenDeletedAtIsNull() {
        Warehouse warehouse = buildWarehouse(true);

        assertThat(warehouse.isDeleted()).isFalse();
    }
}
