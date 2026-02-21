package com.jcuadrado.erplitebackend.domain.exception.security;

public class DuplicateEmailException extends SecurityDomainException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
