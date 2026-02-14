package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;

import java.util.UUID;

public class TaxTypeValidationService {
    
    private final TaxTypeRepository repository;
    
    public TaxTypeValidationService(TaxTypeRepository repository) {
        this.repository = repository;
    }
    
    public void ensureCodeIsUnique(String code, UUID excludeUuid) {
        boolean exists;
        if (excludeUuid != null) {
            exists = repository.existsByCodeAndUuidNot(code, excludeUuid);
        } else {
            exists = repository.existsByCode(code);
        }
        
        if (exists) {
            throw new DuplicateTaxTypeCodeException(
                "Tax type with code '" + code + "' already exists"
            );
        }
    }
}
