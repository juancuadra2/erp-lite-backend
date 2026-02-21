package com.jcuadrado.erplitebackend.domain.model.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class User {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private UUID documentTypeId;
    private String documentNumber;
    private boolean active;
    private int failedAttempts;
    private LocalDateTime lockedAt;
    private LocalDateTime lastLogin;
    private LocalDateTime lastFailedLoginAt;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
    private LocalDateTime deletedAt;

    public static User create(String username, String email, String passwordHash,
                              String firstName, String lastName,
                              UUID documentTypeId, String documentNumber,
                              UUID createdBy) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .firstName(firstName)
                .lastName(lastName)
                .documentTypeId(documentTypeId)
                .documentNumber(documentNumber)
                .active(true)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
        this.lastFailedLoginAt = LocalDateTime.now();
        if (this.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            this.active = false;
            this.lockedAt = LocalDateTime.now();
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lastFailedLoginAt = null;
    }

    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        resetFailedAttempts();
    }

    public void unlock() {
        this.active = true;
        this.failedAttempts = 0;
        this.lockedAt = null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void update(String email, String firstName, String lastName,
                       UUID documentTypeId, String documentNumber, UUID updatedBy) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentTypeId = documentTypeId;
        this.documentNumber = documentNumber;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    public void updatePasswordHash(String newPasswordHash, UUID updatedBy) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    public boolean isLocked() {
        return !this.active && this.failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
