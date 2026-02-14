package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxPercentageException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeDataException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;

import java.math.BigDecimal;

/**
 * TaxTypeDomainService - Servicio de dominio (POJO)
 * 
 * Contiene reglas de negocio que no pertenecen a una entidad específica.
 * Se registra como Bean en BeanConfiguration.
 * NO usa anotaciones Spring (@Service) porque está en la capa de dominio.
 */
public class TaxTypeDomainService {

    private static final BigDecimal MAX_TAX_PERCENTAGE = new BigDecimal("100.0000");
    
    /**
     * Valida el formato del código (BR-TT-001)
     * - No puede ser vacío
     * - Máximo 20 caracteres
     * - Solo letras mayúsculas, números, puntos, guiones y guiones bajos
     */
    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidTaxTypeCodeException("Tax type code cannot be empty");
        }
        if (code.length() > 20) {
            throw new InvalidTaxTypeCodeException("Tax type code cannot exceed 20 characters");
        }
        if (!code.matches("^[A-Z0-9._-]+$")) {
            throw new InvalidTaxTypeCodeException(
                "Tax type code must contain only uppercase letters, numbers, dots, hyphens, and underscores"
            );
        }
    }
    
    /**
     * Valida el porcentaje (BR-TT-002)
     * - No puede ser null
     * - Debe estar entre 0.0000 y 100.0000
     * - Máximo 4 decimales de precisión
     */
    public void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new InvalidTaxPercentageException("Tax percentage cannot be null");
        }
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTaxPercentageException("Percentage must be between 0 and 100");
        }
        if (percentage.compareTo(MAX_TAX_PERCENTAGE) > 0) {
            throw new InvalidTaxPercentageException("Percentage must be between 0 and 100");
        }
        if (percentage.scale() > 4) {
            throw new InvalidTaxPercentageException("Percentage cannot have more than 4 decimal places");
        }
    }

    public boolean isApplicableForSales(TaxType taxType) {
        if (taxType == null) {
            return false;
        }
        TaxApplicationType applicationType = taxType.getApplicationType();
        return applicationType == TaxApplicationType.SALE ||
               applicationType == TaxApplicationType.BOTH;
    }

    public boolean isApplicableForPurchases(TaxType taxType) {
        if (taxType == null) {
            return false;
        }
        TaxApplicationType applicationType = taxType.getApplicationType();
        return applicationType == TaxApplicationType.PURCHASE ||
               applicationType == TaxApplicationType.BOTH;
    }

    public boolean isValidPercentage(BigDecimal percentage) {
        return percentage != null &&
               percentage.compareTo(BigDecimal.ZERO) >= 0 &&
               percentage.compareTo(MAX_TAX_PERCENTAGE) <= 0;
    }
    
    /**
     * Valida el nombre
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidTaxTypeDataException("Tax type name cannot be empty");
        }
        if (name.length() > 100) {
            throw new InvalidTaxTypeDataException("Tax type name cannot exceed 100 characters");
        }
    }
    
    /**
     * Determina si un tipo de impuesto puede ser eliminado (BR-TT-005)
     * No puede eliminarse si tiene productos o transacciones asociadas
     */
    public boolean canBeDeleted(TaxType taxType, long associatedProductsCount, long associatedTransactionsCount) {
        return associatedProductsCount == 0 && associatedTransactionsCount == 0;
    }
}
