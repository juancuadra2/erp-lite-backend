package com.jcuadrado.erplitebackend.domain.service.warehouse;

import com.jcuadrado.erplitebackend.domain.exception.warehouse.WarehouseInUseException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarehouseValidationServiceTest {

    private WarehouseValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new WarehouseValidationService();
    }

    @Test
    @DisplayName("validateDeletable should throw when warehouse is PRINCIPAL and active (BR-03.3)")
    void validateDeletable_shouldThrow_whenPrincipalAndActive() {
        Warehouse warehouse = Warehouse.builder()
                .uuid(UUID.randomUUID())
                .code("BOD-001")
                .type(WarehouseType.PRINCIPAL)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> validationService.validateDeletable(warehouse))
                .isInstanceOf(WarehouseInUseException.class)
                .hasMessageContaining("PRINCIPAL");
    }

    @Test
    @DisplayName("validateDeletable should pass when warehouse is PRINCIPAL but inactive")
    void validateDeletable_shouldPass_whenPrincipalButInactive() {
        Warehouse warehouse = Warehouse.builder()
                .uuid(UUID.randomUUID())
                .code("BOD-001")
                .type(WarehouseType.PRINCIPAL)
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();

        assertThatNoException().isThrownBy(() -> validationService.validateDeletable(warehouse));
    }

    @Test
    @DisplayName("validateDeletable should pass when warehouse is not PRINCIPAL (BR-03.1 stub)")
    void validateDeletable_shouldPass_whenNotPrincipal() {
        Warehouse warehouse = Warehouse.builder()
                .uuid(UUID.randomUUID())
                .code("BOD-002")
                .type(WarehouseType.SUCURSAL)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        assertThatNoException().isThrownBy(() -> validationService.validateDeletable(warehouse));
    }

    @Test
    @DisplayName("validateDeactivatable should not throw (stub)")
    void validateDeactivatable_shouldNotThrow() {
        assertThatNoException().isThrownBy(
                () -> validationService.validateDeactivatable(UUID.randomUUID()));
    }
}
