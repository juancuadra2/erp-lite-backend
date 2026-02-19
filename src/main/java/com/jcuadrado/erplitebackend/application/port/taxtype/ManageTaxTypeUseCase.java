package com.jcuadrado.erplitebackend.application.port.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;

import java.util.UUID;

/**
 * ManageTaxTypeUseCase - Casos de uso de comando (CQRS - Command Side)
 */
public interface ManageTaxTypeUseCase {
    
    /**
     * Crea un nuevo tipo de impuesto
     */
    TaxType create(TaxType taxType);
    
    /**
     * Actualiza un tipo de impuesto existente
     */
    TaxType update(UUID uuid, TaxType updates);
    
    /**
     * Activa un tipo de impuesto
     */
    void activate(UUID uuid);
    
    /**
     * Desactiva un tipo de impuesto (soft delete)
     */
    void deactivate(UUID uuid);
    
    /**
     * Elimina un tipo de impuesto (solo si no tiene productos ni transacciones)
     */
    void delete(UUID uuid);
}
