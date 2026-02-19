package com.jcuadrado.erplitebackend.application.usecase.taxtype;

import com.jcuadrado.erplitebackend.application.port.taxtype.CompareTaxTypesUseCase;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;

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
public class CompareTaxTypesUseCaseImpl implements CompareTaxTypesUseCase {
    
    private final TaxTypeRepository repository;
    
    @Override
    @Transactional(readOnly = true)
    public TaxType getByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));
    }
    
    @Override
    @Transactional(readOnly = true)
    public TaxType getByCode(String code) {
        return repository.findByCode(code)
            .orElseThrow(() -> new TaxTypeNotFoundException("Tax type not found with code: " + code));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TaxType> getAllActive() {
        return repository.findByEnabled(true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable) {
        return repository.findAll(filters, pageable);
    }
}
