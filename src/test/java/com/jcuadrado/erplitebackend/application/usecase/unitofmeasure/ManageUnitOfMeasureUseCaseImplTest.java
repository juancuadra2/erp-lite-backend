package com.jcuadrado.erplitebackend.application.usecase.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;
import com.jcuadrado.erplitebackend.domain.service.unitofmeasure.UnitOfMeasureDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageUnitOfMeasureUseCaseImplTest {

    @Mock
    private UnitOfMeasureRepository repository;

    @Mock
    private UnitOfMeasureDomainService domainService;

    @InjectMocks
    private ManageUnitOfMeasureUseCaseImpl useCase;

    @Test
    void create_shouldSetDefaultsAndPersist() {
        UnitOfMeasure request = UnitOfMeasure.builder().name("Caja").abbreviation("CJ").build();
        UnitOfMeasure saved = UnitOfMeasure.builder().id(1L).name("Caja").abbreviation("CJ").enabled(true).build();
        when(repository.save(any(UnitOfMeasure.class))).thenReturn(saved);

        UnitOfMeasure result = useCase.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        ArgumentCaptor<UnitOfMeasure> captor = ArgumentCaptor.forClass(UnitOfMeasure.class);
        verify(domainService).prepareForCreation(request);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUuid()).isNotNull();
        assertThat(captor.getValue().getEnabled()).isTrue();
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(0L);
    }

    @Test
    void create_shouldKeepProvidedUuidAndEnabled() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure request = UnitOfMeasure.builder().uuid(uuid).enabled(false).name("Caja").abbreviation("CJ").build();
        when(repository.save(any(UnitOfMeasure.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UnitOfMeasure result = useCase.create(request);

        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void update_shouldModifyAndPersist() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure existing = UnitOfMeasure.builder().uuid(uuid).name("Caja").abbreviation("CJ").enabled(true).build();
        UnitOfMeasure updates = UnitOfMeasure.builder().name("Caja Grande").abbreviation("CG").build();
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UnitOfMeasure result = useCase.update(uuid, updates);

        assertThat(result.getName()).isEqualTo("Caja Grande");
        assertThat(result.getAbbreviation()).isEqualTo("CG");
        assertThat(result.getUpdatedBy()).isEqualTo(0L);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(domainService).prepareForUpdate(existing, uuid);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(uuid, UnitOfMeasure.builder().build()))
                .isInstanceOf(UnitOfMeasureNotFoundException.class);
    }

    @Test
    void update_shouldKeepExistingValuesWhenUpdatesAreNull() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure existing = UnitOfMeasure.builder().uuid(uuid).name("Caja").abbreviation("CJ").enabled(true).build();
        UnitOfMeasure updates = UnitOfMeasure.builder().name(null).abbreviation(null).build();
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UnitOfMeasure result = useCase.update(uuid, updates);

        assertThat(result.getName()).isEqualTo("Caja");
        assertThat(result.getAbbreviation()).isEqualTo("CJ");
        verify(domainService).prepareForUpdate(existing, uuid);
    }

    @Test
    void activate_shouldActivateAndSave() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure existing = UnitOfMeasure.builder().uuid(uuid).enabled(false).build();
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UnitOfMeasure result = useCase.activate(uuid);

        assertThat(result.getEnabled()).isTrue();
        verify(repository).save(existing);
    }

    @Test
    void activate_shouldThrowWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.activate(uuid))
                .isInstanceOf(UnitOfMeasureNotFoundException.class);
    }

    @Test
    void deactivate_shouldValidateUsageAndSave() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure existing = UnitOfMeasure.builder().uuid(uuid).enabled(true).build();
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(existing));
        when(repository.countProductsWithUnitOfMeasure(uuid)).thenReturn(0L);
        when(repository.save(existing)).thenReturn(existing);

        UnitOfMeasure result = useCase.deactivate(uuid);

        assertThat(result.getEnabled()).isFalse();
        verify(domainService).ensureCanBeDeactivated(0L);
        verify(repository).save(existing);
    }

    @Test
    void deactivate_shouldThrowWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deactivate(uuid))
                .isInstanceOf(UnitOfMeasureNotFoundException.class);
    }

    @Test
    void delete_shouldDelegateToDeactivate() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure existing = UnitOfMeasure.builder().uuid(uuid).enabled(true).build();
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(existing));
        when(repository.countProductsWithUnitOfMeasure(uuid)).thenReturn(0L);
        when(repository.save(existing)).thenReturn(existing);

        useCase.delete(uuid);

        verify(domainService).ensureCanBeDeactivated(0L);
        verify(repository).save(existing);
    }
}
