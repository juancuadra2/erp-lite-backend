package com.jcuadrado.erplitebackend.application.usecase.unitofmeasure;

import com.jcuadrado.erplitebackend.application.port.unitofmeasure.CompareUnitsOfMeasureUseCase;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CompareUnitsOfMeasureUseCaseImpl implements CompareUnitsOfMeasureUseCase {

    private final UnitOfMeasureRepository repository;

    @Override
    public UnitOfMeasure getByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new UnitOfMeasureNotFoundException(uuid));
    }

    @Override
    public List<UnitOfMeasure> getAllActive() {
        return repository.findByEnabled(true);
    }

    @Override
    public Page<UnitOfMeasure> findAll(Map<String, Object> filters, Pageable pageable) {
        return repository.findAll(filters, pageable);
    }

    @Override
    public List<UnitOfMeasure> searchByName(String name, Boolean enabled) {
        return repository.findByNameContaining(name, enabled);
    }

    @Override
    public List<UnitOfMeasure> searchByAbbreviation(String abbreviation, Boolean enabled) {
        return repository.findByAbbreviationContaining(abbreviation, enabled);
    }
}
