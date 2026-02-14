package com.jcuadrado.erplitebackend.application.usecase.taxtype;

import com.jcuadrado.erplitebackend.application.port.taxtype.ManageTaxTypeUseCase;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ManageTaxTypeUseCaseImpl - Implementación de casos de uso de comando
 * 
 * Implementa el patrón CQRS (Command Side) para operaciones de escritura.
 */
@Service
@RequiredArgsConstructor
public class ManageTaxTypeUseCaseImpl implements ManageTaxTypeUseCase {
    
    private final TaxTypeRepository repository;
    private final TaxTypeDomainService domainService;
    private final TaxTypeValidationService validationService;
    
    @Override
    @Transactional
    public TaxType create(TaxType taxType) {
        domainService.validateCode(taxType.getCode());
        domainService.validateName(taxType.getName());
        domainService.validatePercentage(taxType.getPercentage());

        validationService.ensureCodeIsUnique(taxType.getCode(), null);

        if (taxType.getUuid() == null) {
            taxType.setUuid(UUID.randomUUID());
        }

        if (taxType.getEnabled() == null) {
            taxType.setEnabled(true);
        }

        taxType.setCreatedAt(LocalDateTime.now());

        return repository.save(taxType);
    }
    
    @Override
    @Transactional
    public TaxType update(UUID uuid, TaxType updates) {
        TaxType existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));

        if (updates.getCode() != null) {
            domainService.validateCode(updates.getCode());
            if (!updates.getCode().equals(existing.getCode())) {
                validationService.ensureCodeIsUnique(updates.getCode(), uuid);
            }
            existing.setCode(updates.getCode());
        }
        
        if (updates.getName() != null) {
            domainService.validateName(updates.getName());
            existing.setName(updates.getName());
        }
        
        if (updates.getPercentage() != null) {
            domainService.validatePercentage(updates.getPercentage());
            existing.setPercentage(updates.getPercentage());
        }
        
        if (updates.getIsIncluded() != null) {
            existing.setIsIncluded(updates.getIsIncluded());
        }
        
        if (updates.getApplicationType() != null) {
            existing.setApplicationType(updates.getApplicationType());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        return repository.save(existing);
    }
    
    @Override
    @Transactional
    public void activate(UUID uuid) {
        TaxType taxType = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));
        
        taxType.activate();
        taxType.setUpdatedAt(LocalDateTime.now());
        repository.save(taxType);
    }
    
    @Override
    @Transactional
    public void deactivate(UUID uuid) {
        TaxType taxType = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));

        Long userId = 1L;
        
        taxType.deactivate(userId);
        taxType.setUpdatedAt(LocalDateTime.now());
        repository.save(taxType);
    }
    
    @Override
    @Transactional
    public void delete(UUID uuid) {
        TaxType taxType = repository.findByUuid(uuid)
            .orElseThrow(() -> new TaxTypeNotFoundException(uuid));

        long productsCount = repository.countProductsWithTaxType(uuid);
        long transactionsCount = repository.countTransactionsWithTaxType(uuid);
        
        if (!domainService.canBeDeleted(taxType, productsCount, transactionsCount)) {
            throw new TaxTypeConstraintException(
                "Cannot delete tax type with associated products or transactions"
            );
        }
        
        repository.delete(taxType);
    }
}
