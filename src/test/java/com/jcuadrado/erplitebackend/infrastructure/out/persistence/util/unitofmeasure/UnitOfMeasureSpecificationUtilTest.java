package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.unitofmeasure;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure.UnitOfMeasureEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureSpecificationUtilTest {

    @Mock
    private Root<UnitOfMeasureEntity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate predicate;

    @Mock
    private Expression<String> expression;

    @BeforeEach
    void setUp() {
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        lenient().when(criteriaBuilder.like(ArgumentMatchers.<Expression<String>>any(), anyString())).thenReturn(predicate);
        lenient().when(criteriaBuilder.lower(ArgumentMatchers.<Expression<String>>any())).thenReturn(expression);
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.conjunction()).thenReturn(predicate);
    }

    @Test
    void constructor_shouldBeCallable() throws Exception {
        Constructor<UnitOfMeasureSpecificationUtil> constructor = UnitOfMeasureSpecificationUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        UnitOfMeasureSpecificationUtil util = constructor.newInstance();
        assertThat(util).isNotNull();
    }

    @Test
    void buildSpecification_shouldReturnConjunctionWhenNullFilters() {
        Specification<UnitOfMeasureEntity> spec = UnitOfMeasureSpecificationUtil.buildSpecification(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void buildSpecification_shouldReturnConjunctionWhenEmptyFilters() {
        Specification<UnitOfMeasureEntity> spec = UnitOfMeasureSpecificationUtil.buildSpecification(Collections.emptyMap());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder, atLeastOnce()).conjunction();
    }

    @Test
    void buildSpecification_shouldApplyEnabledNameAndAbbreviationFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        filters.put("name", "ca");
        filters.put("abbreviation", "k");

        Specification<UnitOfMeasureEntity> spec = UnitOfMeasureSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(root).get("enabled");
        verify(root).get("name");
        verify(root).get("abbreviation");
        verify(criteriaBuilder).equal(path, true);
        verify(criteriaBuilder, times(2)).like(ArgumentMatchers.<Expression<String>>any(), anyString());
    }

    @Test
    void buildSpecification_shouldIgnoreBlankOrNullNameAndAbbreviation() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", null);
        filters.put("name", " ");
        filters.put("abbreviation", null);

        Specification<UnitOfMeasureEntity> spec = UnitOfMeasureSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder, never()).like(ArgumentMatchers.<Expression<String>>any(), anyString());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    void buildSpecification_shouldIgnoreUnknownFilterKeys() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("unknown", "value");

        Specification<UnitOfMeasureEntity> spec = UnitOfMeasureSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    void buildSpecification_shouldIgnoreNullNameAndNullAbbreviation() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", null);
        filters.put("abbreviation", null);

        Specification<UnitOfMeasureEntity> spec = UnitOfMeasureSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder, never()).like(ArgumentMatchers.<Expression<String>>any(), anyString());
    }

    @Test
    void buildSpecification_shouldIgnoreBlankAbbreviation() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("abbreviation", "   ");

        Specification<UnitOfMeasureEntity> spec = UnitOfMeasureSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder, never()).like(ArgumentMatchers.<Expression<String>>any(), anyString());
    }
}
