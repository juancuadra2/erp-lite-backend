package com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.specification;

import com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.entity.DocumentTypeEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JPA Specifications for DocumentType dynamic queries.
 */
public class DocumentTypeSpecification {
    
    /**
     * Create specification with filters.
     * 
     * @param enabled filter by active status
     * @param search global search in code, name, description
     * @param filters additional dynamic filters
     * @return specification
     */
    public static Specification<DocumentTypeEntity> withFilters(Boolean enabled, String search, Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by active status
            if (enabled != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), enabled));
            }
            
            // Global search in code, name, description
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate codePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("code")), searchPattern);
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern);
                
                predicates.add(criteriaBuilder.or(codePredicate, namePredicate, descriptionPredicate));
            }
            
            // Dynamic filters
            if (filters != null && !filters.isEmpty()) {
                filters.forEach((key, value) -> {
                    if (value != null) {
                        try {
                            predicates.add(criteriaBuilder.equal(root.get(key), value));
                        } catch (IllegalArgumentException e) {
                            // Ignore invalid field names
                        }
                    }
                });
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
