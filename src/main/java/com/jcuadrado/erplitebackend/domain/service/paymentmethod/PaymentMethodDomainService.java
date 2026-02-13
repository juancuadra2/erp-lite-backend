package com.jcuadrado.erplitebackend.domain.service.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.DuplicatePaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class PaymentMethodDomainService {

    private final PaymentMethodRepository repository;
    private final PaymentMethodValidator validator;

    public String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.toUpperCase().trim();
    }

    public void validateUniqueCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (repository.existsByCode(normalizedCode)) {
            log.warn("Duplicate code attempt: {}", normalizedCode);
            throw new DuplicatePaymentMethodCodeException(normalizedCode);
        }
    }

    public void validateUniqueCodeExcluding(String code, UUID excludeUuid) {
        String normalizedCode = normalizeCode(code);
        if (repository.existsByCodeAndUuidNot(normalizedCode, excludeUuid)) {
            log.warn("Duplicate code attempt for update: {}", normalizedCode);
            throw new DuplicatePaymentMethodCodeException(normalizedCode);
        }
    }

    public boolean canDeactivate(PaymentMethod paymentMethod, long transactionsCount) {
        return transactionsCount == 0;
    }

    public boolean canDelete(PaymentMethod paymentMethod, long transactionsCount) {
        return transactionsCount == 0;
    }

    public void prepareForCreation(PaymentMethod paymentMethod) {
        validator.validateAll(
            paymentMethod.getCode(),
            paymentMethod.getName()
        );

        paymentMethod.setCode(normalizeCode(paymentMethod.getCode()));
        validateUniqueCode(paymentMethod.getCode());

        if (paymentMethod.getUuid() == null) {
            paymentMethod.setUuid(UUID.randomUUID());
        }
        if (paymentMethod.getEnabled() == null) {
            paymentMethod.setEnabled(true);
        }
    }

    public void prepareForUpdate(PaymentMethod paymentMethod, UUID existingUuid) {
        validator.validateAll(
            paymentMethod.getCode(),
            paymentMethod.getName()
        );

        paymentMethod.setCode(normalizeCode(paymentMethod.getCode()));
        validateUniqueCodeExcluding(paymentMethod.getCode(), existingUuid);
    }
}
