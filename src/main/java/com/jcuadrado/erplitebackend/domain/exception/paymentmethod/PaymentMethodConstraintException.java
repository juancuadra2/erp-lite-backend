package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

/**
 * Exception thrown when a payment method operation violates a business constraint
 * (e.g., trying to delete a payment method with associated transactions)
 */
public class PaymentMethodConstraintException extends RuntimeException {
    
    public PaymentMethodConstraintException(String message) {
        super(message);
    }
    
    public PaymentMethodConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
