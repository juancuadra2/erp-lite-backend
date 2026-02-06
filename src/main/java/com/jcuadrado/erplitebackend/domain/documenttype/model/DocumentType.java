package com.jcuadrado.erplitebackend.domain.documenttype.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for DocumentType aggregate root.
 * Represents a type of identification document (NIT, CC, Passport, etc.)
 * that can be used to identify natural or legal persons in the system.
 * <p>
 * This is a pure domain object without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentType {
    
    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private String description;
    private Boolean active;
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /**
     * Activates the document type.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivates the document type (soft delete).
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Checks if the document type is active.
     * 
     * @return true if the document type is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}
