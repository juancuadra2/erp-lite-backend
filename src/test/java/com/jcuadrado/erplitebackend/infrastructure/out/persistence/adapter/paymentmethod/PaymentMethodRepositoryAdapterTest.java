package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.paymentmethod.PaymentMethodEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.PaymentMethodJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PaymentMethodRepositoryAdapter
 */
@ExtendWith(MockitoExtension.class)
class PaymentMethodRepositoryAdapterTest {

    @Mock
    private PaymentMethodJpaRepository jpaRepository;

    @Mock
    private PaymentMethodEntityMapper mapper;

    @InjectMocks
    private PaymentMethodRepositoryAdapter adapter;

    private PaymentMethod domainObject;
    private PaymentMethodEntity entity;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        
        domainObject = PaymentMethod.builder()
                .id(1L)
                .uuid(testUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();
        
        entity = PaymentMethodEntity.builder()
                .id(1L)
                .uuid(testUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();
    }

    // ==================== save Tests ====================

    @Test
    void save_shouldSaveAndReturnDomainObject() {
        // Given
        when(mapper.toEntity(domainObject)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        PaymentMethod result = adapter.save(domainObject);

        // Then
        assertThat(result).isNotNull().isEqualTo(domainObject);
        verify(mapper).toEntity(domainObject);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    // ==================== findByUuid Tests ====================

    @Test
    void findByUuid_whenExists_shouldReturnOptionalWithDomainObject() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Optional<PaymentMethod> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findByUuid(testUuid);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByUuid_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // When
        Optional<PaymentMethod> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByUuid(testUuid);
    }

    // ==================== findByCode Tests ====================

    @Test
    void findByCode_whenExists_shouldReturnOptionalWithDomainObject() {
        // Given
        when(jpaRepository.findByCode("CASH")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Optional<PaymentMethod> result = adapter.findByCode("CASH");

        // Then
        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findByCode("CASH");
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByCode_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByCode("CASH")).thenReturn(Optional.empty());

        // When
        Optional<PaymentMethod> result = adapter.findByCode("CASH");

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByCode("CASH");
    }

    // ==================== findAll (with filters) Tests ====================

    @Test
    void findAll_shouldReturnPagedDomainObjects() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);

        List<PaymentMethodEntity> entities = Arrays.asList(entity);
        Page<PaymentMethodEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(jpaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Page<PaymentMethod> result = adapter.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(domainObject);
        verify(jpaRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenNoResults() {
        // Given
        Map<String, Object> filters = Collections.emptyMap();
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentMethodEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(jpaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        // When
        Page<PaymentMethod> result = adapter.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ==================== findAllActive Tests ====================

    @Test
    void findAllActive_shouldReturnActiveDomainObjects() {
        // Given
        List<PaymentMethodEntity> entities = Arrays.asList(entity);
        when(jpaRepository.findByEnabledTrue()).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        List<PaymentMethod> result = adapter.findAllActive();

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isEqualTo(domainObject);
        verify(jpaRepository).findByEnabledTrue();
    }

    @Test
    void findAllActive_shouldReturnEmptyListWhenNoActive() {
        // Given
        when(jpaRepository.findByEnabledTrue()).thenReturn(Collections.emptyList());

        // When
        List<PaymentMethod> result = adapter.findAllActive();

        // Then
        assertThat(result).isNotNull().isEmpty();
        verify(jpaRepository).findByEnabledTrue();
    }

    // ==================== findByNameContaining Tests ====================

    @Test
    void findByNameContaining_shouldReturnMatchingDomainObjects() {
        // Given
        String searchTerm = "Efect";
        List<PaymentMethodEntity> entities = Arrays.asList(entity);
        when(jpaRepository.findByNameContainingIgnoreCase(searchTerm)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        List<PaymentMethod> result = adapter.findByNameContaining(searchTerm);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0)).isEqualTo(domainObject);
        verify(jpaRepository).findByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    void findByNameContaining_shouldReturnEmptyListWhenNoMatches() {
        // Given
        String searchTerm = "NonExistent";
        when(jpaRepository.findByNameContainingIgnoreCase(searchTerm)).thenReturn(Collections.emptyList());

        // When
        List<PaymentMethod> result = adapter.findByNameContaining(searchTerm);

        // Then
        assertThat(result).isNotNull().isEmpty();
        verify(jpaRepository).findByNameContainingIgnoreCase(searchTerm);
    }

    // ==================== existsByCode Tests ====================

    @Test
    void existsByCode_shouldReturnTrueWhenExists() {
        // Given
        when(jpaRepository.existsByCode("CASH")).thenReturn(true);

        // When
        boolean result = adapter.existsByCode("CASH");

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCode("CASH");
    }

    @Test
    void existsByCode_shouldReturnFalseWhenNotExists() {
        // Given
        when(jpaRepository.existsByCode("INVALID")).thenReturn(false);

        // When
        boolean result = adapter.existsByCode("INVALID");

        // Then
        assertThat(result).isFalse();
        verify(jpaRepository).existsByCode("INVALID");
    }

    // ==================== existsByCodeAndUuidNot Tests ====================

    @Test
    void existsByCodeAndUuidNot_shouldReturnTrueWhenExistsWithDifferentUuid() {
        // Given
        UUID otherUuid = UUID.randomUUID();
        when(jpaRepository.existsByCodeAndUuidNot("CASH", otherUuid)).thenReturn(true);

        // When
        boolean result = adapter.existsByCodeAndUuidNot("CASH", otherUuid);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCodeAndUuidNot("CASH", otherUuid);
    }

    @Test
    void existsByCodeAndUuidNot_shouldReturnFalseWhenNotExists() {
        // Given
        when(jpaRepository.existsByCodeAndUuidNot("CASH", testUuid)).thenReturn(false);

        // When
        boolean result = adapter.existsByCodeAndUuidNot("CASH", testUuid);

        // Then
        assertThat(result).isFalse();
        verify(jpaRepository).existsByCodeAndUuidNot("CASH", testUuid);
    }

    // ==================== countTransactionsWithPaymentMethod Tests ====================

    @Test
    void countTransactionsWithPaymentMethod_shouldReturnCount() {
        // Given
        when(jpaRepository.countTransactionsWithPaymentMethod(testUuid)).thenReturn(5L);

        // When
        long result = adapter.countTransactionsWithPaymentMethod(testUuid);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(jpaRepository).countTransactionsWithPaymentMethod(testUuid);
    }

    @Test
    void countTransactionsWithPaymentMethod_shouldReturnZeroWhenNoTransactions() {
        // Given
        when(jpaRepository.countTransactionsWithPaymentMethod(testUuid)).thenReturn(0L);

        // When
        long result = adapter.countTransactionsWithPaymentMethod(testUuid);

        // Then
        assertThat(result).isZero();
        verify(jpaRepository).countTransactionsWithPaymentMethod(testUuid);
    }
}
