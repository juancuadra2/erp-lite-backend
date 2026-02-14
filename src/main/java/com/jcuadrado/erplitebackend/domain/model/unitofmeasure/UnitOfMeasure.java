package com.jcuadrado.erplitebackend.domain.model.unitofmeasure;

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
public class UnitOfMeasure {

    private Long id;
    private UUID uuid;
    private String name;
    private String abbreviation;
    private Boolean enabled;
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static UnitOfMeasure create(String name, String abbreviation, Long userId) {
        return UnitOfMeasure.builder()
                .uuid(UUID.randomUUID())
                .name(name)
                .abbreviation(abbreviation.toUpperCase().trim())
                .enabled(true)
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String abbreviation, Long userId) {
        this.name = name;
        this.abbreviation = abbreviation.toUpperCase().trim();
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate(Long userId) {
        this.enabled = false;
        this.deletedBy = userId;
        this.deletedAt = LocalDateTime.now();
        this.updatedBy = userId;
        this.updatedAt = this.deletedAt;
    }

    public void activate(Long userId) {
        this.enabled = true;
        this.deletedBy = null;
        this.deletedAt = null;
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.enabled);
    }
}