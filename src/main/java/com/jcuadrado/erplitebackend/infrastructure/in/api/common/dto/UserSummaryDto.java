package com.jcuadrado.erplitebackend.infrastructure.in.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for user summary information in responses.
 * Used to provide basic user details in nested objects like createdBy/updatedBy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    
    private Long id;
    private UUID uuid;
    private String username;
    private String email;
}
