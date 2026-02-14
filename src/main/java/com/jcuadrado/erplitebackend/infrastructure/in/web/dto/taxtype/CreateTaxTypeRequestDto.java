package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * CreateTaxTypeRequestDto - DTO for creating a new tax type
 * 
 * Includes Bean Validation constraints for structural validation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaxTypeRequestDto {

    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9._-]+$", 
             message = "Code must contain only uppercase letters, numbers, dots, hyphens, and underscores")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", message = "Percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Percentage must be between 0 and 100")
    @Digits(integer = 3, fraction = 4, message = "Percentage cannot have more than 4 decimal places")
    private BigDecimal percentage;

    @NotNull(message = "isIncluded is required")
    private Boolean isIncluded;

    @NotNull(message = "applicationType is required")
    private TaxApplicationType applicationType;
}
