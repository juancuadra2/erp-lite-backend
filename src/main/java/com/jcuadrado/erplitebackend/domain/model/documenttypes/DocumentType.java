package com.jcuadrado.erplitebackend.domain.model.documenttypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model for Document Type (Pure domain - no JPA annotations).
 * Represents a type of identification document (NIT, CC, CE, Passport, etc.)
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

    // Audit fields
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /**
     * Activate this document type
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Deactivate this document type
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Check if document type is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Check if document type is deleted (soft delete)
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Mark document type as deleted (soft delete)
     */
    public void markAsDeleted(Long userId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
        this.active = false;
    }

    /**
     * Normalize code to uppercase
     */
    public void normalizeCode() {
        if (this.code != null) {
            this.code = this.code.toUpperCase().trim();
        }
    }
}

