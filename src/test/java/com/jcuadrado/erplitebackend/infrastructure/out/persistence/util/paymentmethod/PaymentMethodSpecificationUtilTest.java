package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.paymentmethod;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentMethodSpecificationUtil
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMethodSpecificationUtil Tests")
class PaymentMethodSpecificationUtilTest {

    @Mock
    private Root<PaymentMethodEntity> root;

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
    @DisplayName("Should throw exception when trying to instantiate utility class")
    void testPrivateConstructor() throws NoSuchMethodException {
        // Given
        Constructor<PaymentMethodSpecificationUtil> constructor = PaymentMethodSpecificationUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Then
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .hasRootCauseMessage("Utility class cannot be instantiated");
    }

    @Test
    @DisplayName("Should execute specification with null filters")
    void testExecuteSpecification_withNullFilters() {
        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should execute specification with empty filters")
    void testExecuteSpecification_withEmptyFilters() {
        // Given
        Map<String, Object> filters = new HashMap<>();

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should apply enabled filter when true")
    void testHasEnabled_withTrueValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", Boolean.TRUE);

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root).get("enabled");
        verify(criteriaBuilder).equal(path, Boolean.TRUE);
    }

    @Test
    @DisplayName("Should apply enabled filter when false")
    void testHasEnabled_withFalseValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", Boolean.FALSE);

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root).get("enabled");
        verify(criteriaBuilder).equal(path, Boolean.FALSE);
    }

    @Test
    @DisplayName("Should not apply enabled filter when value is null")
    void testHasEnabled_withNullValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", null);

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    @DisplayName("Should apply search filter with non-empty search term")
    void testSearch_withNonEmptyTerm() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "Efectivo");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root, times(2)).get(anyString()); // Once for code, once for name
        verify(criteriaBuilder, times(2)).lower(ArgumentMatchers.<Expression<String>>any());
        verify(criteriaBuilder, times(2)).like(ArgumentMatchers.<Expression<String>>any(), eq("%efectivo%"));
        verify(criteriaBuilder).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    @DisplayName("Should trim search term before applying")
    void testSearch_withSpacesInTerm() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "  Tarjeta  ");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder, times(2)).like(ArgumentMatchers.<Expression<String>>any(), eq("%tarjeta%"));
    }

    @Test
    @DisplayName("Should not apply search filter when term is null")
    void testSearch_withNullTerm() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", null);

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    @DisplayName("Should not apply search filter when term is empty")
    void testSearch_withEmptyTerm() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    @DisplayName("Should not apply search filter when term is blank")
    void testSearch_withBlankTerm() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("search", "   ");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).and(any(Predicate[].class));
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    @DisplayName("Should apply code filter")
    void testHasCode_withValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("code", "CASH");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root).get("code");
        verify(criteriaBuilder).equal(path, "CASH");
    }

    @Test
    @DisplayName("Should not apply code filter when code is null")
    void testHasCode_withNullValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("code", null);

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root, never()).get("code");
        verify(criteriaBuilder, never()).equal(any(), any());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should not apply code filter when code is blank")
    void testHasCode_withBlankValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("code", "   ");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root, never()).get("code");
        verify(criteriaBuilder, never()).equal(any(), any());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should apply name filter")
    void testHasName_withValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", "Efectivo");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root).get("name");
        verify(criteriaBuilder).lower(ArgumentMatchers.<Expression<String>>any());
        verify(criteriaBuilder).like(ArgumentMatchers.<Expression<String>>any(), eq("%efectivo%"));
    }

    @Test
    @DisplayName("Should not apply name filter when name is null")
    void testHasName_withNullValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", null);

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root, never()).get("name");
        verify(criteriaBuilder, never()).like(any(), anyString());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should not apply name filter when name is blank")
    void testHasName_withBlankValue() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("name", "   ");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(root, never()).get("name");
        verify(criteriaBuilder, never()).like(any(), anyString());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should combine multiple filters with AND")
    void testCombineMultipleFilters() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", Boolean.TRUE);
        filters.put("search", "Tarjeta");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).equal(path, Boolean.TRUE);
        verify(criteriaBuilder, times(2)).like(ArgumentMatchers.<Expression<String>>any(), eq("%tarjeta%"));
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Should handle all filters combined")
    void testAllFiltersCombined() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", Boolean.TRUE);
        filters.put("code", "CC");
        filters.put("name", "Tarjeta de Crédito");
        filters.put("search", "crédito");

        // When
        Specification<PaymentMethodEntity> spec = PaymentMethodSpecificationUtil.buildSpecification(filters);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        // Verify enabled filter
        verify(criteriaBuilder).equal(path, Boolean.TRUE);
        // Verify code filter (exact match with uppercase)
        verify(criteriaBuilder).equal(path, "CC");
        // Verify name filter (like with lowercase)
        verify(criteriaBuilder, atLeast(3)).lower(ArgumentMatchers.<Expression<String>>any());
        verify(criteriaBuilder).like(ArgumentMatchers.<Expression<String>>any(), eq("%tarjeta de crédito%"));
        // Verify search filter (like with lowercase) - searches in code and name
        verify(criteriaBuilder, times(2)).like(ArgumentMatchers.<Expression<String>>any(), eq("%crédito%"));
        // Verify OR for search (code OR name)
        verify(criteriaBuilder).or(any(Predicate.class), any(Predicate.class));
        // Verify final AND combination
        verify(criteriaBuilder).and(any(Predicate[].class));
    }
}
