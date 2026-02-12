package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.application.port.geography.CompareDepartmentsUseCase;
import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
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
public class CompareDepartmentsUseCaseImpl implements CompareDepartmentsUseCase {

    private final DepartmentRepository repository;

    @Override
    public Department getByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new DepartmentNotFoundException(uuid));
    }

    @Override
    public Department getByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new DepartmentNotFoundException(code));
    }

    @Override
    public List<Department> getAllActive() {
        return repository.findAllEnabled();
    }

    @Override
    public Page<Department> findAll(Map<String, Object> filters, Pageable pageable) {
        return repository.findAll(filters, pageable);
    }
}
