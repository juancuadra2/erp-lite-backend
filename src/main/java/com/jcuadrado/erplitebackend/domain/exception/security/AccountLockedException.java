package com.jcuadrado.erplitebackend.domain.exception.security;

public class AccountLockedException extends SecurityDomainException {

    public AccountLockedException(String message) {
        super(message);
    }
}
