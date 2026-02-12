package com.jcuadrado.erplitebackend.domain.service.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateDepartmentCodeException;
import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateMunicipalityCodeException;
import com.jcuadrado.erplitebackend.domain.exception.geography.GeographyConstraintException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Domain service for geography business rules.
 * No Spring annotations - beans configured in BeanConfiguration.
 */
@RequiredArgsConstructor
public class GeographyDomainService {

    private final DepartmentRepository departmentRepository;
    private final MunicipalityRepository municipalityRepository;
    private final GeographyValidator validator;

    // ==================== Department Operations ====================

    public void validateUniqueDepartmentCode(String code) {
        if (departmentRepository.existsByCode(code)) {
            throw new DuplicateDepartmentCodeException(code);
        }
    }

    public void validateUniqueDepartmentCodeExcluding(String code, UUID excludeUuid) {
        if (departmentRepository.existsByCodeExcludingUuid(code, excludeUuid)) {
            throw new DuplicateDepartmentCodeException(code);
        }
    }

    public boolean canDeleteDepartment(Department department) {
        long municipalityCount = municipalityRepository.countByDepartmentId(department.getId());
        return municipalityCount == 0;
    }

    public void ensureDepartmentCanBeDeleted(Department department) {
        if (!canDeleteDepartment(department)) {
            throw new GeographyConstraintException(
                    "Cannot delete department with associated municipalities");
        }
    }

    public void prepareForDepartmentCreation(Department department) {
        validator.validateDepartment(department.getCode(), department.getName());
        validateUniqueDepartmentCode(department.getCode());
        if (department.getUuid() == null) {
            department.setUuid(UUID.randomUUID());
        }
        if (department.getEnabled() == null) {
            department.setEnabled(true);
        }
    }

    public void prepareForDepartmentUpdate(Department department) {
        validator.validateDepartment(department.getCode(), department.getName());
        validateUniqueDepartmentCodeExcluding(department.getCode(), department.getUuid());
    }

    // ==================== Municipality Operations ====================

    public void validateUniqueMunicipalityCode(String code, Long departmentId) {
        if (municipalityRepository.existsByCodeAndDepartmentId(code, departmentId)) {
            throw new DuplicateMunicipalityCodeException(code);
        }
    }

    public void validateUniqueMunicipalityCodeExcluding(String code, Long departmentId, UUID excludeUuid) {
        if (municipalityRepository.existsByCodeAndDepartmentIdExcludingUuid(code, departmentId, excludeUuid)) {
            throw new DuplicateMunicipalityCodeException(code);
        }
    }

    public void prepareForMunicipalityCreation(com.jcuadrado.erplitebackend.domain.model.geography.Municipality municipality) {
        validator.validateMunicipality(municipality.getCode(), municipality.getName());
        validateUniqueMunicipalityCode(municipality.getCode(), municipality.getDepartment().getId());
        if (municipality.getUuid() == null) {
            municipality.setUuid(UUID.randomUUID());
        }
        if (municipality.getEnabled() == null) {
            municipality.setEnabled(true);
        }
    }

    public void prepareForMunicipalityUpdate(com.jcuadrado.erplitebackend.domain.model.geography.Municipality municipality) {
        validator.validateMunicipality(municipality.getCode(), municipality.getName());
        validateUniqueMunicipalityCodeExcluding(
                municipality.getCode(),
                municipality.getDepartment().getId(),
                municipality.getUuid());
    }
}
