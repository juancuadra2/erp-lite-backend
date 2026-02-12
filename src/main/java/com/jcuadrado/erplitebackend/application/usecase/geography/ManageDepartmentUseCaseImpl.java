package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.application.port.geography.ManageDepartmentUseCase;
import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
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
public class ManageDepartmentUseCaseImpl implements ManageDepartmentUseCase {

    private final DepartmentRepository repository;
    private final GeographyDomainService domainService;

    @Override
    public Department create(Department department) {
        log.debug("Creating department with code: {}", department.getCode());

        domainService.prepareForDepartmentCreation(department);
        department.setCreatedAt(LocalDateTime.now());

        Department saved = repository.save(department);
        log.info("Department created successfully with ID: {} and UUID: {}", saved.getId(), saved.getUuid());
        return saved;
    }

    @Override
    public Department update(UUID uuid, Department department) {
        Department existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new DepartmentNotFoundException(uuid));

        existing.setCode(department.getCode());
        existing.setName(department.getName());

        domainService.prepareForDepartmentUpdate(existing);
        existing.setUpdatedAt(LocalDateTime.now());

        return repository.save(existing);
    }

    @Override
    public void delete(UUID uuid) {
        Department existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new DepartmentNotFoundException(uuid));

        domainService.ensureDepartmentCanBeDeleted(existing);
        repository.deleteByUuid(uuid);
    }

    @Override
    public void activate(UUID uuid) {
        Department existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new DepartmentNotFoundException(uuid));

        existing.activate();
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }

    @Override
    public void deactivate(UUID uuid) {
        Department existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new DepartmentNotFoundException(uuid));

        existing.deactivate();
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);
    }
}
