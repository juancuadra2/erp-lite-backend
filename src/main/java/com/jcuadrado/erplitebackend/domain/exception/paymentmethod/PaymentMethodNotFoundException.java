package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

/**
 * Exception thrown when a payment method is not found
 */
public class PaymentMethodNotFoundException extends RuntimeException {
    
    public PaymentMethodNotFoundException(String message) {
        super(message);
    }
    
    public PaymentMethodNotFoundException(String field, String value) {
        super(String.format("Payment method not found with %s: %s", field, value));
    }
}
