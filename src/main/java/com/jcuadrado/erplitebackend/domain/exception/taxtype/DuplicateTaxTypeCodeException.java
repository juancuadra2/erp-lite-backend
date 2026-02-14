package com.jcuadrado.erplitebackend.domain.exception.taxtype;

public class DuplicateTaxTypeCodeException extends RuntimeException {
    
    public DuplicateTaxTypeCodeException(String message) {
        super(message);
    }
}
