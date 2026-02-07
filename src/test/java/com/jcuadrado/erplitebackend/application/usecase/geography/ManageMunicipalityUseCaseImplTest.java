package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.geography.MunicipalityNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageMunicipalityUseCaseImplTest {

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private GeographyDomainService domainService;

    @InjectMocks
    private ManageMunicipalityUseCaseImpl useCase;

    @Captor
    private ArgumentCaptor<Municipality> municipalityCaptor;

    private UUID sampleUuid;
    private Department sampleDepartment;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
        sampleDepartment = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true).build();
    }

    // ==================== create ====================

    @Test
    void create_shouldVerifyDepartmentAndSave() {
        Municipality input = Municipality.builder()
                .code("05001").name("Medellín")
                .department(Department.builder().id(1L).build())
                .build();
        Municipality saved = Municipality.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(sampleDepartment).enabled(true).build();

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        doNothing().when(domainService).prepareForMunicipalityCreation(any(Municipality.class));
        when(municipalityRepository.save(any(Municipality.class))).thenReturn(saved);

        Municipality result = useCase.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("05001");
        verify(departmentRepository).findById(1L);
        verify(domainService).prepareForMunicipalityCreation(any(Municipality.class));
        verify(municipalityRepository).save(municipalityCaptor.capture());
        assertThat(municipalityCaptor.getValue().getCreatedAt()).isNotNull();
        assertThat(municipalityCaptor.getValue().getDepartment()).isEqualTo(sampleDepartment);
    }

    @Test
    void create_shouldThrowWhenDepartmentNotFound() {
        Municipality input = Municipality.builder()
                .code("05001").name("Medellín")
                .department(Department.builder().id(999L).build())
                .build();

        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(DepartmentNotFoundException.class);
        verify(municipalityRepository, never()).save(any());
    }

    // ==================== update ====================

    @Test
    void update_shouldFetchExistingAndUpdateFields() {
        Municipality existing = Municipality.builder()
                .id(1L).uuid(sampleUuid).code("05001").name("Medellín")
                .department(sampleDepartment).enabled(true).build();
        Municipality updateData = Municipality.builder().code("05002").name("Abejorral").build();

        when(municipalityRepository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).prepareForMunicipalityUpdate(any(Municipality.class));
        when(municipalityRepository.save(any(Municipality.class))).thenAnswer(inv -> inv.getArgument(0));

        Municipality result = useCase.update(sampleUuid, updateData);

        assertThat(result.getCode()).isEqualTo("05002");
        assertThat(result.getName()).isEqualTo("Abejorral");
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(domainService).prepareForMunicipalityUpdate(any(Municipality.class));
    }

    @Test
    void update_shouldThrowWhenMunicipalityNotFound() {
        UUID uuid = UUID.randomUUID();
        Municipality updateData = Municipality.builder().code("05002").name("Abejorral").build();
        when(municipalityRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(uuid, updateData))
                .isInstanceOf(MunicipalityNotFoundException.class);
        verify(municipalityRepository, never()).save(any());
    }

    // ==================== delete ====================

    @Test
    void delete_shouldFindAndDeleteMunicipality() {
        Municipality existing = Municipality.builder()
                .id(1L).uuid(sampleUuid).code("05001").name("Medellín").build();
        when(municipalityRepository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));

        useCase.delete(sampleUuid);

        verify(municipalityRepository).deleteByUuid(sampleUuid);
    }

    @Test
    void delete_shouldThrowWhenMunicipalityNotFound() {
        UUID uuid = UUID.randomUUID();
        when(municipalityRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(uuid))
                .isInstanceOf(MunicipalityNotFoundException.class);
        verify(municipalityRepository, never()).deleteByUuid(any());
    }

    // ==================== activate ====================

    @Test
    void activate_shouldSetEnabledTrueAndSave() {
        Municipality existing = Municipality.builder()
                .id(1L).uuid(sampleUuid).code("05001").name("Medellín").enabled(false).build();
        when(municipalityRepository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(municipalityRepository.save(any(Municipality.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.activate(sampleUuid);

        verify(municipalityRepository).save(municipalityCaptor.capture());
        assertThat(municipalityCaptor.getValue().getEnabled()).isTrue();
        assertThat(municipalityCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void activate_shouldThrowWhenMunicipalityNotFound() {
        UUID uuid = UUID.randomUUID();
        when(municipalityRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.activate(uuid))
                .isInstanceOf(MunicipalityNotFoundException.class);
        verify(municipalityRepository, never()).save(any());
    }

    // ==================== deactivate ====================

    @Test
    void deactivate_shouldSetEnabledFalseAndSave() {
        Municipality existing = Municipality.builder()
                .id(1L).uuid(sampleUuid).code("05001").name("Medellín").enabled(true).build();
        when(municipalityRepository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(municipalityRepository.save(any(Municipality.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.deactivate(sampleUuid);

        verify(municipalityRepository).save(municipalityCaptor.capture());
        assertThat(municipalityCaptor.getValue().getEnabled()).isFalse();
        assertThat(municipalityCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void deactivate_shouldThrowWhenMunicipalityNotFound() {
        UUID uuid = UUID.randomUUID();
        when(municipalityRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deactivate(uuid))
                .isInstanceOf(MunicipalityNotFoundException.class);
        verify(municipalityRepository, never()).save(any());
    }
}
