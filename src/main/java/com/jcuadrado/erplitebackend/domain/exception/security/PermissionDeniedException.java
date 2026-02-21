package com.jcuadrado.erplitebackend.domain.exception.security;

public class PermissionDeniedException extends SecurityDomainException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}
