package com.jcuadrado.erplitebackend.application.usecase.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ComparePaymentMethodsUseCaseImpl
 */
@ExtendWith(MockitoExtension.class)
class ComparePaymentMethodsUseCaseImplTest {

    @Mock
    private PaymentMethodRepository repository;

    @InjectMocks
    private ComparePaymentMethodsUseCaseImpl useCase;

    private PaymentMethod samplePaymentMethod1;
    private PaymentMethod samplePaymentMethod2;
    private UUID sampleUuid1;
    private UUID sampleUuid2;

    @BeforeEach
    void setUp() {
        sampleUuid1 = UUID.randomUUID();
        sampleUuid2 = UUID.randomUUID();

        samplePaymentMethod1 = PaymentMethod.builder()
                .id(1L)
                .uuid(sampleUuid1)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        samplePaymentMethod2 = PaymentMethod.builder()
                .id(2L)
                .uuid(sampleUuid2)
                .code("CC")
                .name("Tarjeta de Crédito")
                .enabled(true)
                .build();
    }

    // ==================== getByUuid Tests ====================

    @Test
    void getByUuid_shouldReturnPaymentMethodWhenFound() {
        // Given
        when(repository.findByUuid(sampleUuid1)).thenReturn(Optional.of(samplePaymentMethod1));

        // When
        PaymentMethod result = useCase.getByUuid(sampleUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(sampleUuid1);
        assertThat(result.getCode()).isEqualTo("CASH");
        verify(repository).findByUuid(sampleUuid1);
    }

    @Test
    void getByUuid_shouldThrowExceptionWhenNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        when(repository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.getByUuid(nonExistentUuid))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(repository).findByUuid(nonExistentUuid);
    }

    // ==================== getByCode Tests ====================

    @Test
    void getByCode_shouldReturnPaymentMethodWhenFound() {
        // Given
        String code = "CASH";
        when(repository.findByCode(code)).thenReturn(Optional.of(samplePaymentMethod1));

        // When
        PaymentMethod result = useCase.getByCode(code);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CASH");
        assertThat(result.getName()).isEqualTo("Efectivo");
        verify(repository).findByCode(code);
    }

    @Test
    void getByCode_shouldThrowExceptionWhenNotFound() {
        // Given
        String nonExistentCode = "INVALID";
        when(repository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.getByCode(nonExistentCode))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(repository).findByCode(nonExistentCode);
    }

    // ==================== getAllActive Tests ====================

    @Test
    void getAllActive_shouldReturnAllActivePaymentMethods() {
        // Given
        List<PaymentMethod> activePaymentMethods = Arrays.asList(
                samplePaymentMethod1,
                samplePaymentMethod2
        );
        when(repository.findAllActive()).thenReturn(activePaymentMethods);

        // When
        List<PaymentMethod> result = useCase.getAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(samplePaymentMethod1, samplePaymentMethod2);
        verify(repository).findAllActive();
    }

    @Test
    void getAllActive_shouldReturnEmptyListWhenNoActivePaymentMethods() {
        // Given
        when(repository.findAllActive()).thenReturn(Collections.emptyList());

        // When
        List<PaymentMethod> result = useCase.getAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(repository).findAllActive();
    }

    // ==================== findAll Tests ====================

    @Test
    void findAll_shouldReturnPagedPaymentMethods() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);

        List<PaymentMethod> paymentMethods = Arrays.asList(samplePaymentMethod1, samplePaymentMethod2);
        Page<PaymentMethod> page = new PageImpl<>(paymentMethods, pageable, 2);

        when(repository.findAll(filters, pageable)).thenReturn(page);

        // When
        Page<PaymentMethod> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(samplePaymentMethod1, samplePaymentMethod2);
        verify(repository).findAll(filters, pageable);
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenNoPaymentMethods() {
        // Given
        Map<String, Object> filters = Collections.emptyMap();
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentMethod> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(filters, pageable)).thenReturn(emptyPage);

        // When
        Page<PaymentMethod> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(repository).findAll(filters, pageable);
    }

    @Test
    void findAll_shouldWorkWithoutFilters() {
        // Given
        Map<String, Object> filters = Collections.emptyMap();
        Pageable pageable = PageRequest.of(0, 10);

        List<PaymentMethod> paymentMethods = Arrays.asList(samplePaymentMethod1, samplePaymentMethod2);
        Page<PaymentMethod> page = new PageImpl<>(paymentMethods, pageable, 2);

        when(repository.findAll(filters, pageable)).thenReturn(page);

        // When
        Page<PaymentMethod> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll(filters, pageable);
    }

    // ==================== searchByName Tests ====================

    @Test
    void searchByName_shouldReturnMatchingPaymentMethods() {
        // Given
        String searchTerm = "Tarjeta";
        List<PaymentMethod> searchResults = Collections.singletonList(samplePaymentMethod2);
        when(repository.findByNameContaining(searchTerm)).thenReturn(searchResults);

        // When
        List<PaymentMethod> result = useCase.searchByName(searchTerm);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Crédito");
        verify(repository).findByNameContaining(searchTerm);
    }

    @Test
    void searchByName_shouldReturnEmptyListWhenNoMatches() {
        // Given
        String searchTerm = "NonExistent";
        when(repository.findByNameContaining(searchTerm)).thenReturn(Collections.emptyList());

        // When
        List<PaymentMethod> result = useCase.searchByName(searchTerm);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(repository).findByNameContaining(searchTerm);
    }

    @Test
    void searchByName_shouldReturnMultipleMatches() {
        // Given
        String searchTerm = "Tarjeta";
        List<PaymentMethod> searchResults = Arrays.asList(samplePaymentMethod1, samplePaymentMethod2);
        when(repository.findByNameContaining(searchTerm)).thenReturn(searchResults);

        // When
        List<PaymentMethod> result = useCase.searchByName(searchTerm);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(repository).findByNameContaining(searchTerm);
    }
}
