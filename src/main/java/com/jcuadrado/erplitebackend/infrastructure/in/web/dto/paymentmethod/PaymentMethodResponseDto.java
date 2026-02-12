package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for payment method response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponseDto {

    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private Boolean enabled;

    // Audit fields
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
