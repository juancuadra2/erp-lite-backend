package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

public class PaymentMethodConstraintException extends RuntimeException {
    
    public PaymentMethodConstraintException(String message) {
        super(message);
    }
    
    public PaymentMethodConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
