package com.jcuadrado.erplitebackend.domain.model.taxtype;

/**
 * TaxApplicationType - Enum de tipo de aplicación
 * 
 * Define en qué contexto se aplica el impuesto.
 */
public enum TaxApplicationType {
    /**
     * Solo para ventas
     */
    SALE,
    
    /**
     * Solo para compras
     */
    PURCHASE,
    
    /**
     * Aplicable a ambos contextos (ventas y compras)
     */
    BOTH
}
