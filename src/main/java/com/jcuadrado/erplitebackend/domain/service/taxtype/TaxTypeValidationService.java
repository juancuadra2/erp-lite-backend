package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;

import java.util.UUID;

/**
 * TaxTypeValidationService - Validaciones que requieren acceso a repositorio
 * 
 * Se registra como Bean en BeanConfiguration y recibe el repository port inyectado.
 * NO usa anotaciones Spring (@Service) porque está en la capa de dominio.
 */
public class TaxTypeValidationService {
    
    private final TaxTypeRepository repository;
    
    public TaxTypeValidationService(TaxTypeRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Asegura que el código sea único (BR-TT-001)
     * 
     * @param code Código a validar
     * @param excludeUuid UUID a excluir (en caso de actualización)
     */
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
