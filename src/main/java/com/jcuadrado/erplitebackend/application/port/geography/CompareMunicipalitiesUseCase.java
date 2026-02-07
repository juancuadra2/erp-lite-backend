package com.jcuadrado.erplitebackend.application.port.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Input port for municipality query operations (CQRS - Query side).
 */
public interface CompareMunicipalitiesUseCase {

    Municipality getByUuid(UUID uuid);

    List<Municipality> getAllActive();

    Page<Municipality> findAll(Map<String, Object> filters, Pageable pageable);
}
