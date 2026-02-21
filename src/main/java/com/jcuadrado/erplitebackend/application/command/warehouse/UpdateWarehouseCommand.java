package com.jcuadrado.erplitebackend.application.command.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;

import java.util.UUID;

public record UpdateWarehouseCommand(
        String name,
        String description,
        WarehouseType type,
        String address,
        UUID municipalityId,
        String responsible,
        String email,
        String phone
) {}
