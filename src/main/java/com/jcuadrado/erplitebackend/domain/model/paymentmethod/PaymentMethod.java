package com.jcuadrado.erplitebackend.domain.model.paymentmethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    private Long id;
    private UUID uuid;
    private String code;
    private String name;
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

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void normalizeCode() {
        if (this.code != null) {
            this.code = this.code.toUpperCase().trim();
        }
    }
}
