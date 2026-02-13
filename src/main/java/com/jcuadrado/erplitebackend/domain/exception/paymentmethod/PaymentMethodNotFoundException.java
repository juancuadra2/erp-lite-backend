package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

public class PaymentMethodNotFoundException extends RuntimeException {
    
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
    
    public PaymentMethodNotFoundException(String field, String value) {
        super(String.format("Payment method not found with %s: %s", field, value));
    }
}
