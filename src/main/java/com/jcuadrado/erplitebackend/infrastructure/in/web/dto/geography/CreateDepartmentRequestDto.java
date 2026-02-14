package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequestDto {

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "\\d{2}", message = "Code must be exactly 2 digits")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
}
