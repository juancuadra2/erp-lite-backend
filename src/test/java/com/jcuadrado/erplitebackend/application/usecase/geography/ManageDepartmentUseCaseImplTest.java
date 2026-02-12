package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
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
class ManageDepartmentUseCaseImplTest {

    @Mock
    private DepartmentRepository repository;

    @Mock
    private GeographyDomainService domainService;

    @InjectMocks
    private ManageDepartmentUseCaseImpl useCase;

    @Captor
    private ArgumentCaptor<Department> departmentCaptor;

    private UUID sampleUuid;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
    }

    // ==================== create ====================

    @Test
    void create_shouldPrepareAndSaveDepartment() {
        Department input = Department.builder().code("05").name("Antioquia").build();
        Department saved = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true).build();

        doNothing().when(domainService).prepareForDepartmentCreation(any(Department.class));
        when(repository.save(any(Department.class))).thenReturn(saved);

        Department result = useCase.create(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("05");
        verify(domainService).prepareForDepartmentCreation(any(Department.class));
        verify(repository).save(departmentCaptor.capture());
        assertThat(departmentCaptor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void create_shouldSetCreatedAtTimestamp() {
        Department input = Department.builder().code("05").name("Antioquia").build();
        doNothing().when(domainService).prepareForDepartmentCreation(any(Department.class));
        when(repository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        Department result = useCase.create(input);

        assertThat(result.getCreatedAt()).isNotNull();
    }

    // ==================== update ====================

    @Test
    void update_shouldFetchExistingAndUpdateFields() {
        Department existing = Department.builder()
                .id(1L).uuid(sampleUuid).code("05").name("Antioquia").enabled(true).build();
        Department updateData = Department.builder().code("08").name("Atlántico").build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).prepareForDepartmentUpdate(any(Department.class));
        when(repository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        Department result = useCase.update(sampleUuid, updateData);

        assertThat(result.getCode()).isEqualTo("08");
        assertThat(result.getName()).isEqualTo("Atlántico");
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(domainService).prepareForDepartmentUpdate(any(Department.class));
    }

    @Test
    void update_shouldThrowWhenDepartmentNotFound() {
        UUID uuid = UUID.randomUUID();
        Department updateData = Department.builder().code("08").name("Atlántico").build();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(uuid, updateData))
                .isInstanceOf(DepartmentNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ==================== delete ====================

    @Test
    void delete_shouldVerifyConstraintsAndDelete() {
        Department existing = Department.builder()
                .id(1L).uuid(sampleUuid).code("05").name("Antioquia").build();
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).ensureDepartmentCanBeDeleted(existing);

        useCase.delete(sampleUuid);

        verify(domainService).ensureDepartmentCanBeDeleted(existing);
        verify(repository).deleteByUuid(sampleUuid);
    }

    @Test
    void delete_shouldThrowWhenDepartmentNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(uuid))
                .isInstanceOf(DepartmentNotFoundException.class);
        verify(repository, never()).deleteByUuid(any());
    }

    // ==================== activate ====================

    @Test
    void activate_shouldSetEnabledTrueAndSave() {
        Department existing = Department.builder()
                .id(1L).uuid(sampleUuid).code("05").name("Antioquia").enabled(false).build();
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(repository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.activate(sampleUuid);

        verify(repository).save(departmentCaptor.capture());
        assertThat(departmentCaptor.getValue().getEnabled()).isTrue();
        assertThat(departmentCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void activate_shouldThrowWhenDepartmentNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.activate(uuid))
                .isInstanceOf(DepartmentNotFoundException.class);
        verify(repository, never()).save(any());
    }

    // ==================== deactivate ====================

    @Test
    void deactivate_shouldSetEnabledFalseAndSave() {
        Department existing = Department.builder()
                .id(1L).uuid(sampleUuid).code("05").name("Antioquia").enabled(true).build();
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(repository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.deactivate(sampleUuid);

        verify(repository).save(departmentCaptor.capture());
        assertThat(departmentCaptor.getValue().getEnabled()).isFalse();
        assertThat(departmentCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void deactivate_shouldThrowWhenDepartmentNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deactivate(uuid))
                .isInstanceOf(DepartmentNotFoundException.class);
        verify(repository, never()).save(any());
    }
}
