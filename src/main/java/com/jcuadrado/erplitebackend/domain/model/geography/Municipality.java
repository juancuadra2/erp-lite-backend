package com.jcuadrado.erplitebackend.domain.model.geography;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Municipality (Pure domain - no JPA annotations).
 * Represents a Colombian municipality, child of a Department.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Municipality {

    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private Department department;
    private Boolean enabled;

    // Audit fields
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void activate() {
        this.enabled = true;
    }

    public void deactivate() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.enabled);
    }
}
