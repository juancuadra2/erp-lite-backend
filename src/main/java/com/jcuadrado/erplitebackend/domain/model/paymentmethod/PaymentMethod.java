package com.jcuadrado.erplitebackend.domain.model.paymentmethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PaymentMethod - Aggregate Root (Pure domain model - no JPA annotations)
 * 
 * Represents a payment method in the system (cash, credit card, transfer, etc.).
 * This is the entry point for all operations related to payment methods.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    private Long id;
    private UUID uuid;
    private String code;                     // Unique, max 30 chars, uppercase + numbers + underscores
    private String name;                     // max 100 chars
    private Boolean enabled;
    
    // Audit fields (modeled as Long in domain, resolved in external layers)
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // ========== Business Methods ==========
    
    /**
     * Activate the payment method
     */
    public void activate() {
        this.enabled = true;
        this.deletedBy = null;
        this.deletedAt = null;
    }
    
    /**
     * Deactivate the payment method (soft delete)
     */
    public void deactivate(Long userId) {
        this.enabled = false;
        this.deletedBy = userId;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * Check if payment method is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(enabled);
    }
    
    /**
     * Check if payment method is deleted (soft delete)
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
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
