package com.jcuadrado.erplitebackend.domain.exception.security;

public class RoleInUseException extends SecurityDomainException {

    public RoleInUseException(String message) {
        super(message);
    }
}
