package com.jcuadrado.erplitebackend.application.command.security;

public record LoginResponse(String accessToken, String refreshToken, long expiresIn) {
}
