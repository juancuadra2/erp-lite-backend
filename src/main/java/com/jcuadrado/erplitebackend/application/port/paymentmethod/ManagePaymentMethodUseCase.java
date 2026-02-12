package com.jcuadrado.erplitebackend.application.port.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;

import java.util.UUID;

/**
 * Use case for managing payment methods (Commands: create, update, delete, activate/deactivate)
 * CQRS - Command side
 */
public interface ManagePaymentMethodUseCase {

    /**
     * Create a new payment method
     */
    PaymentMethod create(PaymentMethod paymentMethod);

    /**
     * Update an existing payment method
     */
    PaymentMethod update(UUID uuid, PaymentMethod paymentMethod);

    /**
     * Delete a payment method (soft delete)
     */
    void delete(UUID uuid);

    /**
     * Activate a payment method
     */
    void activate(UUID uuid);

    /**
     * Deactivate a payment method
     */
    void deactivate(UUID uuid);
}
