package com.jcuadrado.erplitebackend.domain.service.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.DuplicateWarehouseCodeException;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.DuplicateWarehouseNameException;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.SinglePrincipalWarehouseException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.domain.port.warehouse.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseDomainServiceTest {

    @Mock
    private WarehouseRepository repository;

    private WarehouseDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new WarehouseDomainService(new WarehouseValidator(), repository);
    }

    // ── prepareForCreate ──────────────────────────────────────────

    @Test
    @DisplayName("prepareForCreate should build warehouse with normalized code and active=true")
    void prepareForCreate_shouldBuildWarehouse_withNormalizedCode() {
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsByNameIgnoreCase(anyString())).thenReturn(false);

        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "bod-001", "Bodega Sur", null, WarehouseType.SUCURSAL,
                null, null, null, null, null);

        Warehouse result = domainService.prepareForCreate(command);

        assertThat(result.getCode()).isEqualTo("BOD-001");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUuid()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("prepareForCreate should throw DuplicateWarehouseCodeException when code exists")
    void prepareForCreate_shouldThrow_whenCodeExists() {
        when(repository.existsByCodeIgnoreCase("BOD-001")).thenReturn(true);

        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "BOD-001", "Bodega Sur", null, WarehouseType.SUCURSAL,
                null, null, null, null, null);

        assertThatThrownBy(() -> domainService.prepareForCreate(command))
                .isInstanceOf(DuplicateWarehouseCodeException.class);
    }

    @Test
    @DisplayName("prepareForCreate should throw DuplicateWarehouseNameException when name exists")
    void prepareForCreate_shouldThrow_whenNameExists() {
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsByNameIgnoreCase("Bodega Sur")).thenReturn(true);

        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "BOD-002", "Bodega Sur", null, WarehouseType.SUCURSAL,
                null, null, null, null, null);

        assertThatThrownBy(() -> domainService.prepareForCreate(command))
                .isInstanceOf(DuplicateWarehouseNameException.class);
    }

    @Test
    @DisplayName("prepareForCreate should throw SinglePrincipalWarehouseException when PRINCIPAL already exists")
    void prepareForCreate_shouldThrow_whenPrincipalAlreadyExists() {
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsActivePrincipalWarehouse()).thenReturn(true);

        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "BOD-NEW", "Nueva Principal", null, WarehouseType.PRINCIPAL,
                null, null, null, null, null);

        assertThatThrownBy(() -> domainService.prepareForCreate(command))
                .isInstanceOf(SinglePrincipalWarehouseException.class);
    }

    @Test
    @DisplayName("prepareForCreate should not check PRINCIPAL constraint for non-PRINCIPAL types")
    void prepareForCreate_shouldNotCheckPrincipal_forNonPrincipalType() {
        when(repository.existsByCodeIgnoreCase(anyString())).thenReturn(false);
        when(repository.existsByNameIgnoreCase(anyString())).thenReturn(false);

        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "BOD-003", "Temporal", null, WarehouseType.TEMPORAL,
                null, null, null, null, null);

        Warehouse result = domainService.prepareForCreate(command);

        assertThat(result.getType()).isEqualTo(WarehouseType.TEMPORAL);
    }

    // ── applyUpdate ───────────────────────────────────────────────

    @Test
    @DisplayName("applyUpdate should update fields and set updatedAt")
    void applyUpdate_shouldUpdateFields_andSetUpdatedAt() {
        UUID uuid = UUID.randomUUID();
        Warehouse existing = Warehouse.builder()
                .uuid(uuid).code("BOD-001").name("Original").type(WarehouseType.SUCURSAL)
                .active(true).createdAt(LocalDateTime.now()).build();

        when(repository.existsByNameIgnoreCaseAndUuidNot("Nuevo Nombre", uuid)).thenReturn(false);

        UpdateWarehouseCommand command = new UpdateWarehouseCommand(
                "Nuevo Nombre", null, WarehouseType.CONSIGNACION,
                null, null, null, null, null);

        domainService.applyUpdate(existing, command);

        assertThat(existing.getName()).isEqualTo("Nuevo Nombre");
        assertThat(existing.getType()).isEqualTo(WarehouseType.CONSIGNACION);
        assertThat(existing.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("applyUpdate should throw DuplicateWarehouseNameException when name taken by another")
    void applyUpdate_shouldThrow_whenNameTakenByAnother() {
        UUID uuid = UUID.randomUUID();
        Warehouse existing = Warehouse.builder()
                .uuid(uuid).code("BOD-001").name("Original").type(WarehouseType.SUCURSAL)
                .active(true).createdAt(LocalDateTime.now()).build();

        when(repository.existsByNameIgnoreCaseAndUuidNot("Nombre Ocupado", uuid)).thenReturn(true);

        UpdateWarehouseCommand command = new UpdateWarehouseCommand(
                "Nombre Ocupado", null, WarehouseType.SUCURSAL,
                null, null, null, null, null);

        assertThatThrownBy(() -> domainService.applyUpdate(existing, command))
                .isInstanceOf(DuplicateWarehouseNameException.class);
    }

    @Test
    @DisplayName("applyUpdate should throw SinglePrincipalWarehouseException when changing to PRINCIPAL and another exists")
    void applyUpdate_shouldThrow_whenChangingToPrincipalAndAnotherExists() {
        UUID uuid = UUID.randomUUID();
        Warehouse existing = Warehouse.builder()
                .uuid(uuid).code("BOD-002").name("Sucursal").type(WarehouseType.SUCURSAL)
                .active(true).createdAt(LocalDateTime.now()).build();

        when(repository.existsByNameIgnoreCaseAndUuidNot(any(), any())).thenReturn(false);
        when(repository.existsActivePrincipalWarehouseAndUuidNot(uuid)).thenReturn(true);

        UpdateWarehouseCommand command = new UpdateWarehouseCommand(
                "Sucursal", null, WarehouseType.PRINCIPAL,
                null, null, null, null, null);

        assertThatThrownBy(() -> domainService.applyUpdate(existing, command))
                .isInstanceOf(SinglePrincipalWarehouseException.class);
    }

    // ── validateForActivation ─────────────────────────────────────

    @Test
    @DisplayName("validateForActivation should throw when activating PRINCIPAL and another PRINCIPAL exists")
    void validateForActivation_shouldThrow_whenPrincipalAlreadyActive() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = Warehouse.builder()
                .uuid(uuid).code("BOD-002").type(WarehouseType.PRINCIPAL).active(false)
                .createdAt(LocalDateTime.now()).build();

        when(repository.existsActivePrincipalWarehouseAndUuidNot(uuid)).thenReturn(true);

        assertThatThrownBy(() -> domainService.validateForActivation(warehouse))
                .isInstanceOf(SinglePrincipalWarehouseException.class);
    }

    @Test
    @DisplayName("validateForActivation should pass for non-PRINCIPAL warehouse")
    void validateForActivation_shouldPass_forNonPrincipal() {
        Warehouse warehouse = Warehouse.builder()
                .uuid(UUID.randomUUID()).code("BOD-003").type(WarehouseType.TEMPORAL)
                .active(false).createdAt(LocalDateTime.now()).build();

        org.assertj.core.api.Assertions.assertThatNoException()
                .isThrownBy(() -> domainService.validateForActivation(warehouse));
    }
}
