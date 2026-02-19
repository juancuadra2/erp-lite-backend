package com.jcuadrado.erplitebackend.application.command.security;

public record LoginCommand(String username, String password, String ipAddress, String userAgent) {
}
