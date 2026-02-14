package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxPercentageException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeDataException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("TaxTypeDomainService - Unit Tests")
class TaxTypeDomainServiceTest {

    private TaxTypeDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new TaxTypeDomainService();
    }

    @Test
    @DisplayName("validateCode should throw exception when code is null")
    void validateCode_shouldThrowExceptionWhenCodeIsNull() {
        assertThatThrownBy(() -> domainService.validateCode(null))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateCode should throw exception when code is empty")
    void validateCode_shouldThrowExceptionWhenCodeIsEmpty() {
        assertThatThrownBy(() -> domainService.validateCode(""))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateCode should throw exception when code is blank")
    void validateCode_shouldThrowExceptionWhenCodeIsBlank() {
        assertThatThrownBy(() -> domainService.validateCode("   "))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateCode should throw exception when code exceeds 20 characters")
    void validateCode_shouldThrowExceptionWhenCodeExceeds20Characters() {
        String code = "A".repeat(21);

        assertThatThrownBy(() -> domainService.validateCode(code))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot exceed 20 characters");
    }

    @Test
    @DisplayName("validateCode should throw exception when code contains lowercase letters")
    void validateCode_shouldThrowExceptionWhenCodeContainsLowercase() {
        assertThatThrownBy(() -> domainService.validateCode("iva19"))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("uppercase letters");
    }

    @Test
    @DisplayName("validateCode should throw exception when code contains spaces")
    void validateCode_shouldThrowExceptionWhenCodeContainsSpaces() {
        assertThatThrownBy(() -> domainService.validateCode("IVA 19"))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("uppercase letters");
    }

    @Test
    @DisplayName("validateCode should throw exception when code contains special characters")
    void validateCode_shouldThrowExceptionWhenCodeContainsSpecialCharacters() {
        assertThatThrownBy(() -> domainService.validateCode("IVA@19"))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("uppercase letters");
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with uppercase")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithUppercase() {
        assertDoesNotThrow(() -> domainService.validateCode("IVA19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with numbers")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithNumbers() {
        assertDoesNotThrow(() -> domainService.validateCode("IVA123"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with dots")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithDots() {
        assertDoesNotThrow(() -> domainService.validateCode("IVA.19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with hyphens")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithHyphens() {
        assertDoesNotThrow(() -> domainService.validateCode("IVA-19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with underscores")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithUnderscores() {
        assertDoesNotThrow(() -> domainService.validateCode("IVA_19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code has 20 characters")
    void validateCode_shouldNotThrowExceptionWhenCodeHas20Characters() {
        String code = "A".repeat(20);

        assertDoesNotThrow(() -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage is null")
    void validatePercentage_shouldThrowExceptionWhenPercentageIsNull() {
        assertThatThrownBy(() -> domainService.validatePercentage(null))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage is negative")
    void validatePercentage_shouldThrowExceptionWhenPercentageIsNegative() {
        assertThatThrownBy(() -> domainService.validatePercentage(new BigDecimal("-0.0001")))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage exceeds 100")
    void validatePercentage_shouldThrowExceptionWhenPercentageExceeds100() {
        assertThatThrownBy(() -> domainService.validatePercentage(new BigDecimal("100.0001")))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage has more than 4 decimals")
    void validatePercentage_shouldThrowExceptionWhenPercentageHasMoreThan4Decimals() {
        assertThatThrownBy(() -> domainService.validatePercentage(new BigDecimal("19.12345")))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("4 decimal places");
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is zero")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsZero() {
        assertDoesNotThrow(() -> domainService.validatePercentage(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is 100")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIs100() {
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("100.0000")));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is valid with 4 decimals")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsValidWith4Decimals() {
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("19.1234")));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is valid integer")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsValidInteger() {
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("19")));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is valid with 2 decimals")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsValidWith2Decimals() {
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("19.12")));
    }

    @Test
    @DisplayName("validateName should throw exception when name is null")
    void validateName_shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> domainService.validateName(null))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateName should throw exception when name is empty")
    void validateName_shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> domainService.validateName(""))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateName should throw exception when name is blank")
    void validateName_shouldThrowExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> domainService.validateName("   "))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateName should throw exception when name exceeds 100 characters")
    void validateName_shouldThrowExceptionWhenNameExceeds100Characters() {
        String name = "A".repeat(101);

        assertThatThrownBy(() -> domainService.validateName(name))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot exceed 100 characters");
    }

    @Test
    @DisplayName("validateName should not throw exception when name is valid")
    void validateName_shouldNotThrowExceptionWhenNameIsValid() {
        assertDoesNotThrow(() -> domainService.validateName("IVA 19%"));
    }

    @Test
    @DisplayName("validateName should not throw exception when name has 100 characters")
    void validateName_shouldNotThrowExceptionWhenNameHas100Characters() {
        String name = "A".repeat(100);

        assertDoesNotThrow(() -> domainService.validateName(name));
    }

    @Test
    @DisplayName("canBeDeleted should return true when no products and no transactions")
    void canBeDeleted_shouldReturnTrueWhenNoProductsAndNoTransactions() {
        TaxType taxType = TaxType.builder()
                .uuid(UUID.randomUUID())
                .code("IVA19")
                .name("IVA 19%")
                .build();

        // When
        boolean canBeDeleted = domainService.canBeDeleted(taxType, 0, 0);

        // Then
        assertThat(canBeDeleted).isTrue();
    }

    @Test
    @DisplayName("canBeDeleted should return false when has associated products")
    void canBeDeleted_shouldReturnFalseWhenHasAssociatedProducts() {
        TaxType taxType = TaxType.builder()
                .uuid(UUID.randomUUID())
                .code("IVA19")
                .name("IVA 19%")
                .build();

        // When
        boolean canBeDeleted = domainService.canBeDeleted(taxType, 5, 0);

        // Then
        assertThat(canBeDeleted).isFalse();
    }

    @Test
    @DisplayName("canBeDeleted should return false when has associated transactions")
    void canBeDeleted_shouldReturnFalseWhenHasAssociatedTransactions() {
        TaxType taxType = TaxType.builder()
                .uuid(UUID.randomUUID())
                .code("IVA19")
                .name("IVA 19%")
                .build();

        // When
        boolean canBeDeleted = domainService.canBeDeleted(taxType, 0, 10);

        // Then
        assertThat(canBeDeleted).isFalse();
    }

    @Test
    @DisplayName("canBeDeleted should return false when has both products and transactions")
    void canBeDeleted_shouldReturnFalseWhenHasBothProductsAndTransactions() {
        TaxType taxType = TaxType.builder()
                .uuid(UUID.randomUUID())
                .code("IVA19")
                .name("IVA 19%")
                .build();

        // When
        boolean canBeDeleted = domainService.canBeDeleted(taxType, 5, 10);

        // Then
        assertThat(canBeDeleted).isFalse();
    }

    @Test
    @DisplayName("isApplicableForSales should return true when application type is SALE")
    void isApplicableForSales_shouldReturnTrueWhenApplicationTypeIsSale() {
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.SALE)
                .build();

        // When
        boolean applies = domainService.isApplicableForSales(taxType);

        // Then
        assertThat(applies).isTrue();
    }

    @Test
    @DisplayName("isApplicableForSales should return true when application type is BOTH")
    void isApplicableForSales_shouldReturnTrueWhenApplicationTypeIsBoth() {
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.BOTH)
                .build();

        // When
        boolean applies = domainService.isApplicableForSales(taxType);

        // Then
        assertThat(applies).isTrue();
    }

    @Test
    @DisplayName("isApplicableForSales should return false when application type is PURCHASE")
    void isApplicableForSales_shouldReturnFalseWhenApplicationTypeIsPurchase() {
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.PURCHASE)
                .build();

        // When
        boolean applies = domainService.isApplicableForSales(taxType);

        // Then
        assertThat(applies).isFalse();
    }

    @Test
    @DisplayName("isApplicableForPurchases should return true when application type is PURCHASE")
    void isApplicableForPurchases_shouldReturnTrueWhenApplicationTypeIsPurchase() {
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.PURCHASE)
                .build();

        // When
        boolean applies = domainService.isApplicableForPurchases(taxType);

        // Then
        assertThat(applies).isTrue();
    }

    @Test
    @DisplayName("isApplicableForPurchases should return true when application type is BOTH")
    void isApplicableForPurchases_shouldReturnTrueWhenApplicationTypeIsBoth() {
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.BOTH)
                .build();

        // When
        boolean applies = domainService.isApplicableForPurchases(taxType);

        // Then
        assertThat(applies).isTrue();
    }

    @Test
    @DisplayName("isApplicableForPurchases should return false when application type is SALE")
    void isApplicableForPurchases_shouldReturnFalseWhenApplicationTypeIsSale() {
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.SALE)
                .build();

        // When
        boolean applies = domainService.isApplicableForPurchases(taxType);

        // Then
        assertThat(applies).isFalse();
    }

    @Test
    @DisplayName("isApplicableForSales should return false when taxType is null")
    void isApplicableForSales_shouldReturnFalseWhenTaxTypeIsNull() {
        // When
        boolean applies = domainService.isApplicableForSales(null);

        // Then
        assertThat(applies).isFalse();
    }

    @Test
    @DisplayName("isApplicableForPurchases should return false when taxType is null")
    void isApplicableForPurchases_shouldReturnFalseWhenTaxTypeIsNull() {
        // When
        boolean applies = domainService.isApplicableForPurchases(null);

        // Then
        assertThat(applies).isFalse();
    }

    @Test
    @DisplayName("isValidPercentage should return true when percentage is valid")
    void isValidPercentage_shouldReturnTrueWhenPercentageIsValid() {
        // When
        boolean isValid = domainService.isValidPercentage(new BigDecimal("19.0000"));

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isValidPercentage should return true when percentage is zero")
    void isValidPercentage_shouldReturnTrueWhenPercentageIsZero() {
        // When
        boolean isValid = domainService.isValidPercentage(BigDecimal.ZERO);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isValidPercentage should return true when percentage is one hundred")
    void isValidPercentage_shouldReturnTrueWhenPercentageIsOneHundred() {
        // When
        boolean isValid = domainService.isValidPercentage(new BigDecimal("100.0000"));

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isValidPercentage should return false when percentage is negative")
    void isValidPercentage_shouldReturnFalseWhenPercentageIsNegative() {
        // When
        boolean isValid = domainService.isValidPercentage(new BigDecimal("-1.0000"));

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isValidPercentage should return false when percentage exceeds one hundred")
    void isValidPercentage_shouldReturnFalseWhenPercentageExceedsOneHundred() {
        // When
        boolean isValid = domainService.isValidPercentage(new BigDecimal("100.0001"));

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isValidPercentage should return false when percentage is null")
    void isValidPercentage_shouldReturnFalseWhenPercentageIsNull() {
        // When
        boolean isValid = domainService.isValidPercentage(null);

        // Then
        assertThat(isValid).isFalse();
    }
}
