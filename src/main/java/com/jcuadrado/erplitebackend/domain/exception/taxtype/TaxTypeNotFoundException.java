package com.jcuadrado.erplitebackend.domain.exception.taxtype;

import java.util.UUID;

/**
 * TaxTypeNotFoundException - Excepción cuando no se encuentra un tipo de impuesto
 */
public class TaxTypeNotFoundException extends RuntimeException {
    
    public TaxTypeNotFoundException(String message) {
        super(message);
    }
    
    public TaxTypeNotFoundException(UUID uuid) {
        super("Tax type not found with UUID: " + uuid);
    }
}
