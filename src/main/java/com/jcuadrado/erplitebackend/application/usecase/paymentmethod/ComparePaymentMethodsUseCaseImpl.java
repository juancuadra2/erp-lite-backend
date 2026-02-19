package com.jcuadrado.erplitebackend.application.usecase.paymentmethod;

import com.jcuadrado.erplitebackend.application.port.paymentmethod.ComparePaymentMethodsUseCase;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ComparePaymentMethodsUseCaseImpl implements ComparePaymentMethodsUseCase {

    private final PaymentMethodRepository repository;

    @Override
    public PaymentMethod getByUuid(UUID uuid) {
        log.debug("Finding payment method by UUID: {}", uuid);
        return repository.findByUuid(uuid)
            .orElseThrow(() -> new PaymentMethodNotFoundException("uuid", uuid.toString()));
    }

    @Override
    public PaymentMethod getByCode(String code) {
        log.debug("Finding payment method by code: {}", code);
        return repository.findByCode(code)
            .orElseThrow(() -> new PaymentMethodNotFoundException("code", code));
    }

    @Override
    public List<PaymentMethod> getAllActive() {
        log.debug("Finding all active payment methods");
        return repository.findAllActive();
    }

    @Override
    public Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable) {
        log.debug("Finding all payment methods with filters: {} and pageable: {}", filters, pageable);
        return repository.findAll(filters, pageable);
    }

    @Override
    public List<PaymentMethod> searchByName(String name) {
        log.debug("Searching payment methods by name containing: {}", name);
        return repository.findByNameContaining(name);
    }
}
