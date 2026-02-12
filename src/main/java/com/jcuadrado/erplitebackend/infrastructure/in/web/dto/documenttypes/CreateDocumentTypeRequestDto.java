package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new document type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentTypeRequestDto {

    @NotBlank(message = "Code is required")
    @Size(min = 2, max = 10, message = "Code must be between 2 and 10 characters")
    private String code;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}

