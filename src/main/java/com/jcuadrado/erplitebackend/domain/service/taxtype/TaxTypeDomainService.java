package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxPercentageException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeDataException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;

import java.math.BigDecimal;

public class TaxTypeDomainService {

    private static final BigDecimal MAX_TAX_PERCENTAGE = new BigDecimal("100.0000");

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

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidTaxTypeDataException("Tax type name cannot be empty");
        }
        if (name.length() > 100) {
            throw new InvalidTaxTypeDataException("Tax type name cannot exceed 100 characters");
        }
    }

    public boolean canBeDeleted(TaxType taxType, long associatedProductsCount, long associatedTransactionsCount) {
        return associatedProductsCount == 0 && associatedTransactionsCount == 0;
    }
}
