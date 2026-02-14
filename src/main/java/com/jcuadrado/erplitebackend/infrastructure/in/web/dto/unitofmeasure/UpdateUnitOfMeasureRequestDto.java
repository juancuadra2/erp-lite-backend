package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUnitOfMeasureRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Name can only contain letters and spaces")
    private String name;

    @NotBlank(message = "Abbreviation is required")
    @Size(min = 1, max = 10, message = "Abbreviation must be between 1 and 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9²³]+$", message = "Abbreviation can only contain letters, numbers and superscripts ² ³")
    private String abbreviation;
}
