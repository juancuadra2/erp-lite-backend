package com.jcuadrado.erplitebackend.application.port.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CompareUnitsOfMeasureUseCase {

    UnitOfMeasure getByUuid(UUID uuid);

    List<UnitOfMeasure> getAllActive();

    Page<UnitOfMeasure> findAll(Map<String, Object> filters, Pageable pageable);

    List<UnitOfMeasure> searchByName(String name, Boolean enabled);

    List<UnitOfMeasure> searchByAbbreviation(String abbreviation, Boolean enabled);
}
