package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TaxTypeResponseDto - DTO for tax type responses
 * 
 * Uses Java Record for immutability and automatic generation of equals/hashCode/toString.
 */
@Builder
public record TaxTypeResponseDto(
    Long id,
    UUID uuid,
    String code,
    String name,
    BigDecimal percentage,
    Boolean isIncluded,
    TaxApplicationType applicationType,
    Boolean enabled,
    Long createdBy,
    Long updatedBy,
    @JsonIgnore Long deletedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @JsonIgnore LocalDateTime deletedAt
) {}
