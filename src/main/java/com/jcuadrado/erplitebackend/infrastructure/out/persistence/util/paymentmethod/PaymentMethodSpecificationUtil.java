package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating JPA Specifications for PaymentMethod queries
 */
public class PaymentMethodSpecificationUtil {

    private PaymentMethodSpecificationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Build specification from filter map
     * @param filters Map with filter criteria
     * @return JPA Specification
     */
    public static Specification<PaymentMethodEntity> buildSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters == null || filters.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Filter by enabled status
            if (filters.containsKey("enabled")) {
                Boolean enabled = (Boolean) filters.get("enabled");
                if (enabled != null) {
                    predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
                }
            }

            // Global search in code and name
            if (filters.containsKey("search")) {
                String search = (String) filters.get("search");
                if (search != null && !search.trim().isEmpty()) {
                    String searchPattern = "%" + search.trim().toLowerCase() + "%";
                    Predicate codePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")), searchPattern);
                    Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);

                    predicates.add(criteriaBuilder.or(codePredicate, namePredicate));
                }
            }

            // Filter by code (exact match)
            if (filters.containsKey("code")) {
                String code = (String) filters.get("code");
                if (code != null && !code.trim().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("code"), code.toUpperCase()));
                }
            }

            // Filter by name (contains)
            if (filters.containsKey("name")) {
                String name = (String) filters.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
