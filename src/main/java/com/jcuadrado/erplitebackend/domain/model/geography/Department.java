package com.jcuadrado.erplitebackend.domain.model.geography;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain model for Department (Pure domain - no JPA annotations).
 * Represents a Colombian department (administrative division).
 * Aggregate Root of the Geography module.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private Boolean enabled;

    // Relationships
    private List<Municipality> municipalities;

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

    public boolean canBeDeleted() {
        return municipalities == null || municipalities.isEmpty();
    }
}
