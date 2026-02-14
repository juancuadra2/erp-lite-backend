package com.jcuadrado.erplitebackend.application.usecase.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompareTaxTypesUseCaseImpl - Query Side (CQRS)
 */
@DisplayName("CompareTaxTypesUseCase - Query Tests")
@ExtendWith(MockitoExtension.class)
class CompareTaxTypesUseCaseImplTest {

    @Mock
    private TaxTypeRepository repository;

    @InjectMocks
    private CompareTaxTypesUseCaseImpl useCase;

    private TaxType sampleTaxType1;
    private TaxType sampleTaxType2;
    private UUID sampleUuid1;
    private UUID sampleUuid2;

    @BeforeEach
    void setUp() {
        sampleUuid1 = UUID.randomUUID();
        sampleUuid2 = UUID.randomUUID();

        sampleTaxType1 = TaxType.builder()
                .id(1L)
                .uuid(sampleUuid1)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .build();

        sampleTaxType2 = TaxType.builder()
                .id(2L)
                .uuid(sampleUuid2)
                .code("IVA5")
                .name("IVA 5%")
                .percentage(new BigDecimal("5.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.SALE)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("getByUuid should return tax type when found")
    void getByUuid_shouldReturnTaxTypeWhenFound() {
        // Given
        when(repository.findByUuid(sampleUuid1)).thenReturn(Optional.of(sampleTaxType1));

        // When
        TaxType result = useCase.getByUuid(sampleUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(sampleUuid1);
        assertThat(result.getCode()).isEqualTo("IVA19");
        assertThat(result.getName()).isEqualTo("IVA 19%");
        verify(repository).findByUuid(sampleUuid1);
    }

    @Test
    @DisplayName("getByUuid should throw exception when not found")
    void getByUuid_shouldThrowExceptionWhenNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        when(repository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.getByUuid(nonExistentUuid))
                .isInstanceOf(TaxTypeNotFoundException.class);

        verify(repository).findByUuid(nonExistentUuid);
    }

    @Test
    @DisplayName("getByCode should return tax type when found")
    void getByCode_shouldReturnTaxTypeWhenFound() {
        // Given
        String code = "IVA19";
        when(repository.findByCode(code)).thenReturn(Optional.of(sampleTaxType1));

        // When
        TaxType result = useCase.getByCode(code);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("IVA19");
        assertThat(result.getName()).isEqualTo("IVA 19%");
        assertThat(result.getPercentage()).isEqualByComparingTo(new BigDecimal("19.0000"));
        verify(repository).findByCode(code);
    }

    @Test
    @DisplayName("getByCode should throw exception when not found")
    void getByCode_shouldThrowExceptionWhenNotFound() {
        // Given
        String nonExistentCode = "INVALID";
        when(repository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.getByCode(nonExistentCode))
                .isInstanceOf(TaxTypeNotFoundException.class)
                .hasMessageContaining("code: " + nonExistentCode);

        verify(repository).findByCode(nonExistentCode);
    }

    @Test
    @DisplayName("getAllActive should return all active tax types")
    void getAllActive_shouldReturnAllActiveTaxTypes() {
        // Given
        List<TaxType> activeTaxTypes = Arrays.asList(sampleTaxType1, sampleTaxType2);
        when(repository.findByEnabled(true)).thenReturn(activeTaxTypes);

        // When
        List<TaxType> result = useCase.getAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(sampleTaxType1, sampleTaxType2);
        verify(repository).findByEnabled(true);
    }

    @Test
    @DisplayName("getAllActive should return empty list when no active tax types")
    void getAllActive_shouldReturnEmptyListWhenNoActiveTaxTypes() {
        // Given
        when(repository.findByEnabled(true)).thenReturn(Collections.emptyList());

        // When
        List<TaxType> result = useCase.getAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(repository).findByEnabled(true);
    }

    @Test
    @DisplayName("findAll should return paged tax types")
    void findAll_shouldReturnPagedTaxTypes() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);

        List<TaxType> taxTypes = Arrays.asList(sampleTaxType1, sampleTaxType2);
        Page<TaxType> page = new PageImpl<>(taxTypes, pageable, 2);

        when(repository.findAll(filters, pageable)).thenReturn(page);

        // When
        Page<TaxType> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(sampleTaxType1, sampleTaxType2);
        verify(repository).findAll(filters, pageable);
    }

    @Test
    @DisplayName("findAll should return empty page when no tax types")
    void findAll_shouldReturnEmptyPageWhenNoTaxTypes() {
        // Given
        Map<String, Object> filters = Collections.emptyMap();
        Pageable pageable = PageRequest.of(0, 10);
        Page<TaxType> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(filters, pageable)).thenReturn(emptyPage);

        // When
        Page<TaxType> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(repository).findAll(filters, pageable);
    }

    @Test
    @DisplayName("findAll should pass filters to repository")
    void findAll_shouldPassFiltersToRepository() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        filters.put("applicationType", "BOTH");
        filters.put("name", "IVA");
        Pageable pageable = PageRequest.of(1, 20);

        List<TaxType> taxTypes = List.of(sampleTaxType1);
        Page<TaxType> page = new PageImpl<>(taxTypes, pageable, 1);

        when(repository.findAll(filters, pageable)).thenReturn(page);

        // When
        Page<TaxType> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll(filters, pageable);
    }

    @Test
    @DisplayName("findAll should support pagination")
    void findAll_shouldSupportPagination() {
        // Given
        Map<String, Object> filters = Collections.emptyMap();
        Pageable pageable = PageRequest.of(2, 5);

        List<TaxType> taxTypes = List.of(sampleTaxType1);
        Page<TaxType> page = new PageImpl<>(taxTypes, pageable, 15);

        when(repository.findAll(filters, pageable)).thenReturn(page);

        // When
        Page<TaxType> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(3);
        verify(repository).findAll(filters, pageable);
    }
}
