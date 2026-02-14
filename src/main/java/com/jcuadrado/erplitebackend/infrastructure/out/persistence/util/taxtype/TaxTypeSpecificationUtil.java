package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TaxTypeSpecificationUtil - Utility for building dynamic JPA queries
 * 
 * Constructs Specifications for filtering TaxType queries based on dynamic filters.
 * Handles special logic for TaxApplicationType filtering (BOTH appears in all queries).
 */
public class TaxTypeSpecificationUtil {

    /**
     * Builds a Specification from a map of filters
     * 
     * Supported filters:
     * - enabled (Boolean): Filter by enabled status
     * - applicationType (TaxApplicationType): Filter by application type (includes BOTH)
     * - name (String): Case-insensitive partial match on name
     * 
     * @param filters Map of filter parameters
     * @return Specification for querying TaxTypeEntity
     */
    public static Specification<TaxTypeEntity> buildSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters == null || filters.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            if (filters.containsKey("enabled")) {
                Boolean enabled = (Boolean) filters.get("enabled");
                if (enabled != null) {
                    predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
                }
            }

            if (filters.containsKey("applicationType")) {
                TaxApplicationType applicationType = (TaxApplicationType) filters.get("applicationType");
                if (applicationType != null) {
                    predicates.add(
                        criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("applicationType"), applicationType),
                            criteriaBuilder.equal(root.get("applicationType"), TaxApplicationType.BOTH)
                        )
                    );
                }
            }

            if (filters.containsKey("name")) {
                String name = (String) filters.get("name");
                if (name != null && !name.isBlank()) {
                    predicates.add(
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            "%" + name.toLowerCase() + "%"
                        )
                    );
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
