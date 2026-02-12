package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.documenttypes;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.documenttypes.DocumentTypeEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating JPA Specifications for DocumentType queries
 */
public class DocumentTypeSpecificationUtil {

    private DocumentTypeSpecificationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Build specification from filter map
     * @param filters Map with filter criteria
     * @return JPA Specification
     */
    public static Specification<DocumentTypeEntity> buildSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters == null || filters.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Filter by active status
            if (filters.containsKey("enabled")) {
                Boolean enabled = (Boolean) filters.get("enabled");
                if (enabled != null) {
                    predicates.add(criteriaBuilder.equal(root.get("active"), enabled));
                }
            }

            // Global search in code, name, description
            if (filters.containsKey("search")) {
                String search = (String) filters.get("search");
                if (search != null && !search.trim().isEmpty()) {
                    String searchPattern = "%" + search.trim().toLowerCase() + "%";
                    Predicate codePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")), searchPattern);
                    Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                    Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);

                    predicates.add(criteriaBuilder.or(codePredicate, namePredicate, descPredicate));
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

