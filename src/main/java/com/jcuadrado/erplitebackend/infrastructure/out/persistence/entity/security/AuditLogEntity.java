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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user_id", columnList = "user_id"),
                @Index(name = "idx_audit_entity", columnList = "entity"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "entity", length = 50)
    private String entity;

    @Column(name = "entity_id", columnDefinition = "BINARY(16)")
    private UUID entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
