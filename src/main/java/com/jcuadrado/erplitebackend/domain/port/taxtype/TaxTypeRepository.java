package com.jcuadrado.erplitebackend.domain.port.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * TaxTypeRepository - Puerto de salida (Output Port)
 * 
 * Define las operaciones de persistencia sin implementación.
 * Se implementa en la capa de infraestructura (TaxTypeRepositoryAdapter).
 */
public interface TaxTypeRepository {

    TaxType save(TaxType taxType);
    
    Optional<TaxType> findByUuid(UUID uuid);
    
    Optional<TaxType> findByCode(String code);
    
    void delete(TaxType taxType);

    Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable);
    
    List<TaxType> findByEnabled(Boolean enabled);
    
    List<TaxType> findByNameContaining(String searchTerm, Boolean enabled);
    
    List<TaxType> findByApplicationType(TaxApplicationType applicationType, Boolean enabled);

    boolean existsByCode(String code);
    
    boolean existsByCodeAndUuidNot(String code, UUID uuid);

    long countProductsWithTaxType(UUID taxTypeUuid);
    
    long countTransactionsWithTaxType(UUID taxTypeUuid);
}
