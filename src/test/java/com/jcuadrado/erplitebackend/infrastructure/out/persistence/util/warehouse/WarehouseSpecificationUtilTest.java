package com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.warehouse.WarehouseEntity;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseSpecificationUtilTest {

    @Mock
    private Root<WarehouseEntity> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder cb;

    private Predicate mockPredicate() {
        return mock(Predicate.class);
    }

    @SuppressWarnings("unchecked")
    private void setupRootPath(String field) {
        Path path = mock(Path.class);
        when(root.get(field)).thenReturn(path);
    }

    @Test
    @DisplayName("buildSpecification should always include deletedAt IS NULL predicate")
    void buildSpecification_shouldAlwaysIncludeDeletedAtIsNull() {
        setupRootPath("deletedAt");
        when(cb.isNull(any())).thenReturn(mockPredicate());
        when(cb.and(any(Predicate[].class))).thenReturn(mockPredicate());

        Specification<WarehouseEntity> spec = WarehouseSpecificationUtil.buildSpecification(Map.of());
        spec.toPredicate(root, query, cb);

        verify(cb).isNull(root.get("deletedAt"));
    }

    @Test
    @DisplayName("buildSpecification should filter by active when provided")
    void buildSpecification_shouldFilterByActive() {
        setupRootPath("deletedAt");
        setupRootPath("active");
        when(cb.isNull(any())).thenReturn(mockPredicate());
        when(cb.equal(any(), eq(true))).thenReturn(mockPredicate());
        when(cb.and(any(Predicate[].class))).thenReturn(mockPredicate());

        WarehouseSpecificationUtil.buildSpecification(Map.of("active", true))
                .toPredicate(root, query, cb);

        verify(cb).equal(root.get("active"), true);
    }

    @Test
    @DisplayName("buildSpecification should filter by type using WarehouseType")
    void buildSpecification_shouldFilterByType() {
        setupRootPath("deletedAt");
        setupRootPath("type");
        when(cb.isNull(any())).thenReturn(mockPredicate());
        when(cb.equal(any(), eq(WarehouseType.SUCURSAL))).thenReturn(mockPredicate());
        when(cb.and(any(Predicate[].class))).thenReturn(mockPredicate());

        WarehouseSpecificationUtil.buildSpecification(Map.of("type", "SUCURSAL"))
                .toPredicate(root, query, cb);

        verify(cb).equal(root.get("type"), WarehouseType.SUCURSAL);
    }

    @Test
    @DisplayName("buildSpecification should filter by municipalityId as String")
    void buildSpecification_shouldFilterByMunicipalityId() {
        UUID municipalityId = UUID.randomUUID();
        setupRootPath("deletedAt");
        setupRootPath("municipalityUuid");
        when(cb.isNull(any())).thenReturn(mockPredicate());
        when(cb.equal(any(), eq(municipalityId.toString()))).thenReturn(mockPredicate());
        when(cb.and(any(Predicate[].class))).thenReturn(mockPredicate());

        WarehouseSpecificationUtil.buildSpecification(Map.of("municipalityId", municipalityId))
                .toPredicate(root, query, cb);

        verify(cb).equal(root.get("municipalityUuid"), municipalityId.toString());
    }

    @Test
    @DisplayName("buildSpecification should filter by name using LIKE")
    void buildSpecification_shouldFilterByNameLike() {
        setupRootPath("deletedAt");
        setupRootPath("name");
        Path namePath = mock(Path.class);
        when(root.get("name")).thenReturn(namePath);
        when(cb.isNull(any())).thenReturn(mockPredicate());
        when(cb.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(cb.like(any(), anyString())).thenReturn(mockPredicate());
        when(cb.and(any(Predicate[].class))).thenReturn(mockPredicate());

        WarehouseSpecificationUtil.buildSpecification(Map.of("name", "bodega"))
                .toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%bodega%"));
    }

    @Test
    @DisplayName("buildSpecification should return non-null specification for empty filters")
    void buildSpecification_shouldReturnSpec_forEmptyFilters() {
        setupRootPath("deletedAt");
        when(cb.isNull(any())).thenReturn(mockPredicate());
        when(cb.and(any(Predicate[].class))).thenReturn(mockPredicate());

        Specification<WarehouseEntity> spec = WarehouseSpecificationUtil.buildSpecification(null);

        assertThat(spec).isNotNull();
        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
    }
}
