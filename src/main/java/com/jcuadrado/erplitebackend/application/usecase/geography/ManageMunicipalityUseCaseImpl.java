package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.application.port.geography.ManageMunicipalityUseCase;
import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.geography.MunicipalityNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ManageMunicipalityUseCaseImpl implements ManageMunicipalityUseCase {

    private final MunicipalityRepository municipalityRepository;
    private final DepartmentRepository departmentRepository;
    private final GeographyDomainService domainService;

    @Override
    public Municipality create(Municipality municipality) {
        log.debug("Creating municipality with code: {}", municipality.getCode());

        // Verify department exists
        Department department = departmentRepository.findById(municipality.getDepartment().getId())
                .orElseThrow(() -> new DepartmentNotFoundException(municipality.getDepartment().getId()));
        municipality.setDepartment(department);

        domainService.prepareForMunicipalityCreation(municipality);
        municipality.setCreatedAt(LocalDateTime.now());

        Municipality saved = municipalityRepository.save(municipality);
        log.info("Municipality created successfully with ID: {} and UUID: {}", saved.getId(), saved.getUuid());
        return saved;
    }

    @Override
    public Municipality update(UUID uuid, Municipality municipality) {
        Municipality existing = municipalityRepository.findByUuid(uuid)
                .orElseThrow(() -> new MunicipalityNotFoundException(uuid));

        existing.setCode(municipality.getCode());
        existing.setName(municipality.getName());

        domainService.prepareForMunicipalityUpdate(existing);
        existing.setUpdatedAt(LocalDateTime.now());

        return municipalityRepository.save(existing);
    }

    @Override
    public void delete(UUID uuid) {
        municipalityRepository.findByUuid(uuid)
                .orElseThrow(() -> new MunicipalityNotFoundException(uuid));

        municipalityRepository.deleteByUuid(uuid);
    }

    @Override
    public void activate(UUID uuid) {
        Municipality existing = municipalityRepository.findByUuid(uuid)
                .orElseThrow(() -> new MunicipalityNotFoundException(uuid));

        existing.activate();
        existing.setUpdatedAt(LocalDateTime.now());
        municipalityRepository.save(existing);
    }

    @Override
    public void deactivate(UUID uuid) {
        Municipality existing = municipalityRepository.findByUuid(uuid)
                .orElseThrow(() -> new MunicipalityNotFoundException(uuid));

        existing.deactivate();
        existing.setUpdatedAt(LocalDateTime.now());
        municipalityRepository.save(existing);
    }
}
