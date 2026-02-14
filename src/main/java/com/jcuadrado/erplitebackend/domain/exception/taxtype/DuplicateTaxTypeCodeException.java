package com.jcuadrado.erplitebackend.domain.exception.taxtype;

/**
 * DuplicateTaxTypeCodeException - Excepción cuando se intenta crear un tipo de impuesto con código duplicado
 */
public class DuplicateTaxTypeCodeException extends RuntimeException {
    
    public DuplicateTaxTypeCodeException(String message) {
        super(message);
    }
}
