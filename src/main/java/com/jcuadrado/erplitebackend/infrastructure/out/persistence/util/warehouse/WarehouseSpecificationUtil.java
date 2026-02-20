package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.warehouse.WarehouseEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarehouseSpecificationUtil {

    private WarehouseSpecificationUtil() {
    }

    public static Specification<WarehouseEntity> buildSpecification(Map<String, Object> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre excluir soft-deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filters == null || filters.isEmpty()) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filters.containsKey("active")) {
                Boolean active = (Boolean) filters.get("active");
                if (active != null) {
                    predicates.add(cb.equal(root.get("active"), active));
                }
            }

            if (filters.containsKey("type")) {
                Object typeValue = filters.get("type");
                if (typeValue != null) {
                    WarehouseType type = typeValue instanceof WarehouseType wt
                            ? wt
                            : WarehouseType.valueOf(typeValue.toString());
                    predicates.add(cb.equal(root.get("type"), type));
                }
            }

            if (filters.containsKey("municipalityId")) {
                Object municipalityValue = filters.get("municipalityId");
                if (municipalityValue != null) {
                    predicates.add(cb.equal(root.get("municipalityUuid"), municipalityValue.toString()));
                }
            }

            if (filters.containsKey("name")) {
                String name = (String) filters.get("name");
                if (name != null && !name.isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                }
            }

            if (filters.containsKey("code")) {
                String code = (String) filters.get("code");
                if (code != null && !code.isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
