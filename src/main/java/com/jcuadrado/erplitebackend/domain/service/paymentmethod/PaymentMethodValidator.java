package com.jcuadrado.erplitebackend.domain.service.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.InvalidPaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.InvalidPaymentMethodDataException;

/**
 * Validator for Payment Method domain rules
 * Contains validation logic for payment method fields
 */
public class PaymentMethodValidator {

    private static final int CODE_MAX_LENGTH = 30;
    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 100;
    private static final String CODE_PATTERN = "^[A-Z0-9_]+$";

    /**
     * Validate payment method code (BR-PM-001)
     * Rules: 
     * - Cannot be empty
     * - Max 30 characters
     * - Only uppercase letters, numbers and underscores
     */
    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidPaymentMethodCodeException("Payment method code cannot be empty");
        }

        String trimmedCode = code.trim().toUpperCase();

        if (trimmedCode.length() > CODE_MAX_LENGTH) {
            throw new InvalidPaymentMethodCodeException(
                String.format("Payment method code cannot exceed %d characters", CODE_MAX_LENGTH)
            );
        }

        if (!trimmedCode.matches(CODE_PATTERN)) {
            throw new InvalidPaymentMethodCodeException(
                "Payment method code must contain only uppercase letters, numbers, and underscores"
            );
        }
    }

    /**
     * Validate payment method name
     * Rules:
     * - Required
     * - Max 100 characters
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidPaymentMethodDataException("name", "Payment method name cannot be empty");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < NAME_MIN_LENGTH) {
            throw new InvalidPaymentMethodDataException("name", "Payment method name cannot be empty");
        }

        if (trimmedName.length() > NAME_MAX_LENGTH) {
            throw new InvalidPaymentMethodDataException("name",
                String.format("Payment method name cannot exceed %d characters", NAME_MAX_LENGTH)
            );
        }
    }

    /**
     * Validate all fields of a payment method
     */
    public void validateAll(String code, String name) {
        validateCode(code);
        validateName(name);
    }
}
