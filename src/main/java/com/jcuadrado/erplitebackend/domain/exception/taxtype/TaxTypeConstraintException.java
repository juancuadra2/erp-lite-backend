package com.jcuadrado.erplitebackend.domain.exception.taxtype;

/**
 * TaxTypeConstraintException - Excepción cuando se violan constraints de negocio
 */
public class TaxTypeConstraintException extends RuntimeException {
    
    public TaxTypeConstraintException(String message) {
        super(message);
    }
}
