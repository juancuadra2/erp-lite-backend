package com.jcuadrado.erplitebackend.domain.port.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Municipality persistence operations.
 */
public interface MunicipalityRepository {

    Municipality save(Municipality municipality);

    Optional<Municipality> findById(Long id);

    Optional<Municipality> findByUuid(UUID uuid);

    Optional<Municipality> findByCodeAndDepartmentId(String code, Long departmentId);

    Page<Municipality> findAll(Map<String, Object> filters, Pageable pageable);

    List<Municipality> findAllEnabled();

    boolean existsByCodeAndDepartmentId(String code, Long departmentId);

    boolean existsByCodeAndDepartmentIdExcludingUuid(String code, Long departmentId, UUID excludeUuid);

    void deleteByUuid(UUID uuid);

    long countByDepartmentId(Long departmentId);
}
