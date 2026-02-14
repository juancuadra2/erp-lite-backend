package com.jcuadrado.erplitebackend.domain.exception.taxtype;

public class InvalidTaxPercentageException extends RuntimeException {
    
    public InvalidTaxPercentageException(String message) {
        super(message);
    }
}
