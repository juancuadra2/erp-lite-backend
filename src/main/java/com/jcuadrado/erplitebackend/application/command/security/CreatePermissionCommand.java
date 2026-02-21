package com.jcuadrado.erplitebackend.application.command.security;

public record CreatePermissionCommand(String entity, String action, String condition, String description) {
}
