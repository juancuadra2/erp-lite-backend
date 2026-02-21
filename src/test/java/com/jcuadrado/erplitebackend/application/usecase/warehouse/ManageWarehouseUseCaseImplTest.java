package com.jcuadrado.erplitebackend.application.usecase.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.WarehouseInUseException;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.WarehouseNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.domain.port.warehouse.WarehouseRepository;
import com.jcuadrado.erplitebackend.domain.service.warehouse.WarehouseDomainService;
import com.jcuadrado.erplitebackend.domain.service.warehouse.WarehouseValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageWarehouseUseCaseImplTest {

    @Mock
    private WarehouseRepository repository;
    @Mock
    private WarehouseDomainService domainService;
    @Mock
    private WarehouseValidationService validationService;

    private ManageWarehouseUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageWarehouseUseCaseImpl(repository, domainService, validationService);
    }

    private Warehouse sampleWarehouse(UUID uuid) {
        return Warehouse.builder()
                .uuid(uuid).code("BOD-001").name("Bodega").type(WarehouseType.SUCURSAL)
                .active(true).createdAt(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("create should delegate to domainService and save")
    void create_shouldDelegateAndSave() {
        CreateWarehouseCommand command = new CreateWarehouseCommand(
                "BOD-001", "Bodega", null, WarehouseType.SUCURSAL,
                null, null, null, null, null);
        Warehouse built = sampleWarehouse(UUID.randomUUID());

        when(domainService.prepareForCreate(command)).thenReturn(built);
        when(repository.save(built)).thenReturn(built);

        Warehouse result = useCase.create(command);

        assertThat(result).isEqualTo(built);
        verify(domainService).prepareForCreate(command);
        verify(repository).save(built);
    }

    @Test
    @DisplayName("update should load warehouse, apply update and save")
    void update_shouldLoadApplyAndSave() {
        UUID uuid = UUID.randomUUID();
        Warehouse existing = sampleWarehouse(uuid);
        UpdateWarehouseCommand command = new UpdateWarehouseCommand(
                "Nuevo", null, WarehouseType.SUCURSAL, null, null, null, null, null);

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).applyUpdate(existing, command);
        when(repository.save(existing)).thenReturn(existing);

        Warehouse result = useCase.update(uuid, command);

        assertThat(result).isEqualTo(existing);
        verify(domainService).applyUpdate(existing, command);
    }

    @Test
    @DisplayName("update should throw WarehouseNotFoundException when not found")
    void update_shouldThrow_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(uuid, new UpdateWarehouseCommand(
                "X", null, WarehouseType.SUCURSAL, null, null, null, null, null)))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    @DisplayName("delete should soft-delete when warehouse is deletable")
    void delete_shouldSoftDelete_whenDeletable() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse(uuid);

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(warehouse));
        doNothing().when(validationService).validateDeletable(warehouse);
        when(repository.save(any())).thenReturn(warehouse);

        useCase.delete(uuid);

        assertThat(warehouse.isDeleted()).isTrue();
        verify(repository).save(warehouse);
    }

    @Test
    @DisplayName("delete should throw WarehouseInUseException when validation fails")
    void delete_shouldThrow_whenValidationFails() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse(uuid);
        warehouse = Warehouse.builder().uuid(uuid).code("BOD-001").name("Principal")
                .type(WarehouseType.PRINCIPAL).active(true).createdAt(LocalDateTime.now()).build();

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(warehouse));
        doThrow(new WarehouseInUseException("No se puede eliminar")).when(validationService).validateDeletable(warehouse);

        Warehouse finalWarehouse = warehouse;
        assertThatThrownBy(() -> useCase.delete(uuid))
                .isInstanceOf(WarehouseInUseException.class);
    }

    @Test
    @DisplayName("activate should validate and set active=true")
    void activate_shouldValidateAndActivate() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse(uuid);
        warehouse.deactivate();

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(warehouse));
        doNothing().when(domainService).validateForActivation(warehouse);
        when(repository.save(warehouse)).thenReturn(warehouse);

        Warehouse result = useCase.activate(uuid);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("deactivate should set active=false")
    void deactivate_shouldSetActiveFalse() {
        UUID uuid = UUID.randomUUID();
        Warehouse warehouse = sampleWarehouse(uuid);

        when(repository.findByUuid(uuid)).thenReturn(Optional.of(warehouse));
        doNothing().when(validationService).validateDeactivatable(uuid);
        when(repository.save(warehouse)).thenReturn(warehouse);

        Warehouse result = useCase.deactivate(uuid);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("delete should throw WarehouseNotFoundException when not found")
    void delete_shouldThrow_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(uuid))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    @DisplayName("activate should throw WarehouseNotFoundException when not found")
    void activate_shouldThrow_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.activate(uuid))
                .isInstanceOf(WarehouseNotFoundException.class);
    }

    @Test
    @DisplayName("deactivate should throw WarehouseNotFoundException when not found")
    void deactivate_shouldThrow_whenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deactivate(uuid))
                .isInstanceOf(WarehouseNotFoundException.class);
    }
}
