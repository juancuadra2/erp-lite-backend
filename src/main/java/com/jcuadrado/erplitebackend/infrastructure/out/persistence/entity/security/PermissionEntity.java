package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "permissions",
        indexes = @Index(name = "idx_permission_entity_action", columnList = "entity, action"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "entity", nullable = false, length = 50)
    private String entity;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "condition_expr", length = 255)
    private String conditionExpr;

    @Column(name = "description", length = 255)
    private String description;
}
