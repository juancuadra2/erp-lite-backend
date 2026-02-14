package com.jcuadrado.erplitebackend.domain.exception.taxtype;

public class InvalidTaxTypeCodeException extends RuntimeException {
    
    public InvalidTaxTypeCodeException(String message) {
        super(message);
    }
}
