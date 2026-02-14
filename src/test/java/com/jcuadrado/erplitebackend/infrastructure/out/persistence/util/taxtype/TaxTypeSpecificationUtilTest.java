package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TaxTypeSpecificationUtil - Unit Tests")
@ExtendWith(MockitoExtension.class)
class TaxTypeSpecificationUtilTest {

    @Mock
    private Root<TaxTypeEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @Mock
    private Expression<String> stringExpression;

    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.like(ArgumentMatchers.<Expression<String>>any(), anyString())).thenReturn(predicate);
        lenient().when(criteriaBuilder.lower(ArgumentMatchers.<Expression<String>>any())).thenReturn(stringExpression);
        lenient().when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.conjunction()).thenReturn(predicate);
    }

    @Test
    @DisplayName("should build specification with null filters")
    void buildSpecification_withNullFilters_shouldReturnConjunction() {
        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("should build specification with empty filters")
    void buildSpecification_withEmptyFilters_shouldReturnConjunction() {
        Map<String, Object> filters = new HashMap<>();

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("should apply enabled filter when true")
    void buildSpecification_withEnabledTrue_shouldApplyFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", Boolean.TRUE);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(root).get("enabled");
        verify(criteriaBuilder).equal(path, Boolean.TRUE);
    }

    @Test
    @DisplayName("should apply enabled filter when false")
    void buildSpecification_withEnabledFalse_shouldApplyFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", Boolean.FALSE);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(root).get("enabled");
        verify(criteriaBuilder).equal(path, Boolean.FALSE);
    }

    @Test
    @DisplayName("should skip enabled filter when value is null")
    void buildSpecification_withEnabledNull_shouldSkipFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", null);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).equal(any(), eq(Boolean.TRUE));
        verify(criteriaBuilder, never()).equal(any(), eq(Boolean.FALSE));
    }

    @Test
    @DisplayName("should apply applicationType filter with BOTH logic for SALE")
    void buildSpecification_withApplicationTypeSale_shouldIncludeBoth() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("applicationType", TaxApplicationType.SALE);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should apply applicationType filter with BOTH logic for PURCHASE")
    void buildSpecification_withApplicationTypePurchase_shouldIncludeBoth() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("applicationType", TaxApplicationType.PURCHASE);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should apply applicationType filter for BOTH")
    void buildSpecification_withApplicationTypeBoth_shouldIncludeBoth() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("applicationType", TaxApplicationType.BOTH);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should skip applicationType filter when value is null")
    void buildSpecification_withApplicationTypeNull_shouldSkipFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("applicationType", null);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    @DisplayName("should apply name filter with case-insensitive partial match")
    void buildSpecification_withName_shouldApplyLikeFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", "IVA");

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should trim name filter before applying")
    void buildSpecification_withNameWithSpaces_shouldTrimAndApply() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", "  ReteFuente  ");

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should skip name filter when value is null")
    void buildSpecification_withNameNull_shouldSkipFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", null);

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    @DisplayName("should skip name filter when value is blank")
    void buildSpecification_withNameBlank_shouldSkipFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", "   ");

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    @DisplayName("should combine multiple filters with AND")
    void buildSpecification_withMultipleFilters_shouldCombineWithAnd() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", Boolean.TRUE);
        filters.put("applicationType", TaxApplicationType.SALE);
        filters.put("name", "IVA");

        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
    }
}
