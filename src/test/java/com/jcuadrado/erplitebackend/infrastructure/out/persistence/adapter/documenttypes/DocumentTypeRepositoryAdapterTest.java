package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.DocumentTypeJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.documenttypes.DocumentTypeEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.documenttypes.DocumentTypeEntityMapper;
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
class DocumentTypeRepositoryAdapterTest {

    @Mock
    private DocumentTypeJpaRepository jpaRepository;

    @Mock
    private DocumentTypeEntityMapper mapper;

    @InjectMocks
    private DocumentTypeRepositoryAdapter adapter;

    private DocumentType domainObject;
    private DocumentTypeEntity entity;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        domainObject = DocumentType.builder()
                .code("CC")
                .name("Cedula de Ciudadania")
                .description("Documento de identidad")
                .active(true)
                .build();
        entity = DocumentTypeEntity.builder()
                .id(1L)
                .uuid(testUuid)
                .code("CC")
                .name("Cedula de Ciudadania")
                .description("Documento de identidad")
                .active(true)
                .build();
    }

    @Test
    void save_shouldSaveAndReturnDomainObject() {
        // Given
        when(mapper.toEntity(domainObject)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        DocumentType result = adapter.save(domainObject);

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
        Optional<DocumentType> result = adapter.findById(1L);

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
        Optional<DocumentType> result = adapter.findById(1L);

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
        Optional<DocumentType> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findByUuid(testUuid);
    }

    @Test
    void findByUuid_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        // When
        Optional<DocumentType> result = adapter.findByUuid(testUuid);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByCode_whenExists_shouldReturnOptionalWithDomainObject() {
        // Given
        when(jpaRepository.findByCode("CC")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Optional<DocumentType> result = adapter.findByCode("CC");

        // Then
        assertThat(result).isPresent().contains(domainObject);
    }

    @Test
    void findByCode_whenNotExists_shouldReturnEmptyOptional() {
        // Given
        when(jpaRepository.findByCode("CC")).thenReturn(Optional.empty());

        // When
        Optional<DocumentType> result = adapter.findByCode("CC");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnPageOfDomainObjects() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<DocumentTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        
        when(jpaRepository.findAll(ArgumentMatchers.<Specification<DocumentTypeEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        Page<DocumentType> result = adapter.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(domainObject);
        verify(jpaRepository).findAll(ArgumentMatchers.<Specification<DocumentTypeEntity>>any(), eq(pageable));
    }

    @Test
    void findAllActive_shouldReturnListOfActiveDomainObjects() {
        // Given
        when(jpaRepository.findByActiveTrue()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        // When
        List<DocumentType> result = adapter.findAllActive();

        // Then
        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findByActiveTrue();
    }

    @Test
    void existsByCode_whenExists_shouldReturnTrue() {
        // Given
        when(jpaRepository.existsByCode("CC")).thenReturn(true);

        // When
        boolean result = adapter.existsByCode("CC");

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCode("CC");
    }

    @Test
    void existsByCode_whenNotExists_shouldReturnFalse() {
        // Given
        when(jpaRepository.existsByCode("CC")).thenReturn(false);

        // When
        boolean result = adapter.existsByCode("CC");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByCodeExcludingUuid_whenExists_shouldReturnTrue() {
        // Given
        when(jpaRepository.existsByCodeAndUuidNot("CC", testUuid)).thenReturn(true);

        // When
        boolean result = adapter.existsByCodeExcludingUuid("CC", testUuid);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByCodeAndUuidNot("CC", testUuid);
    }

    @Test
    void existsByCodeExcludingUuid_whenNotExists_shouldReturnFalse() {
        // Given
        when(jpaRepository.existsByCodeAndUuidNot("CC", testUuid)).thenReturn(false);

        // When
        boolean result = adapter.existsByCodeExcludingUuid("CC", testUuid);

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
}
