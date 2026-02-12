package com.jcuadrado.erplitebackend.domain.service.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.DuplicatePaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * PaymentMethodDomainService - Domain service (POJO)
 * 
 * Contains business rules that don't belong to a specific entity.
 * Registered as Bean in BeanConfiguration.
 */
@RequiredArgsConstructor
public class PaymentMethodDomainService {

    private final PaymentMethodRepository repository;
    private final PaymentMethodValidator validator;

    /**
     * Normalize code to uppercase and trim
     */
    public String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.toUpperCase().trim();
    }

    /**
     * Validate that code is unique in the system (BR-PM-001)
     * @throws DuplicatePaymentMethodCodeException if code already exists
     */
    public void validateUniqueCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (repository.existsByCode(normalizedCode)) {
            throw new DuplicatePaymentMethodCodeException(normalizedCode);
        }
    }

    /**
     * Validate that code is unique excluding a specific UUID (for updates)
     * @throws DuplicatePaymentMethodCodeException if code already exists for different payment method
     */
    public void validateUniqueCodeExcluding(String code, UUID excludeUuid) {
        String normalizedCode = normalizeCode(code);
        if (repository.existsByCodeAndUuidNot(normalizedCode, excludeUuid)) {
            throw new DuplicatePaymentMethodCodeException(normalizedCode);
        }
    }

    /**
     * Check if payment method can be deactivated
     * Business rule: Can always deactivate
     */
    public boolean canDeactivate(PaymentMethod paymentMethod) {
        // Payment methods can always be deactivated (soft delete)
        return true;
    }

    /**
     * Check if payment method can be deleted (BR-PM-003)
     * Business rule: Cannot delete if there are associated transactions
     */
    public boolean canDelete(PaymentMethod paymentMethod, long transactionsCount) {
        return transactionsCount == 0;
    }

    /**
     * Validate and prepare payment method for creation
     */
    public void prepareForCreation(PaymentMethod paymentMethod) {
        // Validate fields
        validator.validateAll(
            paymentMethod.getCode(),
            paymentMethod.getName()
        );

        // Normalize code
        paymentMethod.setCode(normalizeCode(paymentMethod.getCode()));

        // Validate uniqueness
        validateUniqueCode(paymentMethod.getCode());

        // Set defaults
        if (paymentMethod.getUuid() == null) {
            paymentMethod.setUuid(UUID.randomUUID());
        }
        if (paymentMethod.getEnabled() == null) {
            paymentMethod.setEnabled(true);
        }
    }

    /**
     * Validate and prepare payment method for update
     */
    public void prepareForUpdate(PaymentMethod paymentMethod, UUID existingUuid) {
        // Validate fields
        validator.validateAll(
            paymentMethod.getCode(),
            paymentMethod.getName()
        );

        // Normalize code
        paymentMethod.setCode(normalizeCode(paymentMethod.getCode()));

        // Validate uniqueness excluding current payment method
        validateUniqueCodeExcluding(paymentMethod.getCode(), existingUuid);
    }
}
