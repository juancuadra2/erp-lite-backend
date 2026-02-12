package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between PaymentMethod domain model and PaymentMethodEntity
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMethodEntityMapper {

    /**
     * Convert domain model to entity
     */
    PaymentMethodEntity toEntity(PaymentMethod paymentMethod);

    /**
     * Convert entity to domain model
     */
    PaymentMethod toDomain(PaymentMethodEntity entity);
}
