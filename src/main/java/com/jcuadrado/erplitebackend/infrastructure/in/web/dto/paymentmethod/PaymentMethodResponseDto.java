package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentMethodResponseDto(
    Long id,
    UUID uuid,
    String code,
    String name,
    Boolean enabled,
    Long createdBy,
    Long updatedBy,
    @JsonIgnore Long deletedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @JsonIgnore LocalDateTime deletedAt
) {}
