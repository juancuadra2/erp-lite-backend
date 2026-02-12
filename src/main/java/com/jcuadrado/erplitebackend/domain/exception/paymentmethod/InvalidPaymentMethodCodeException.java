package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

/**
 * Exception thrown when a payment method code format is invalid
 */
public class InvalidPaymentMethodCodeException extends RuntimeException {
    
    public InvalidPaymentMethodCodeException(String message) {
        super(message);
    }
    
    public InvalidPaymentMethodCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
