package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UnitOfMeasureResponseDto(
        Long id,
        UUID uuid,
        String name,
        String abbreviation,
        Boolean enabled,
        Long createdBy,
        Long updatedBy,
        @JsonIgnore Long deletedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        @JsonIgnore LocalDateTime deletedAt
) {
}
