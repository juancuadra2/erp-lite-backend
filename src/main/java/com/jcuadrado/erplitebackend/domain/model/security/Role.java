package com.jcuadrado.erplitebackend.domain.model.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Role {

    private UUID id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Role create(String name, String description) {
        return Role.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
}
