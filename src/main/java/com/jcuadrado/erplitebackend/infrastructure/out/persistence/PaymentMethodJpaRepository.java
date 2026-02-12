package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for PaymentMethod persistence operations
 * Note: This is NOT in a feature folder as it's a JPA interface
 */
@Repository
public interface PaymentMethodJpaRepository extends
    JpaRepository<PaymentMethodEntity, Long>,
    JpaSpecificationExecutor<PaymentMethodEntity> {

    /**
     * Find payment method by UUID
     */
    Optional<PaymentMethodEntity> findByUuid(UUID uuid);

    /**
     * Find payment method by code
     */
    Optional<PaymentMethodEntity> findByCode(String code);

    /**
     * Check if a code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if a code exists excluding a specific UUID
     */
    boolean existsByCodeAndUuidNot(String code, UUID uuid);

    /**
     * Find all enabled payment methods
     */
    List<PaymentMethodEntity> findByEnabledTrue();

    /**
     * Find payment methods by name containing (case-insensitive)
     */
    List<PaymentMethodEntity> findByNameContainingIgnoreCase(String name);

    /**
     * Count transactions (sales + purchases) associated with payment method
     * This is a placeholder query - will need to be updated when sales/purchases tables exist
     */
    @Query("""
        SELECT 0
        FROM PaymentMethodEntity pm
        WHERE pm.uuid = :paymentMethodUuid
    """)
    long countTransactionsWithPaymentMethod(@Param("paymentMethodUuid") UUID paymentMethodUuid);
}
