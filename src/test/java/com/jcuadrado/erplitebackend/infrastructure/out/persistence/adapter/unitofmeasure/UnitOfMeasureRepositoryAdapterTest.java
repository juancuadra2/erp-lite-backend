package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.UnitOfMeasureJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure.UnitOfMeasureEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.unitofmeasure.UnitOfMeasureEntityMapper;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureRepositoryAdapterTest {

    @Mock
    private UnitOfMeasureJpaRepository jpaRepository;

    @Mock
    private UnitOfMeasureEntityMapper mapper;

    @InjectMocks
    private UnitOfMeasureRepositoryAdapter adapter;

    private UnitOfMeasure domain;
    private UnitOfMeasureEntity entity;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        domain = UnitOfMeasure.builder().id(1L).uuid(uuid).name("Caja").abbreviation("CJ").enabled(true).build();
        entity = UnitOfMeasureEntity.builder().id(1L).uuid(uuid).name("Caja").abbreviation("CJ").enabled(true).build();
    }

    @Test
    void save_shouldMapAndPersist() {
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        UnitOfMeasure result = adapter.save(domain);

        assertThat(result).isEqualTo(domain);
    }

    @Test
    void findByUuid_shouldReturnMappedOptional() {
        when(jpaRepository.findByUuid(uuid)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<UnitOfMeasure> result = adapter.findByUuid(uuid);

        assertThat(result).contains(domain);
    }

    @Test
    void findAll_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UnitOfMeasureEntity> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(jpaRepository.findAll(ArgumentMatchers.<Specification<UnitOfMeasureEntity>>any(), eq(pageable))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domain);

        Page<UnitOfMeasure> result = adapter.findAll(Map.of("enabled", true), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByEnabled_shouldHandleTrue() {
        when(jpaRepository.findByEnabledTrue()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<UnitOfMeasure> result = adapter.findByEnabled(true);

        assertThat(result).hasSize(1);
        verify(jpaRepository).findByEnabledTrue();
    }

    @Test
    void findByEnabled_shouldHandleFalseAndNull() {
        UnitOfMeasureEntity disabled = UnitOfMeasureEntity.builder().enabled(false).build();
        UnitOfMeasureEntity enabled = UnitOfMeasureEntity.builder().enabled(true).build();
        when(jpaRepository.findAll()).thenReturn(List.of(disabled, enabled));
        when(mapper.toDomain(any(UnitOfMeasureEntity.class))).thenReturn(UnitOfMeasure.builder().build());

        List<UnitOfMeasure> falseResult = adapter.findByEnabled(false);
        List<UnitOfMeasure> nullResult = adapter.findByEnabled(null);

        assertThat(falseResult).hasSize(1);
        assertThat(nullResult).hasSize(2);
    }

    @Test
    void findByNameContaining_shouldFilterByEnabledWhenProvided() {
        UnitOfMeasureEntity disabled = UnitOfMeasureEntity.builder().enabled(false).build();
        UnitOfMeasureEntity enabledEntity = UnitOfMeasureEntity.builder().enabled(true).build();
        when(jpaRepository.findByNameContainingIgnoreCase("ca")).thenReturn(List.of(disabled, enabledEntity));
        when(mapper.toDomain(any(UnitOfMeasureEntity.class))).thenReturn(UnitOfMeasure.builder().build());

        List<UnitOfMeasure> result = adapter.findByNameContaining("ca", true);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByAbbreviationContaining_shouldFilterByEnabledWhenProvided() {
        UnitOfMeasureEntity disabled = UnitOfMeasureEntity.builder().enabled(false).build();
        UnitOfMeasureEntity enabledEntity = UnitOfMeasureEntity.builder().enabled(true).build();
        when(jpaRepository.findByAbbreviationContainingIgnoreCase("k")).thenReturn(List.of(disabled, enabledEntity));
        when(mapper.toDomain(any(UnitOfMeasureEntity.class))).thenReturn(UnitOfMeasure.builder().build());

        List<UnitOfMeasure> result = adapter.findByAbbreviationContaining("k", true);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByNameAndAbbreviationContaining_shouldReturnAllWhenEnabledIsNull() {
        UnitOfMeasureEntity disabled = UnitOfMeasureEntity.builder().enabled(false).build();
        UnitOfMeasureEntity enabledEntity = UnitOfMeasureEntity.builder().enabled(true).build();
        when(jpaRepository.findByNameContainingIgnoreCase("ca")).thenReturn(List.of(disabled, enabledEntity));
        when(jpaRepository.findByAbbreviationContainingIgnoreCase("k")).thenReturn(List.of(disabled, enabledEntity));
        when(mapper.toDomain(any(UnitOfMeasureEntity.class))).thenReturn(UnitOfMeasure.builder().build());

        List<UnitOfMeasure> byName = adapter.findByNameContaining("ca", null);
        List<UnitOfMeasure> byAbbreviation = adapter.findByAbbreviationContaining("k", null);

        assertThat(byName).hasSize(2);
        assertThat(byAbbreviation).hasSize(2);
    }

    @Test
    void existsAndCountMethods_shouldDelegate() {
        when(jpaRepository.existsByNameIgnoreCase("Caja")).thenReturn(true);
        when(jpaRepository.existsByNameIgnoreCaseAndUuidNot("Caja", uuid)).thenReturn(false);
        when(jpaRepository.existsByAbbreviationIgnoreCase("CJ")).thenReturn(true);
        when(jpaRepository.existsByAbbreviationIgnoreCaseAndUuidNot("CJ", uuid)).thenReturn(false);
        when(jpaRepository.countProductsWithUnitOfMeasure(uuid)).thenReturn(3L);

        assertThat(adapter.existsByNameIgnoreCase("Caja")).isTrue();
        assertThat(adapter.existsByNameIgnoreCaseAndUuidNot("Caja", uuid)).isFalse();
        assertThat(adapter.existsByAbbreviationIgnoreCase("CJ")).isTrue();
        assertThat(adapter.existsByAbbreviationIgnoreCaseAndUuidNot("CJ", uuid)).isFalse();
        assertThat(adapter.countProductsWithUnitOfMeasure(uuid)).isEqualTo(3L);
    }
}
