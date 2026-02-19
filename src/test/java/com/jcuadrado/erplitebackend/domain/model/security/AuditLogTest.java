package com.jcuadrado.erplitebackend.domain.model.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogTest {

    @Test
    @DisplayName("create should build an audit log with all required fields and a generated id")
    void create_shouldBuildAuditLogWithRequiredFields() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        String username = "admin";
        String entity = "User";
        AuditAction action = AuditAction.USER_CREATED;
        String ipAddress = "127.0.0.1";
        String userAgent = "TestAgent/1.0";

        AuditLog log = AuditLog.create(userId, username, entity, entityId, action, ipAddress, userAgent);

        assertThat(log.getId()).isNotNull();
        assertThat(log.getUserId()).isEqualTo(userId);
        assertThat(log.getUsername()).isEqualTo(username);
        assertThat(log.getEntity()).isEqualTo(entity);
        assertThat(log.getEntityId()).isEqualTo(entityId);
        assertThat(log.getAction()).isEqualTo(action);
        assertThat(log.getIpAddress()).isEqualTo(ipAddress);
        assertThat(log.getUserAgent()).isEqualTo(userAgent);
        assertThat(log.getTimestamp()).isNotNull();
        assertThat(log.getOldValue()).isNull();
        assertThat(log.getNewValue()).isNull();
    }

    @Test
    @DisplayName("create should allow null userId and entityId for system-level events")
    void create_shouldAllowNullUserIdAndEntityId() {
        AuditLog log = AuditLog.create(null, null, "Role", null, AuditAction.ROLE_CREATED, null, null);

        assertThat(log.getId()).isNotNull();
        assertThat(log.getUserId()).isNull();
        assertThat(log.getUsername()).isNull();
        assertThat(log.getEntityId()).isNull();
        assertThat(log.getIpAddress()).isNull();
        assertThat(log.getUserAgent()).isNull();
    }
}
