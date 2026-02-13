package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMethodEntityMapper {

    PaymentMethodEntity toEntity(PaymentMethod paymentMethod);

    PaymentMethod toDomain(PaymentMethodEntity entity);
}
