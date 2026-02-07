package com.jcuadrado.erplitebackend.application.port.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Input port for department query operations (CQRS - Query side).
 */
public interface CompareDepartmentsUseCase {

    Department getByUuid(UUID uuid);

    Department getByCode(String code);

    List<Department> getAllActive();

    Page<Department> findAll(Map<String, Object> filters, Pageable pageable);
}
