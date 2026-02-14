package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.unitofmeasure;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure.UnitOfMeasureEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnitOfMeasureSpecificationUtil {

    private UnitOfMeasureSpecificationUtil() {
    }

    public static Specification<UnitOfMeasureEntity> buildSpecification(Map<String, Object> filters) {
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

            if (filters.containsKey("abbreviation")) {
                String abbreviation = (String) filters.get("abbreviation");
                if (abbreviation != null && !abbreviation.isBlank()) {
                    predicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("abbreviation")),
                                    "%" + abbreviation.toLowerCase() + "%"
                            )
                    );
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
