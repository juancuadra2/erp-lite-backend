package com.jcuadrado.erplitebackend.domain.exception.taxtype;

import java.util.UUID;

public class TaxTypeNotFoundException extends RuntimeException {
    
    public TaxTypeNotFoundException(String message) {
        super(message);
    }
    
    public TaxTypeNotFoundException(UUID uuid) {
        super("Tax type not found with UUID: " + uuid);
    }
}
