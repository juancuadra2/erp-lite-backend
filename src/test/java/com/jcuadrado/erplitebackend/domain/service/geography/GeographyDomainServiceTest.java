package com.jcuadrado.erplitebackend.domain.service.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateDepartmentCodeException;
import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateMunicipalityCodeException;
import com.jcuadrado.erplitebackend.domain.exception.geography.GeographyConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.geography.InvalidGeographyException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeographyDomainServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private MunicipalityRepository municipalityRepository;

    @Mock
    private GeographyValidator validator;

    @InjectMocks
    private GeographyDomainService domainService;

    // ==================== Department: Unique Code ====================

    @Test
    void validateUniqueDepartmentCode_shouldPassWhenCodeDoesNotExist() {
        when(departmentRepository.existsByCode("05")).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateUniqueDepartmentCode("05"));
        verify(departmentRepository).existsByCode("05");
    }

    @Test
    void validateUniqueDepartmentCode_shouldThrowWhenCodeExists() {
        when(departmentRepository.existsByCode("05")).thenReturn(true);
        assertThatThrownBy(() -> domainService.validateUniqueDepartmentCode("05"))
                .isInstanceOf(DuplicateDepartmentCodeException.class)
                .hasMessageContaining("05");
        verify(departmentRepository).existsByCode("05");
    }

    @Test
    void validateUniqueDepartmentCodeExcluding_shouldPassWhenCodeDoesNotExistForOthers() {
        UUID uuid = UUID.randomUUID();
        when(departmentRepository.existsByCodeExcludingUuid("05", uuid)).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateUniqueDepartmentCodeExcluding("05", uuid));
        verify(departmentRepository).existsByCodeExcludingUuid("05", uuid);
    }

    @Test
    void validateUniqueDepartmentCodeExcluding_shouldThrowWhenCodeExistsForOthers() {
        UUID uuid = UUID.randomUUID();
        when(departmentRepository.existsByCodeExcludingUuid("05", uuid)).thenReturn(true);
        assertThatThrownBy(() -> domainService.validateUniqueDepartmentCodeExcluding("05", uuid))
                .isInstanceOf(DuplicateDepartmentCodeException.class)
                .hasMessageContaining("05");
        verify(departmentRepository).existsByCodeExcludingUuid("05", uuid);
    }

    // ==================== Department: Can Delete ====================

    @Test
    void canDeleteDepartment_shouldReturnTrueWhenNoMunicipalities() {
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();
        when(municipalityRepository.countByDepartmentId(1L)).thenReturn(0L);
        assertThat(domainService.canDeleteDepartment(dept)).isTrue();
    }

    @Test
    void canDeleteDepartment_shouldReturnFalseWhenMunicipalitiesExist() {
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();
        when(municipalityRepository.countByDepartmentId(1L)).thenReturn(5L);
        assertThat(domainService.canDeleteDepartment(dept)).isFalse();
    }

    @Test
    void ensureDepartmentCanBeDeleted_shouldPassWhenNoMunicipalities() {
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();
        when(municipalityRepository.countByDepartmentId(1L)).thenReturn(0L);
        assertDoesNotThrow(() -> domainService.ensureDepartmentCanBeDeleted(dept));
    }

    @Test
    void ensureDepartmentCanBeDeleted_shouldThrowWhenMunicipalitiesExist() {
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();
        when(municipalityRepository.countByDepartmentId(1L)).thenReturn(10L);
        assertThatThrownBy(() -> domainService.ensureDepartmentCanBeDeleted(dept))
                .isInstanceOf(GeographyConstraintException.class)
                .hasMessageContaining("Cannot delete department");
    }

    // ==================== Department: Prepare for Creation ====================

    @Test
    void prepareForDepartmentCreation_shouldValidateAndSetDefaults() {
        Department dept = Department.builder()
                .code("05").name("Antioquia").build();
        doNothing().when(validator).validateDepartment(anyString(), anyString());
        when(departmentRepository.existsByCode("05")).thenReturn(false);

        domainService.prepareForDepartmentCreation(dept);

        assertThat(dept.getUuid()).isNotNull();
        assertThat(dept.getEnabled()).isTrue();
        verify(validator).validateDepartment("05", "Antioquia");
        verify(departmentRepository).existsByCode("05");
    }

    @Test
    void prepareForDepartmentCreation_shouldKeepExistingUuid() {
        UUID existingUuid = UUID.randomUUID();
        Department dept = Department.builder()
                .uuid(existingUuid).code("05").name("Antioquia").build();
        doNothing().when(validator).validateDepartment(anyString(), anyString());
        when(departmentRepository.existsByCode("05")).thenReturn(false);

        domainService.prepareForDepartmentCreation(dept);

        assertThat(dept.getUuid()).isEqualTo(existingUuid);
    }

    @Test
    void prepareForDepartmentCreation_shouldKeepExistingEnabledValue() {
        Department dept = Department.builder()
                .code("05").name("Antioquia").enabled(false).build();
        doNothing().when(validator).validateDepartment(anyString(), anyString());
        when(departmentRepository.existsByCode("05")).thenReturn(false);

        domainService.prepareForDepartmentCreation(dept);

        assertThat(dept.getEnabled()).isFalse();
    }

    @Test
    void prepareForDepartmentCreation_shouldThrowWhenCodeIsDuplicated() {
        Department dept = Department.builder().code("05").name("Antioquia").build();
        doNothing().when(validator).validateDepartment(anyString(), anyString());
        when(departmentRepository.existsByCode("05")).thenReturn(true);

        assertThatThrownBy(() -> domainService.prepareForDepartmentCreation(dept))
                .isInstanceOf(DuplicateDepartmentCodeException.class);
    }

    @Test
    void prepareForDepartmentCreation_shouldThrowWhenValidationFails() {
        Department dept = Department.builder().code("ABC").name("Antioquia").build();
        doThrow(new InvalidGeographyException("code", "Department code must be exactly 2 digits"))
                .when(validator).validateDepartment(anyString(), anyString());

        assertThatThrownBy(() -> domainService.prepareForDepartmentCreation(dept))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Department code must be exactly 2 digits");
    }

    // ==================== Department: Prepare for Update ====================

    @Test
    void prepareForDepartmentUpdate_shouldValidateAndCheckUniqueCode() {
        UUID uuid = UUID.randomUUID();
        Department dept = Department.builder()
                .uuid(uuid).code("08").name("Atlántico").build();
        doNothing().when(validator).validateDepartment(anyString(), anyString());
        when(departmentRepository.existsByCodeExcludingUuid("08", uuid)).thenReturn(false);

        domainService.prepareForDepartmentUpdate(dept);

        verify(validator).validateDepartment("08", "Atlántico");
        verify(departmentRepository).existsByCodeExcludingUuid("08", uuid);
    }

    @Test
    void prepareForDepartmentUpdate_shouldThrowWhenCodeIsDuplicated() {
        UUID uuid = UUID.randomUUID();
        Department dept = Department.builder()
                .uuid(uuid).code("05").name("Antioquia").build();
        doNothing().when(validator).validateDepartment(anyString(), anyString());
        when(departmentRepository.existsByCodeExcludingUuid("05", uuid)).thenReturn(true);

        assertThatThrownBy(() -> domainService.prepareForDepartmentUpdate(dept))
                .isInstanceOf(DuplicateDepartmentCodeException.class);
    }

    @Test
    void prepareForDepartmentUpdate_shouldThrowWhenValidationFails() {
        UUID uuid = UUID.randomUUID();
        Department dept = Department.builder().uuid(uuid).code("X").name("").build();
        doThrow(new InvalidGeographyException("code", "Department code must be exactly 2 digits"))
                .when(validator).validateDepartment(anyString(), anyString());

        assertThatThrownBy(() -> domainService.prepareForDepartmentUpdate(dept))
                .isInstanceOf(InvalidGeographyException.class);
    }

    // ==================== Municipality: Unique Code ====================

    @Test
    void validateUniqueMunicipalityCode_shouldPassWhenCodeDoesNotExist() {
        when(municipalityRepository.existsByCodeAndDepartmentId("05001", 1L)).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateUniqueMunicipalityCode("05001", 1L));
    }

    @Test
    void validateUniqueMunicipalityCode_shouldThrowWhenCodeExists() {
        when(municipalityRepository.existsByCodeAndDepartmentId("05001", 1L)).thenReturn(true);
        assertThatThrownBy(() -> domainService.validateUniqueMunicipalityCode("05001", 1L))
                .isInstanceOf(DuplicateMunicipalityCodeException.class)
                .hasMessageContaining("05001");
    }

    @Test
    void validateUniqueMunicipalityCodeExcluding_shouldPassWhenCodeDoesNotExistForOthers() {
        UUID uuid = UUID.randomUUID();
        when(municipalityRepository.existsByCodeAndDepartmentIdExcludingUuid("05001", 1L, uuid)).thenReturn(false);
        assertDoesNotThrow(() -> domainService.validateUniqueMunicipalityCodeExcluding("05001", 1L, uuid));
    }

    @Test
    void validateUniqueMunicipalityCodeExcluding_shouldThrowWhenCodeExistsForOthers() {
        UUID uuid = UUID.randomUUID();
        when(municipalityRepository.existsByCodeAndDepartmentIdExcludingUuid("05001", 1L, uuid)).thenReturn(true);
        assertThatThrownBy(() -> domainService.validateUniqueMunicipalityCodeExcluding("05001", 1L, uuid))
                .isInstanceOf(DuplicateMunicipalityCodeException.class)
                .hasMessageContaining("05001");
    }

    // ==================== Municipality: Prepare for Creation ====================

    @Test
    void prepareForMunicipalityCreation_shouldValidateAndSetDefaults() {
        Department dept = Department.builder().id(1L).build();
        Municipality mun = Municipality.builder()
                .code("05001").name("Medellín").department(dept).build();
        doNothing().when(validator).validateMunicipality(anyString(), anyString());
        when(municipalityRepository.existsByCodeAndDepartmentId("05001", 1L)).thenReturn(false);

        domainService.prepareForMunicipalityCreation(mun);

        assertThat(mun.getUuid()).isNotNull();
        assertThat(mun.getEnabled()).isTrue();
        verify(validator).validateMunicipality("05001", "Medellín");
    }

    @Test
    void prepareForMunicipalityCreation_shouldKeepExistingUuid() {
        UUID existingUuid = UUID.randomUUID();
        Department dept = Department.builder().id(1L).build();
        Municipality mun = Municipality.builder()
                .uuid(existingUuid).code("05001").name("Medellín").department(dept).build();
        doNothing().when(validator).validateMunicipality(anyString(), anyString());
        when(municipalityRepository.existsByCodeAndDepartmentId("05001", 1L)).thenReturn(false);

        domainService.prepareForMunicipalityCreation(mun);

        assertThat(mun.getUuid()).isEqualTo(existingUuid);
    }

    @Test
    void prepareForMunicipalityCreation_shouldThrowWhenCodeIsDuplicated() {
        Department dept = Department.builder().id(1L).build();
        Municipality mun = Municipality.builder()
                .code("05001").name("Medellín").department(dept).build();
        doNothing().when(validator).validateMunicipality(anyString(), anyString());
        when(municipalityRepository.existsByCodeAndDepartmentId("05001", 1L)).thenReturn(true);

        assertThatThrownBy(() -> domainService.prepareForMunicipalityCreation(mun))
                .isInstanceOf(DuplicateMunicipalityCodeException.class);
    }

    // ==================== Municipality: Prepare for Update ====================

    @Test
    void prepareForMunicipalityUpdate_shouldValidateAndCheckUniqueCode() {
        UUID uuid = UUID.randomUUID();
        Department dept = Department.builder().id(1L).build();
        Municipality mun = Municipality.builder()
                .uuid(uuid).code("05001").name("Medellín").department(dept).build();
        doNothing().when(validator).validateMunicipality(anyString(), anyString());
        when(municipalityRepository.existsByCodeAndDepartmentIdExcludingUuid("05001", 1L, uuid)).thenReturn(false);

        domainService.prepareForMunicipalityUpdate(mun);

        verify(validator).validateMunicipality("05001", "Medellín");
        verify(municipalityRepository).existsByCodeAndDepartmentIdExcludingUuid("05001", 1L, uuid);
    }

    @Test
    void prepareForMunicipalityUpdate_shouldThrowWhenCodeIsDuplicated() {
        UUID uuid = UUID.randomUUID();
        Department dept = Department.builder().id(1L).build();
        Municipality mun = Municipality.builder()
                .uuid(uuid).code("05001").name("Medellín").department(dept).build();
        doNothing().when(validator).validateMunicipality(anyString(), anyString());
        when(municipalityRepository.existsByCodeAndDepartmentIdExcludingUuid("05001", 1L, uuid)).thenReturn(true);

        assertThatThrownBy(() -> domainService.prepareForMunicipalityUpdate(mun))
                .isInstanceOf(DuplicateMunicipalityCodeException.class);
    }
}
