package com.jcuadrado.erplitebackend.domain.port.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Payment Method persistence operations.
 * This interface is part of the domain layer and defines the contract
 * that persistence adapters must implement.
 */
public interface PaymentMethodRepository {

    /**
     * Save a payment method (create or update)
     */
    PaymentMethod save(PaymentMethod paymentMethod);

    /**
     * Find payment method by ID
     */
    Optional<PaymentMethod> findById(Long id);

    /**
     * Find payment method by UUID
     */
    Optional<PaymentMethod> findByUuid(UUID uuid);

    /**
     * Find payment method by code
     */
    Optional<PaymentMethod> findByCode(String code);

    /**
     * Find all payment methods with pagination and filters
     * @param filters Map with filter criteria (enabled, search, etc.)
     * @param pageable Pagination and sorting information
     */
    Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable);

    /**
     * Find all active payment methods
     */
    List<PaymentMethod> findAllActive();

    /**
     * Find all enabled payment methods
     */
    List<PaymentMethod> findByEnabled(Boolean enabled);

    /**
     * Find payment methods by name containing (case-insensitive)
     */
    List<PaymentMethod> findByNameContaining(String name);

    /**
     * Delete a payment method by UUID
     */
    void delete(UUID uuid);

    /**
     * Check if payment method code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if payment method code exists excluding specific UUID
     */
    boolean existsByCodeAndUuidNot(String code, UUID excludeUuid);

    /**
     * Count transactions associated with payment method
     * Used to validate if payment method can be deleted
     */
    long countTransactionsWithPaymentMethod(UUID paymentMethodUuid);
}
