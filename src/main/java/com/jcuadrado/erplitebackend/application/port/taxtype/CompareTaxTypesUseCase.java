package com.jcuadrado.erplitebackend.application.port.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CompareTaxTypesUseCase - Casos de uso de consulta (CQRS - Query Side)
 */
public interface CompareTaxTypesUseCase {
    
    /**
     * Obtiene un tipo de impuesto por UUID
     */
    TaxType getByUuid(UUID uuid);
    
    /**
     * Obtiene un tipo de impuesto por código
     */
    TaxType getByCode(String code);
    
    /**
     * Obtiene todos los tipos de impuestos activos
     */
    List<TaxType> getAllActive();
    
    /**
     * Busca tipos de impuestos con filtros y paginación
     * Filtros soportados: enabled (Boolean), applicationType (TaxApplicationType), name (String)
     * El filtro 'name' realiza búsqueda case-insensitive parcial
     */
    Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable);
}
