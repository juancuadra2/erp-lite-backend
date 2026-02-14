package com.jcuadrado.erplitebackend.domain.exception.taxtype;

/**
 * InvalidTaxTypeDataException - Excepción cuando los datos del tipo de impuesto son inválidos
 */
public class InvalidTaxTypeDataException extends RuntimeException {
    
    public InvalidTaxTypeDataException(String message) {
        super(message);
    }
}
