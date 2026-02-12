package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.DepartmentJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.DepartmentEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography.DepartmentEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentRepositoryAdapterTest {

    @Mock
    private DepartmentJpaRepository jpaRepository;

    @Mock
    private DepartmentEntityMapper mapper;

    @InjectMocks
    private DepartmentRepositoryAdapter adapter;

    private Department domainObject;
    private DepartmentEntity entity;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        domainObject = Department.builder()
                .code("05")
                .name("Antioquia")
                .enabled(true)
                .build();
        entity = DepartmentEntity.builder()
                .id(1L)
                .uuid(testUuid)
                .code("05")
                .name("Antioquia")
                .enabled(true)
                .build();
    }

    @Test
    void save_shouldSaveAndReturnDomainObject() {
        // Given
        when(mapper.toEntity(domainObject)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Department result = adapter.save(domainObject);

        // Then
        assertThat(result).isNotNull().isEqualTo(domainObject);
        verify(mapper).toEntity(domainObject);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_whenExists_shouldReturnOptionalWithDomainObject() {
        // Given
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Optional<Department> result = adapter.findById(1L);

        // Then
        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findById(1L);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Department> result = adapter.findById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(1L);
    }

    @Test
    void findByUuid_whenExists_shouldReturnOptionalWithDomainObject() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Optional<Department> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findByUuid(testUuid);
    }

    @Test
    void findByUuid_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // When
        Optional<Department> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_whenExists_shouldReturnOptionalWithDomainObject() {
        // Given
        when(jpaRepository.findByCode("05")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Optional<Department> result = adapter.findByCode("05");

        // Then
        assertThat(result).isPresent().contains(domainObject);
    }

    @Test
    void findByCode_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByCode("05")).thenReturn(Optional.empty());

        // When
        Optional<Department> result = adapter.findByCode("05");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnPageOfDomainObjects() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<DepartmentEntity> entityPage = new PageImpl<>(List.of(entity));
        
        when(jpaRepository.findAll(ArgumentMatchers.<Specification<DepartmentEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Page<Department> result = adapter.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(domainObject);
        verify(jpaRepository).findAll(ArgumentMatchers.<Specification<DepartmentEntity>>any(), eq(pageable));
    }

    @Test
    void findAllEnabled_shouldReturnListOfEnabledDomainObjects() {
        // Given
        when(jpaRepository.findByEnabledTrue()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        List<Department> result = adapter.findAllEnabled();

        // Then
        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findByEnabledTrue();
    }

    @Test
    void existsByCode_whenExists_shouldReturnTrue() {
        // Given
        when(jpaRepository.existsByCode("05")).thenReturn(true);

        // When
        boolean result = adapter.existsByCode("05");

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCode("05");
    }

    @Test
    void existsByCode_whenNotExists_shouldReturnFalse() {
        // Given
        when(jpaRepository.existsByCode("05")).thenReturn(false);

        // When
        boolean result = adapter.existsByCode("05");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByCodeExcludingUuid_whenExists_shouldReturnTrue() {
        // Given
        when(jpaRepository.existsByCodeAndUuidNot("05", testUuid)).thenReturn(true);

        // When
        boolean result = adapter.existsByCodeExcludingUuid("05", testUuid);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCodeAndUuidNot("05", testUuid);
    }

    @Test
    void existsByCodeExcludingUuid_whenNotExists_shouldReturnFalse() {
        // Given
        when(jpaRepository.existsByCodeAndUuidNot("05", testUuid)).thenReturn(false);

        // When
        boolean result = adapter.existsByCodeExcludingUuid("05", testUuid);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void deleteByUuid_whenExists_shouldDelete() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.of(entity));

        // When
        adapter.deleteByUuid(testUuid);

        // Then
        verify(jpaRepository).findByUuid(testUuid);
        verify(jpaRepository).delete(entity);
    }

    @Test
    void deleteByUuid_whenNotExists_shouldNotDelete() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // When
        adapter.deleteByUuid(testUuid);

        // Then
        verify(jpaRepository).findByUuid(testUuid);
    }

    @Test
    void count_shouldReturnTotalCount() {
        // Given
        when(jpaRepository.count()).thenReturn(5L);

        // When
        long result = adapter.count();

        // Then
        assertThat(result).isEqualTo(5L);
        verify(jpaRepository).count();
    }
}
