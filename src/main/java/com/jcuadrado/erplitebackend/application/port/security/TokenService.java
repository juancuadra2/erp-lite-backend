package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.domain.model.security.User;

import java.util.List;

public interface TokenService {

    String generateAccessToken(User user, List<String> roles, List<String> permissions);

    String extractUsername(String token);

    boolean validateToken(String token);
}
