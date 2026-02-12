package com.jcuadrado.erplitebackend.domain.port.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Department persistence operations.
 */
public interface DepartmentRepository {

    Department save(Department department);

    Optional<Department> findById(Long id);

    Optional<Department> findByUuid(UUID uuid);

    Optional<Department> findByCode(String code);

    Page<Department> findAll(Map<String, Object> filters, Pageable pageable);

    List<Department> findAllEnabled();

    boolean existsByCode(String code);

    boolean existsByCodeExcludingUuid(String code, UUID excludeUuid);

    void deleteByUuid(UUID uuid);

    long count();
}
