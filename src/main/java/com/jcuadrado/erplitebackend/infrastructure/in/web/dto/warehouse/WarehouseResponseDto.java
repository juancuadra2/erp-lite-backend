package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;

import java.time.LocalDateTime;
import java.util.UUID;

public record WarehouseResponseDto(
        UUID uuid,
        String code,
        String name,
        String description,
        WarehouseType type,
        String address,
        UUID municipalityId,
        String responsible,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
