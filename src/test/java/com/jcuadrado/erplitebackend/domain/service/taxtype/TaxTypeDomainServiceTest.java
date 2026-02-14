package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxPercentageException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeDataException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for TaxTypeDomainService
 */
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
        // When & Then
        assertThatThrownBy(() -> domainService.validateCode(null))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateCode should throw exception when code is empty")
    void validateCode_shouldThrowExceptionWhenCodeIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateCode(""))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateCode should throw exception when code is blank")
    void validateCode_shouldThrowExceptionWhenCodeIsBlank() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateCode("   "))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateCode should throw exception when code exceeds 20 characters")
    void validateCode_shouldThrowExceptionWhenCodeExceeds20Characters() {
        // Given
        String code = "A".repeat(21);

        // When & Then
        assertThatThrownBy(() -> domainService.validateCode(code))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("cannot exceed 20 characters");
    }

    @Test
    @DisplayName("validateCode should throw exception when code contains lowercase letters")
    void validateCode_shouldThrowExceptionWhenCodeContainsLowercase() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateCode("iva19"))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("uppercase letters");
    }

    @Test
    @DisplayName("validateCode should throw exception when code contains spaces")
    void validateCode_shouldThrowExceptionWhenCodeContainsSpaces() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateCode("IVA 19"))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("uppercase letters");
    }

    @Test
    @DisplayName("validateCode should throw exception when code contains special characters")
    void validateCode_shouldThrowExceptionWhenCodeContainsSpecialCharacters() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateCode("IVA@19"))
                .isInstanceOf(InvalidTaxTypeCodeException.class)
                .hasMessageContaining("uppercase letters");
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with uppercase")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithUppercase() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode("IVA19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with numbers")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithNumbers() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode("IVA123"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with dots")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithDots() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode("IVA.19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with hyphens")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithHyphens() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode("IVA-19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code is valid with underscores")
    void validateCode_shouldNotThrowExceptionWhenCodeIsValidWithUnderscores() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode("IVA_19"));
    }

    @Test
    @DisplayName("validateCode should not throw exception when code has 20 characters")
    void validateCode_shouldNotThrowExceptionWhenCodeHas20Characters() {
        // Given
        String code = "A".repeat(20);

        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage is null")
    void validatePercentage_shouldThrowExceptionWhenPercentageIsNull() {
        // When & Then
        assertThatThrownBy(() -> domainService.validatePercentage(null))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage is negative")
    void validatePercentage_shouldThrowExceptionWhenPercentageIsNegative() {
        // When & Then
        assertThatThrownBy(() -> domainService.validatePercentage(new BigDecimal("-0.0001")))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage exceeds 100")
    void validatePercentage_shouldThrowExceptionWhenPercentageExceeds100() {
        // When & Then
        assertThatThrownBy(() -> domainService.validatePercentage(new BigDecimal("100.0001")))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    @DisplayName("validatePercentage should throw exception when percentage has more than 4 decimals")
    void validatePercentage_shouldThrowExceptionWhenPercentageHasMoreThan4Decimals() {
        // When & Then
        assertThatThrownBy(() -> domainService.validatePercentage(new BigDecimal("19.12345")))
                .isInstanceOf(InvalidTaxPercentageException.class)
                .hasMessageContaining("4 decimal places");
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is zero")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsZero() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validatePercentage(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is 100")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIs100() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("100.0000")));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is valid with 4 decimals")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsValidWith4Decimals() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("19.1234")));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is valid integer")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsValidInteger() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("19")));
    }

    @Test
    @DisplayName("validatePercentage should not throw exception when percentage is valid with 2 decimals")
    void validatePercentage_shouldNotThrowExceptionWhenPercentageIsValidWith2Decimals() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validatePercentage(new BigDecimal("19.12")));
    }

    @Test
    @DisplayName("validateName should throw exception when name is null")
    void validateName_shouldThrowExceptionWhenNameIsNull() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateName(null))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateName should throw exception when name is empty")
    void validateName_shouldThrowExceptionWhenNameIsEmpty() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateName(""))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateName should throw exception when name is blank")
    void validateName_shouldThrowExceptionWhenNameIsBlank() {
        // When & Then
        assertThatThrownBy(() -> domainService.validateName("   "))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("validateName should throw exception when name exceeds 100 characters")
    void validateName_shouldThrowExceptionWhenNameExceeds100Characters() {
        // Given
        String name = "A".repeat(101);

        // When & Then
        assertThatThrownBy(() -> domainService.validateName(name))
                .isInstanceOf(InvalidTaxTypeDataException.class)
                .hasMessageContaining("cannot exceed 100 characters");
    }

    @Test
    @DisplayName("validateName should not throw exception when name is valid")
    void validateName_shouldNotThrowExceptionWhenNameIsValid() {
        // When & Then
        assertDoesNotThrow(() -> domainService.validateName("IVA 19%"));
    }

    @Test
    @DisplayName("validateName should not throw exception when name has 100 characters")
    void validateName_shouldNotThrowExceptionWhenNameHas100Characters() {
        // Given
        String name = "A".repeat(100);

        // When & Then
        assertDoesNotThrow(() -> domainService.validateName(name));
    }

    @Test
    @DisplayName("canBeDeleted should return true when no products and no transactions")
    void canBeDeleted_shouldReturnTrueWhenNoProductsAndNoTransactions() {
        // Given
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
        // Given
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
        // Given
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
        // Given
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
}
