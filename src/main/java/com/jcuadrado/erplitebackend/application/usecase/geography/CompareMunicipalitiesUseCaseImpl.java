package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.application.port.geography.CompareMunicipalitiesUseCase;
import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.geography.MunicipalityNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompareMunicipalitiesUseCaseImpl implements CompareMunicipalitiesUseCase {

    private final MunicipalityRepository repository;
    private final DepartmentRepository departmentRepository;

    @Override
    public Municipality getByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new MunicipalityNotFoundException(uuid));
    }

    @Override
    public List<Municipality> getAllActive() {
        return repository.findAllEnabled();
    }

    @Override
    public List<Municipality> getAllByDepartment(UUID departmentUuid) {
         var department = departmentRepository.findByUuid(departmentUuid)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentUuid));
        Long departmentId = department.getId();
        return repository.findAllByDepartmentIdAndEnabled(departmentId, true);
    }

    @Override
    public Page<Municipality> findAll(Map<String, Object> filters, Pageable pageable) {
        return repository.findAll(filters, pageable);
    }
}
