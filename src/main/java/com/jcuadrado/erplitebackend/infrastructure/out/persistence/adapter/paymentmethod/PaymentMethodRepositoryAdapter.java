package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.PaymentMethodJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.paymentmethod.PaymentMethodEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.paymentmethod.PaymentMethodSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing PaymentMethodRepository using JPA
 */
@Component
@RequiredArgsConstructor
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private final PaymentMethodJpaRepository jpaRepository;
    private final PaymentMethodEntityMapper mapper;

    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) {
        PaymentMethodEntity entity = mapper.toEntity(paymentMethod);
        PaymentMethodEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PaymentMethod> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<PaymentMethod> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<PaymentMethod> findByCode(String code) {
        return jpaRepository.findByCode(code)
            .map(mapper::toDomain);
    }

    @Override
    public Page<PaymentMethod> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public List<PaymentMethod> findAllActive() {
        return jpaRepository.findByEnabledTrue().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMethod> findByEnabled(Boolean enabled) {
        if (Boolean.TRUE.equals(enabled)) {
            return findAllActive();
        }
        
        // For false or null, find all and filter
        return jpaRepository.findAll().stream()
            .filter(entity -> enabled == null || entity.getEnabled().equals(enabled))
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<PaymentMethod> findByNameContaining(String name) {
        return jpaRepository.findByNameContainingIgnoreCase(name).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID uuid) {
        jpaRepository.findByUuid(uuid)
            .ifPresent(jpaRepository::delete);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndUuidNot(String code, UUID excludeUuid) {
        return jpaRepository.existsByCodeAndUuidNot(code, excludeUuid);
    }

    @Override
    public long countTransactionsWithPaymentMethod(UUID paymentMethodUuid) {
        return jpaRepository.countTransactionsWithPaymentMethod(paymentMethodUuid);
    }
}
