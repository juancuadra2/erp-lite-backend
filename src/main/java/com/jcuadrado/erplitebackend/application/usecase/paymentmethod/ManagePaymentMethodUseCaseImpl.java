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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ManagePaymentMethodUseCaseImpl implements ManagePaymentMethodUseCase {

    private static final Long SYSTEM_USER_ID = 0L;

    private final PaymentMethodRepository repository;
    private final PaymentMethodDomainService domainService;

    @Override
    public PaymentMethod create(PaymentMethod paymentMethod) {
        log.debug("Creating payment method with code: {}", paymentMethod.getCode());

        domainService.prepareForCreation(paymentMethod);

        paymentMethod.setCreatedAt(LocalDateTime.now());
        paymentMethod.setCreatedBy(SYSTEM_USER_ID);

        PaymentMethod saved = repository.save(paymentMethod);

        log.info("Payment method created successfully with ID: {} and UUID: {}", saved.getId(), saved.getUuid());
        return saved;
    }

    @Override
    public PaymentMethod update(UUID uuid, PaymentMethod paymentMethod) {
        log.debug("Updating payment method with UUID: {}", uuid);

        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        existing.setCode(paymentMethod.getCode());
        existing.setName(paymentMethod.getName());
        if (paymentMethod.getEnabled() != null) {
            existing.setEnabled(paymentMethod.getEnabled());
        }

        domainService.prepareForUpdate(existing, uuid);

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_USER_ID);

        PaymentMethod updated = repository.save(existing);

        log.info("Payment method updated successfully with UUID: {}", uuid);
        return updated;
    }

    @Override
    public void delete(UUID uuid) {
        log.debug("Deleting payment method with UUID: {}", uuid);

        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        long transactionsCount = repository.countTransactionsWithPaymentMethod(uuid);
        if (!domainService.canDelete(transactionsCount)) {
            throw new PaymentMethodConstraintException(
                "Cannot delete payment method with associated transactions. Found " + transactionsCount + " transactions."
            );
        }

        existing.deactivate(SYSTEM_USER_ID);
        existing.setUpdatedBy(SYSTEM_USER_ID);
        existing.setUpdatedAt(LocalDateTime.now());
        repository.save(existing);

        log.info("Payment method soft-deleted successfully with UUID: {}", uuid);
    }

    @Override
    public PaymentMethod activate(UUID uuid) {
        log.debug("Activating payment method with UUID: {}", uuid);

        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        existing.activate();
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_USER_ID);

        PaymentMethod activated = repository.save(existing);

        log.info("Payment method activated successfully with UUID: {}", uuid);
        return activated;
    }

    @Override
    public PaymentMethod deactivate(UUID uuid) {
        log.debug("Deactivating payment method with UUID: {}", uuid);

        PaymentMethod existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));

        long transactionsCount = repository.countTransactionsWithPaymentMethod(uuid);
        if (!domainService.canDeactivate(transactionsCount)) {
            throw new PaymentMethodConstraintException(
                "Cannot deactivate payment method with associated transactions. Found " + transactionsCount + " transactions."
            );
        }

        existing.deactivate(SYSTEM_USER_ID);
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_USER_ID);

        PaymentMethod deactivated = repository.save(existing);

        log.info("Payment method deactivated successfully with UUID: {}", uuid);
        return deactivated;
    }
}
