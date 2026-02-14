package com.jcuadrado.erplitebackend.application.port.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Use case for querying payment methods (Queries: get, list, search, compare)
 * CQRS - Query side
 */
public interface ComparePaymentMethodsUseCase {

    /**
     * Get payment method by UUID
     */
    PaymentMethod getByUuid(UUID uuid);

    /**
     * Get payment method by code
     */
    PaymentMethod getByCode(String code);

    /**
     * Get all active payment methods
     */
    List<PaymentMethod> getAllActive();

    /**
     * Find all payment methods with filters and pagination
     * @param filters Map with filter criteria (enabled, search, etc.)
     * @param pageable Pagination and sorting information
     */
    Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable);

    /**
     * Search payment methods by name containing
     */
    List<PaymentMethod> searchByName(String name);
}
