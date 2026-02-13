package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.MunicipalityJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.MunicipalityEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography.MunicipalityEntityMapper;
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
class MunicipalityRepositoryAdapterTest {

    @Mock
    private MunicipalityJpaRepository jpaRepository;

    @Mock
    private MunicipalityEntityMapper mapper;

    @InjectMocks
    private MunicipalityRepositoryAdapter adapter;

    private Municipality domainObject;
    private MunicipalityEntity entity;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();
        domainObject = Municipality.builder()
                .code("001")
                .name("Medellin")
                .department(dept)
                .enabled(true)
                .build();
        entity = MunicipalityEntity.builder()
                .id(1L)
                .uuid(testUuid)
                .code("001")
                .name("Medellin")
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
        Municipality result = adapter.save(domainObject);

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
        Optional<Municipality> result = adapter.findById(1L);

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
        Optional<Municipality> result = adapter.findById(1L);

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
        Optional<Municipality> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findByUuid(testUuid);
    }

    @Test
    void findByUuid_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // When
        Optional<Municipality> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByCodeAndDepartmentId_whenExists_shouldReturnOptionalWithDomainObject() {
        // Given
        when(jpaRepository.findByCodeAndDepartmentId("001", 1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Optional<Municipality> result = adapter.findByCodeAndDepartmentId("001", 1L);

        // Then
        assertThat(result).isPresent().contains(domainObject);
    }

    @Test
    void findByCodeAndDepartmentId_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByCodeAndDepartmentId("001", 1L)).thenReturn(Optional.empty());

        // When
        Optional<Municipality> result = adapter.findByCodeAndDepartmentId("001", 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnPageOfDomainObjects() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<MunicipalityEntity> entityPage = new PageImpl<>(List.of(entity));
        
        when(jpaRepository.findAll(ArgumentMatchers.<Specification<MunicipalityEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Page<Municipality> result = adapter.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(domainObject);
        verify(jpaRepository).findAll(ArgumentMatchers.<Specification<MunicipalityEntity>>any(), eq(pageable));
    }

    @Test
    void findAllEnabled_shouldReturnListOfEnabledDomainObjects() {
        // Given
        when(jpaRepository.findByEnabledTrue()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        List<Municipality> result = adapter.findAllEnabled();

        // Then
        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findByEnabledTrue();
    }

    @Test
    void existsByCodeAndDepartmentId_whenExists_shouldReturnTrue() {
        // Given
        when(jpaRepository.existsByCodeAndDepartmentId("001", 1L)).thenReturn(true);

        // When
        boolean result = adapter.existsByCodeAndDepartmentId("001", 1L);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCodeAndDepartmentId("001", 1L);
    }

    @Test
    void existsByCodeAndDepartmentId_whenNotExists_shouldReturnFalse() {
        // Given
        when(jpaRepository.existsByCodeAndDepartmentId("001", 1L)).thenReturn(false);

        // When
        boolean result = adapter.existsByCodeAndDepartmentId("001", 1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByCodeAndDepartmentIdExcludingUuid_whenExists_shouldReturnTrue() {
        // Given
        when(jpaRepository.existsByCodeAndDepartmentIdAndUuidNot("001", 1L, testUuid)).thenReturn(true);

        // When
        boolean result = adapter.existsByCodeAndDepartmentIdExcludingUuid("001", 1L, testUuid);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCodeAndDepartmentIdAndUuidNot("001", 1L, testUuid);
    }

    @Test
    void existsByCodeAndDepartmentIdExcludingUuid_whenNotExists_shouldReturnFalse() {
        // Given
        when(jpaRepository.existsByCodeAndDepartmentIdAndUuidNot("001", 1L, testUuid)).thenReturn(false);

        // When
        boolean result = adapter.existsByCodeAndDepartmentIdExcludingUuid("001", 1L, testUuid);

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
    void countByDepartmentId_shouldReturnCount() {
        // Given
        when(jpaRepository.countByDepartmentId(1L)).thenReturn(10L);

        // When
        long result = adapter.countByDepartmentId(1L);

        // Then
        assertThat(result).isEqualTo(10L);
        verify(jpaRepository).countByDepartmentId(1L);
    }

    @Test
    void findAllByDepartmentIdAndEnabled_shouldReturnList() {
        // Given
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();
        Municipality muni1 = Municipality.builder().code("001").name("Medellin").department(dept).enabled(true).build();
        Municipality muni2 = Municipality.builder().code("002").name("Envigado").department(dept).enabled(true).build();
        
        MunicipalityEntity entity1 = MunicipalityEntity.builder().id(1L).code("001").name("Medellin").enabled(true).build();
        MunicipalityEntity entity2 = MunicipalityEntity.builder().id(2L).code("002").name("Envigado").enabled(true).build();
        
        when(jpaRepository.findByDepartmentIdAndEnabledOrderByNameAsc(1L, true))
                .thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(muni1);
        when(mapper.toDomain(entity2)).thenReturn(muni2);

        // When
        List<Municipality> result = adapter.findAllByDepartmentIdAndEnabled(1L, true);

        // Then
        assertThat(result).hasSize(2).containsExactly(muni1, muni2);
        verify(jpaRepository).findByDepartmentIdAndEnabledOrderByNameAsc(1L, true);
        verify(mapper).toDomain(entity1);
        verify(mapper).toDomain(entity2);
    }

    @Test
    void findAllByDepartmentIdAndEnabled_shouldReturnEmptyList() {
        // Given
        when(jpaRepository.findByDepartmentIdAndEnabledOrderByNameAsc(1L, true))
                .thenReturn(Collections.emptyList());

        // When
        List<Municipality> result = adapter.findAllByDepartmentIdAndEnabled(1L, true);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByDepartmentIdAndEnabledOrderByNameAsc(1L, true);
    }

    @Test
    void findAllByDepartmentIdAndEnabled_withDisabled_shouldReturnList() {
        // Given
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();
        Municipality muni1 = Municipality.builder().code("001").name("Medellin").department(dept).enabled(false).build();
        
        MunicipalityEntity entity1 = MunicipalityEntity.builder().id(1L).code("001").name("Medellin").enabled(false).build();
        
        when(jpaRepository.findByDepartmentIdAndEnabledOrderByNameAsc(1L, false))
                .thenReturn(List.of(entity1));
        when(mapper.toDomain(entity1)).thenReturn(muni1);

        // When
        List<Municipality> result = adapter.findAllByDepartmentIdAndEnabled(1L, false);

        // Then
        assertThat(result).hasSize(1).containsExactly(muni1);
        verify(jpaRepository).findByDepartmentIdAndEnabledOrderByNameAsc(1L, false);
        verify(mapper).toDomain(entity1);
    }
}
