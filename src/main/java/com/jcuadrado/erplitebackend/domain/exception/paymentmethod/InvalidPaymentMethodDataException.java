package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

/**
 * Exception thrown when payment method data is invalid
 */
public class InvalidPaymentMethodDataException extends RuntimeException {
    
    public InvalidPaymentMethodDataException(String message) {
        super(message);
    }
    
    public InvalidPaymentMethodDataException(String field, String message) {
        super(String.format("Invalid %s: %s", field, message));
    }
    
    public InvalidPaymentMethodDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
