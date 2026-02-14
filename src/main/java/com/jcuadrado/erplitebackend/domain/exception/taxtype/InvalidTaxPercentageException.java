package com.jcuadrado.erplitebackend.domain.exception.taxtype;

/**
 * InvalidTaxPercentageException - Excepción cuando el porcentaje del impuesto es inválido
 */
public class InvalidTaxPercentageException extends RuntimeException {
    
    public InvalidTaxPercentageException(String message) {
        super(message);
    }
}
