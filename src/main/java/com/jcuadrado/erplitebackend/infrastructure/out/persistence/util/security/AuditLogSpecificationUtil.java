package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.AuditLogEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecificationUtil {

    private AuditLogSpecificationUtil() {
    }

    public static Specification<AuditLogEntity> buildSpecification(AuditLogFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.userId()));
            }
            if (filter.entity() != null && !filter.entity().isBlank()) {
                predicates.add(cb.equal(root.get("entity"), filter.entity()));
            }
            if (filter.action() != null) {
                predicates.add(cb.equal(root.get("action"), filter.action().name()));
            }
            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.startDate()));
            }
            if (filter.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.endDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
