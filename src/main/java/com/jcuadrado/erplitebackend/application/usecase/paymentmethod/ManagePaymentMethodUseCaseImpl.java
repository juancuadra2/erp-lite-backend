package com.jcuadrado.erplitebackend.application.usecase.paymentmethod;

import com.jcuadrado.erplitebackend.application.port.paymentmethod.ManagePaymentMethodUseCase;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of ManagePaymentMethodUseCase
 * Orchestrates domain services and repository for command operations
 * CQRS - Command side with write transactions
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ManagePaymentMethodUseCaseImpl implements ManagePaymentMethodUseCase {

    private final PaymentMethodRepository repository;
    private final PaymentMethodDomainService domainService;

    @Override
    public PaymentMethod create(PaymentMethod paymentMethod) {
        log.debug("Creating payment method with code: {}", paymentMethod.getCode());

        // Prepare and validate for creation
        domainService.prepareForCreation(paymentMethod);

        // Set audit fields
        paymentMethod.setCreatedAt(LocalDateTime.now());
        // TODO: Set createdBy from security context when auth is implemented

        // Save and return
        PaymentMethod saved = repository.save(paymentMethod);

        log.info("Payment method created successfully with ID: {} and UUID: {}", saved.getId(), saved.getUuid());
        return saved;
    }

    @Override
    public PaymentMethod update(UUID uuid, PaymentMethod paymentMethod) {
        log.debug("Updating payment method with UUID: {}", uuid);

        // Find existing payment method
        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        // Update fields
        existing.setCode(paymentMethod.getCode());
        existing.setName(paymentMethod.getName());
        if (paymentMethod.getEnabled() != null) {
            existing.setEnabled(paymentMethod.getEnabled());
        }

        // Prepare and validate for update
        domainService.prepareForUpdate(existing, uuid);

        // Set audit fields
        existing.setUpdatedAt(LocalDateTime.now());
        // TODO: Set updatedBy from security context when auth is implemented

        // Save and return
        PaymentMethod updated = repository.save(existing);

        log.info("Payment method updated successfully with UUID: {}", uuid);
        return updated;
    }

    @Override
    public void delete(UUID uuid) {
        log.debug("Deleting payment method with UUID: {}", uuid);

        // Find existing payment method
        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        // Check if can be deleted (BR-PM-003: cannot delete if has transactions)
        long transactionsCount = repository.countTransactionsWithPaymentMethod(uuid);
        if (!domainService.canDelete(existing, transactionsCount)) {
            throw new PaymentMethodConstraintException(
                "Cannot delete payment method with associated transactions. Found " + transactionsCount + " transactions."
            );
        }

        // Soft delete
        existing.deactivate(null); // TODO: Get userId from security context
        repository.save(existing);

        log.info("Payment method soft-deleted successfully with UUID: {}", uuid);
    }

    @Override
    public void activate(UUID uuid) {
        log.debug("Activating payment method with UUID: {}", uuid);

        // Find existing payment method
        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        // Activate
        existing.activate();

        // Set audit fields
        existing.setUpdatedAt(LocalDateTime.now());
        // TODO: Set updatedBy from security context when auth is implemented

        // Save
        repository.save(existing);

        log.info("Payment method activated successfully with UUID: {}", uuid);
    }

    @Override
    public void deactivate(UUID uuid) {
        log.debug("Deactivating payment method with UUID: {}", uuid);

        // Find existing payment method
        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        // Check if can be deactivated (always allowed for payment methods)
        if (!domainService.canDeactivate(existing)) {
            throw new PaymentMethodConstraintException("Cannot deactivate payment method");
        }

        // Deactivate
        existing.deactivate(null); // TODO: Get userId from security context

        // Set audit fields
        existing.setUpdatedAt(LocalDateTime.now());
        // TODO: Set updatedBy from security context when auth is implemented

        // Save
        repository.save(existing);

        log.info("Payment method deactivated successfully with UUID: {}", uuid);
    }
}
