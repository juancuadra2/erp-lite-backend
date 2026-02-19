package com.jcuadrado.erplitebackend.application.command.security;

import java.util.List;
import java.util.UUID;

public record CreateRoleCommand(String name, String description, List<UUID> permissionIds) {
}
