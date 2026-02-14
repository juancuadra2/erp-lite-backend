package com.jcuadrado.erplitebackend.domain.model.taxtype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxType {
    
    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private BigDecimal percentage;
    private Boolean isIncluded;
    private TaxApplicationType applicationType;
    private Boolean enabled;

    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public void activate() {
        this.enabled = true;
        this.deletedBy = null;
        this.deletedAt = null;
    }

    public void deactivate(Long userId) {
        this.enabled = false;
        this.deletedBy = userId;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(enabled);
    }
}
