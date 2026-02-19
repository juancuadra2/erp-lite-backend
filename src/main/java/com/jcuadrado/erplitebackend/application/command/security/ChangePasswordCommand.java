package com.jcuadrado.erplitebackend.application.command.security;

import java.util.UUID;

public record ChangePasswordCommand(String currentPassword, String newPassword, UUID requestedBy) {
}
