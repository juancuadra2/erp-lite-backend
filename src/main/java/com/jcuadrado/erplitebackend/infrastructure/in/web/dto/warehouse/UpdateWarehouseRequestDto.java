package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateWarehouseRequestDto(
        @NotBlank @Size(min = 3, max = 100) String name,
        @NotNull WarehouseType type,
        @Size(max = 255) String description,
        @Size(max = 255) String address,
        UUID municipalityId,
        @Size(max = 100) String responsible,
        @Email @Size(max = 100) String email,
        @Pattern(regexp = "[0-9+\\-\\s]{7,20}") String phone
) {}
