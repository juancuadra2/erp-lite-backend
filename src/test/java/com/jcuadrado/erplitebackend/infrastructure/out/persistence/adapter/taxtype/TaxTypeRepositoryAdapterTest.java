package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.TaxTypeJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.taxtype.TaxTypeEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TaxTypeRepositoryAdapter - Unit Tests")
@ExtendWith(MockitoExtension.class)
class TaxTypeRepositoryAdapterTest {

    @Mock
    private TaxTypeJpaRepository jpaRepository;

    @Mock
    private TaxTypeEntityMapper mapper;

    @InjectMocks
    private TaxTypeRepositoryAdapter adapter;

    private TaxType domainObject;
    private TaxTypeEntity entity;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();

        domainObject = TaxType.builder()
                .id(1L)
                .uuid(testUuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(true)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .build();

        entity = TaxTypeEntity.builder()
                .id(1L)
                .uuid(testUuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(true)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("save should save and return domain object")
    void save_shouldSaveAndReturnDomainObject() {
        when(mapper.toEntity(domainObject)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        TaxType result = adapter.save(domainObject);

        assertThat(result).isNotNull().isEqualTo(domainObject);
        verify(mapper).toEntity(domainObject);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByUuid should return optional with domain object when exists")
    void findByUuid_whenExists_shouldReturnOptionalWithDomainObject() {
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        Optional<TaxType> result = adapter.findByUuid(testUuid);

        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findByUuid(testUuid);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByUuid should return empty optional when not exists")
    void findByUuid_whenNotExists_shouldReturnEmptyOptional() {
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        Optional<TaxType> result = adapter.findByUuid(testUuid);

        assertThat(result).isEmpty();
        verify(jpaRepository).findByUuid(testUuid);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findByCode should return optional with domain object when exists")
    void findByCode_whenExists_shouldReturnOptionalWithDomainObject() {
        when(jpaRepository.findByCode("IVA19")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        Optional<TaxType> result = adapter.findByCode("IVA19");

        assertThat(result).isPresent().contains(domainObject);
        verify(jpaRepository).findByCode("IVA19");
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByCode should return empty optional when not exists")
    void findByCode_whenNotExists_shouldReturnEmptyOptional() {
        when(jpaRepository.findByCode("IVA19")).thenReturn(Optional.empty());

        Optional<TaxType> result = adapter.findByCode("IVA19");

        assertThat(result).isEmpty();
        verify(jpaRepository).findByCode("IVA19");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("delete should delete existing tax type by uuid")
    void delete_shouldDeleteExistingTaxTypeByUuid() {
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.of(entity));

        adapter.delete(domainObject);

        verify(jpaRepository).findByUuid(testUuid);
        verify(jpaRepository).delete(entity);
    }

    @Test
    @DisplayName("delete should do nothing when tax type not found")
    void delete_whenNotFound_shouldDoNothing() {
        when(jpaRepository.findByUuid(testUuid)).thenReturn(Optional.empty());

        adapter.delete(domainObject);

        verify(jpaRepository).findByUuid(testUuid);
        verify(jpaRepository, never()).delete(any(TaxTypeEntity.class));
    }

    @Test
    @DisplayName("findAll should return paged domain objects with filters")
    void findAll_shouldReturnPagedDomainObjectsWithFilters() {
        Map<String, Object> filters = Map.of("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);
        List<TaxTypeEntity> entities = List.of(entity);
        Page<TaxTypeEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(jpaRepository.findAll(ArgumentMatchers.<Specification<TaxTypeEntity>>any(), eq(pageable))).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        Page<TaxType> result = adapter.findAll(filters, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1).contains(domainObject);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(jpaRepository).findAll(ArgumentMatchers.<Specification<TaxTypeEntity>>any(), eq(pageable));
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByEnabled should return all tax types when enabled is null")
    void findByEnabled_whenNull_shouldReturnAll() {
        List<TaxTypeEntity> entities = List.of(entity);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        List<TaxType> result = adapter.findByEnabled(null);

        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findAll();
        verify(jpaRepository, never()).findByEnabled(any());
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByEnabled should return enabled tax types when enabled is true")
    void findByEnabled_whenTrue_shouldReturnEnabled() {
        List<TaxTypeEntity> entities = List.of(entity);
        when(jpaRepository.findByEnabled(true)).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        List<TaxType> result = adapter.findByEnabled(true);

        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findByEnabled(true);
        verify(jpaRepository, never()).findAll();
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByEnabled should return disabled tax types when enabled is false")
    void findByEnabled_whenFalse_shouldReturnDisabled() {
        TaxType disabledDomain = TaxType.builder()
                .id(2L)
                .uuid(UUID.randomUUID())
                .code("IVA5")
                .name("IVA 5%")
                .enabled(false)
                .build();
        TaxTypeEntity disabledEntity = TaxTypeEntity.builder()
                .id(2L)
                .enabled(false)
                .build();

        List<TaxTypeEntity> entities = List.of(disabledEntity);
        when(jpaRepository.findByEnabled(false)).thenReturn(entities);
        when(mapper.toDomain(disabledEntity)).thenReturn(disabledDomain);

        List<TaxType> result = adapter.findByEnabled(false);

        assertThat(result).hasSize(1).contains(disabledDomain);
        verify(jpaRepository).findByEnabled(false);
        verify(mapper).toDomain(disabledEntity);
    }

    @Test
    @DisplayName("findByNameContaining should return matching tax types without enabled filter")
    void findByNameContaining_withoutEnabledFilter_shouldReturnMatchingTaxTypes() {
        List<TaxTypeEntity> entities = List.of(entity);
        when(jpaRepository.findByNameContainingIgnoreCase("IVA")).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        List<TaxType> result = adapter.findByNameContaining("IVA", null);

        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findByNameContainingIgnoreCase("IVA");
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByNameContaining should filter by enabled when specified")
    void findByNameContaining_withEnabledFilter_shouldFilterByEnabled() {
        TaxTypeEntity disabledEntity = TaxTypeEntity.builder()
                .id(2L)
                .name("IVA Disabled")
                .enabled(false)
                .build();
        List<TaxTypeEntity> entities = Arrays.asList(entity, disabledEntity);
        when(jpaRepository.findByNameContainingIgnoreCase("IVA")).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        List<TaxType> result = adapter.findByNameContaining("IVA", true);

        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findByNameContainingIgnoreCase("IVA");
        verify(mapper).toDomain(entity);
        verify(mapper, never()).toDomain(disabledEntity);
    }

    @Test
    @DisplayName("findByApplicationType should return matching tax types without enabled filter")
    void findByApplicationType_withoutEnabledFilter_shouldReturnMatchingTaxTypes() {
        List<TaxTypeEntity> entities = List.of(entity);
        when(jpaRepository.findAll(ArgumentMatchers.<Specification<TaxTypeEntity>>any())).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        List<TaxType> result = adapter.findByApplicationType(TaxApplicationType.SALE, null);

        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findAll(ArgumentMatchers.<Specification<TaxTypeEntity>>any());
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findByApplicationType should filter by enabled when specified")
    void findByApplicationType_withEnabledFilter_shouldFilterByEnabled() {
        List<TaxTypeEntity> entities = List.of(entity);
        when(jpaRepository.findAll(ArgumentMatchers.<Specification<TaxTypeEntity>>any())).thenReturn(entities);
        when(mapper.toDomain(entity)).thenReturn(domainObject);

        List<TaxType> result = adapter.findByApplicationType(TaxApplicationType.PURCHASE, true);

        assertThat(result).hasSize(1).contains(domainObject);
        verify(jpaRepository).findAll(ArgumentMatchers.<Specification<TaxTypeEntity>>any());
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("existsByCode should return true when code exists")
    void existsByCode_whenExists_shouldReturnTrue() {
        when(jpaRepository.existsByCode("IVA19")).thenReturn(true);

        boolean result = adapter.existsByCode("IVA19");

        assertThat(result).isTrue();
        verify(jpaRepository).existsByCode("IVA19");
    }

    @Test
    @DisplayName("existsByCode should return false when code not exists")
    void existsByCode_whenNotExists_shouldReturnFalse() {
        when(jpaRepository.existsByCode("IVA19")).thenReturn(false);

        boolean result = adapter.existsByCode("IVA19");

        assertThat(result).isFalse();
        verify(jpaRepository).existsByCode("IVA19");
    }

    @Test
    @DisplayName("existsByCodeAndUuidNot should return true when code exists for different uuid")
    void existsByCodeAndUuidNot_whenExistsForDifferentUuid_shouldReturnTrue() {
        when(jpaRepository.existsByCodeAndUuidNot("IVA19", testUuid)).thenReturn(true);

        boolean result = adapter.existsByCodeAndUuidNot("IVA19", testUuid);

        assertThat(result).isTrue();
        verify(jpaRepository).existsByCodeAndUuidNot("IVA19", testUuid);
    }

    @Test
    @DisplayName("existsByCodeAndUuidNot should return false when code not exists for different uuid")
    void existsByCodeAndUuidNot_whenNotExistsForDifferentUuid_shouldReturnFalse() {
        when(jpaRepository.existsByCodeAndUuidNot("IVA19", testUuid)).thenReturn(false);

        boolean result = adapter.existsByCodeAndUuidNot("IVA19", testUuid);

        assertThat(result).isFalse();
        verify(jpaRepository).existsByCodeAndUuidNot("IVA19", testUuid);
    }

    @Test
    @DisplayName("countProductsWithTaxType should return 0 (TODO implementation)")
    void countProductsWithTaxType_shouldReturn0() {
        long result = adapter.countProductsWithTaxType(testUuid);

        assertThat(result).isEqualTo(0L);
        verifyNoInteractions(jpaRepository);
    }

    @Test
    @DisplayName("countTransactionsWithTaxType should return 0 (TODO implementation)")
    void countTransactionsWithTaxType_shouldReturn0() {
        long result = adapter.countTransactionsWithTaxType(testUuid);

        assertThat(result).isEqualTo(0L);
        verifyNoInteractions(jpaRepository);
    }
}
