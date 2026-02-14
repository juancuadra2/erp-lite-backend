package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import org.mapstruct.Mapper;

/**
 * TaxTypeEntityMapper - MapStruct mapper for TaxType <-> TaxTypeEntity
 * 
 * Handles bidirectional mapping between domain model and JPA entity.
 */
@Mapper(componentModel = "spring")
public interface TaxTypeEntityMapper {
    
    /**
     * Maps domain model to JPA entity
     */
    TaxTypeEntity toEntity(TaxType taxType);
    
    /**
     * Maps JPA entity to domain model
     */
    TaxType toDomain(TaxTypeEntity entity);
}
