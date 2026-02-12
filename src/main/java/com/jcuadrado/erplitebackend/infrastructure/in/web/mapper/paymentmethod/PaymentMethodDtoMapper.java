package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.CreatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.PaymentMethodResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.UpdatePaymentMethodRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between DTOs and PaymentMethod domain model
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMethodDtoMapper {

    /**
     * Convert CreatePaymentMethodRequestDto to domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    PaymentMethod toDomain(CreatePaymentMethodRequestDto dto);

    /**
     * Convert UpdatePaymentMethodRequestDto to domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    PaymentMethod toDomain(UpdatePaymentMethodRequestDto dto);

    /**
     * Convert domain model to PaymentMethodResponseDto
     */
    PaymentMethodResponseDto toResponseDto(PaymentMethod paymentMethod);
}
