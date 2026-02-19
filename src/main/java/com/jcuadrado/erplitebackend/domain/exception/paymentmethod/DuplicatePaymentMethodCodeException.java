package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

public class DuplicatePaymentMethodCodeException extends RuntimeException {
    
    public DuplicatePaymentMethodCodeException(String code) {
        super(String.format("Payment method with code '%s' already exists", code));
    }
    
    public DuplicatePaymentMethodCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
