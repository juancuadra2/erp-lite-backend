package com.jcuadrado.erplitebackend.domain.exception.taxtype;

/**
 * InvalidTaxTypeCodeException - Excepción cuando el código del tipo de impuesto es inválido
 */
public class InvalidTaxTypeCodeException extends RuntimeException {
    
    public InvalidTaxTypeCodeException(String message) {
        super(message);
    }
}
