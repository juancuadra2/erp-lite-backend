package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.AuditLogEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogSpecificationUtilTest {

    @Mock
    private Root<AuditLogEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @Test
    @DisplayName("buildSpecification should return a non-null specification even with empty filter")
    void buildSpecification_shouldReturnSpec_whenFilterIsEmpty() {
        AuditLogFilter filter = new AuditLogFilter(null, null, null, null, null);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Specification<AuditLogEntity> spec = AuditLogSpecificationUtil.buildSpecification(filter);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("buildSpecification should add userId predicate when userId is provided")
    void buildSpecification_shouldAddUserIdPredicate_whenUserIdProvided() {
        UUID userId = UUID.randomUUID();
        AuditLogFilter filter = new AuditLogFilter(userId, null, null, null, null);

        when(root.get("userId")).thenReturn(path);
        when(cb.equal(path, userId)).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Specification<AuditLogEntity> spec = AuditLogSpecificationUtil.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, userId);
    }

    @Test
    @DisplayName("buildSpecification should add entity predicate when entity is provided")
    void buildSpecification_shouldAddEntityPredicate_whenEntityProvided() {
        AuditLogFilter filter = new AuditLogFilter(null, "User", null, null, null);

        when(root.get("entity")).thenReturn(path);
        when(cb.equal(path, "User")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Specification<AuditLogEntity> spec = AuditLogSpecificationUtil.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, "User");
    }

    @Test
    @DisplayName("buildSpecification should add action predicate when action is provided")
    void buildSpecification_shouldAddActionPredicate_whenActionProvided() {
        AuditLogFilter filter = new AuditLogFilter(null, null, AuditAction.LOGIN, null, null);

        when(root.get("action")).thenReturn(path);
        when(cb.equal(path, AuditAction.LOGIN.name())).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Specification<AuditLogEntity> spec = AuditLogSpecificationUtil.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, AuditAction.LOGIN.name());
    }

    @Test
    @DisplayName("buildSpecification should add startDate and endDate predicates when dates are provided")
    void buildSpecification_shouldAddDatePredicates_whenDatesProvided() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        AuditLogFilter filter = new AuditLogFilter(null, null, null, startDate, endDate);

        when(root.<LocalDateTime>get("timestamp")).thenReturn((Path) path);
        when(cb.greaterThanOrEqualTo(any(), (LocalDateTime) any())).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(), (LocalDateTime) any())).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Specification<AuditLogEntity> spec = AuditLogSpecificationUtil.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(cb).greaterThanOrEqualTo(any(), (LocalDateTime) any());
        verify(cb).lessThanOrEqualTo(any(), (LocalDateTime) any());
    }

    @Test
    @DisplayName("buildSpecification should ignore blank entity filter")
    void buildSpecification_shouldIgnoreBlankEntity() {
        AuditLogFilter filter = new AuditLogFilter(null, "   ", null, null, null);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Specification<AuditLogEntity> spec = AuditLogSpecificationUtil.buildSpecification(filter);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
    }
}
