package com.jcuadrado.erplitebackend.domain.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Warehouse {

    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private String description;
    private WarehouseType type;
    private String address;
    private UUID municipalityId;
    private String responsible;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
