package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.geography;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.DepartmentEntity;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentSpecificationUtil Tests")
class DepartmentSpecificationUtilTest {

    @Mock
    private Root<DepartmentEntity> root;

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
        lenient().when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(criteriaBuilder.conjunction()).thenReturn(predicate);
    }

    @Test
    @DisplayName("Should execute specification with null filters")
    void testExecuteSpecification_withNullFilters() {
        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).conjunction();
    }

    @Test
    @DisplayName("Should execute specification with empty filters")
    void testExecuteSpecification_withEmptyFilters() {
        // Given
        Map<String, Object> filters = new HashMap<>();

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).conjunction();
    }

    @Test
    @DisplayName("Should apply enabled filter when true")
    void testExecuteSpecification_withEnabledTrue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(root).get("enabled");
        verify(criteriaBuilder).equal(path, true);
    }

    @Test
    @DisplayName("Should apply enabled filter when false")
    void testExecuteSpecification_withEnabledFalse() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", false);

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(root).get("enabled");
        verify(criteriaBuilder).equal(path, false);
    }

    @Test
    @DisplayName("Should apply search filter in code and name")
    void testExecuteSpecification_withSearchFilter() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "test");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(root, times(2)).get(anyString()); // code, name
        verify(criteriaBuilder, times(2)).lower(ArgumentMatchers.<Expression<String>>any());
        verify(criteriaBuilder, times(2)).like(ArgumentMatchers.<Expression<String>>any(), eq("%test%"));
    }

    @Test
    @DisplayName("Should apply code filter with exact match")
    void testExecuteSpecification_withCodeFilter() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("code", "05");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(root).get("code");
        verify(criteriaBuilder).equal(path, "05");
    }

    @Test
    @DisplayName("Should apply name filter with contains search")
    void testExecuteSpecification_withNameFilter() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", "antioquia");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(root).get("name");
        verify(criteriaBuilder).lower(ArgumentMatchers.<Expression<String>>any());
        verify(criteriaBuilder).like(ArgumentMatchers.<Expression<String>>any(), eq("%antioquia%"));
    }

    @Test
    @DisplayName("Should combine multiple filters with AND")
    void testExecuteSpecification_withMultipleFilters() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        filters.put("code", "05");
        filters.put("name", "antioquia");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should skip enabled filter when null")
    void testExecuteSpecification_withNullEnabled() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", null);

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    @DisplayName("Should skip search filter when empty")
    void testExecuteSpecification_withEmptySearch() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(criteriaBuilder, never()).like(ArgumentMatchers.<Expression<String>>any(), anyString());
    }

    @Test
    @DisplayName("Should skip search filter when whitespace")
    void testExecuteSpecification_withWhitespaceSearch() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "   ");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(criteriaBuilder, never()).like(ArgumentMatchers.<Expression<String>>any(), anyString());
    }

    @Test
    @DisplayName("Should skip code filter when empty")
    void testExecuteSpecification_withEmptyCode() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("code", "");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should skip code filter when null")
    void testExecuteSpecification_withNullCode() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("code", null);

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(root, never()).get("code");
    }

    @Test
    @DisplayName("Should trim search value")
    void testExecuteSpecification_withSearchValueWithSpaces() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "  TEST  ");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(criteriaBuilder, atLeast(1)).like(ArgumentMatchers.<Expression<String>>any(), eq("%test%"));
    }

    @Test
    @DisplayName("Should trim code value")
    void testExecuteSpecification_withCodeValueWithSpaces() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("code", "  05  ");

        // When
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        spec.toPredicate(root, query, criteriaBuilder);

        // Then
        verify(criteriaBuilder).equal(path, "05");
    }
}
