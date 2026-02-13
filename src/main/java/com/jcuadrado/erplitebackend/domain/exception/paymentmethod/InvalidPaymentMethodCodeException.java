package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

public class InvalidPaymentMethodCodeException extends RuntimeException {
    
    public InvalidPaymentMethodCodeException(String message) {
        super(message);
    }
    
    public InvalidPaymentMethodCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
