package com.jcuadrado.erplitebackend.domain.model.taxtype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TaxType - Raíz de Agregado (Aggregate Root)
 * 
 * Representa un tipo de impuesto en el sistema (IVA, ReteFuente, ReteIVA, ICA, etc.).
 * Es el punto de entrada para todas las operaciones relacionadas con tipos de impuestos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxType {
    
    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private BigDecimal percentage;
    private Boolean isIncluded;
    private TaxApplicationType applicationType;
    private Boolean enabled;

    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    /**
     * Activa el tipo de impuesto
     */
    public void activate() {
        this.enabled = true;
        this.deletedBy = null;
        this.deletedAt = null;
    }
    
    /**
     * Desactiva el tipo de impuesto (soft delete)
     */
    public void deactivate(Long userId) {
        this.enabled = false;
        this.deletedBy = userId;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica si está activo
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(enabled);
    }
    
    /**
     * Verifica si es aplicable para ventas
     */
    public boolean isApplicableForSales() {
        return applicationType == TaxApplicationType.SALE || 
               applicationType == TaxApplicationType.BOTH;
    }
    
    /**
     * Verifica si es aplicable para compras
     */
    public boolean isApplicableForPurchases() {
        return applicationType == TaxApplicationType.PURCHASE || 
               applicationType == TaxApplicationType.BOTH;
    }
    
    /**
     * Valida si el porcentaje está en rango válido
     */
    public boolean isValidPercentage() {
        return percentage != null && 
               percentage.compareTo(BigDecimal.ZERO) >= 0 && 
               percentage.compareTo(new BigDecimal("100.0000")) <= 0;
    }
}
