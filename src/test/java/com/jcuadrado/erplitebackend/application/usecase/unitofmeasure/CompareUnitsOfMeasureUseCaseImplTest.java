package com.jcuadrado.erplitebackend.application.usecase.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareUnitsOfMeasureUseCaseImplTest {

    @Mock
    private UnitOfMeasureRepository repository;

    @InjectMocks
    private CompareUnitsOfMeasureUseCaseImpl useCase;

    @Test
    void getByUuid_shouldReturnWhenExists() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure unit = UnitOfMeasure.builder().uuid(uuid).name("Caja").build();
        when(repository.findByUuid(uuid)).thenReturn(Optional.of(unit));

        UnitOfMeasure result = useCase.getByUuid(uuid);

        assertThat(result).isEqualTo(unit);
        verify(repository).findByUuid(uuid);
    }

    @Test
    void getByUuid_shouldThrowWhenNotExists() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getByUuid(uuid))
                .isInstanceOf(UnitOfMeasureNotFoundException.class);
    }

    @Test
    void getAllActive_shouldDelegateRepository() {
        List<UnitOfMeasure> data = List.of(UnitOfMeasure.builder().name("Unidad").build());
        when(repository.findByEnabled(true)).thenReturn(data);

        List<UnitOfMeasure> result = useCase.getAllActive();

        assertThat(result).hasSize(1);
        verify(repository).findByEnabled(true);
    }

    @Test
    void findAll_shouldDelegateRepository() {
        Map<String, Object> filters = Map.of("enabled", true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<UnitOfMeasure> page = new PageImpl<>(List.of(UnitOfMeasure.builder().name("Unidad").build()));
        when(repository.findAll(filters, pageable)).thenReturn(page);

        Page<UnitOfMeasure> result = useCase.findAll(filters, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findAll(filters, pageable);
    }

    @Test
    void searchByName_shouldDelegateRepository() {
        when(repository.findByNameContaining("ca", true))
                .thenReturn(List.of(UnitOfMeasure.builder().name("Caja").build()));

        List<UnitOfMeasure> result = useCase.searchByName("ca", true);

        assertThat(result).hasSize(1);
        verify(repository).findByNameContaining("ca", true);
    }

    @Test
    void searchByAbbreviation_shouldDelegateRepository() {
        when(repository.findByAbbreviationContaining("K", true))
                .thenReturn(List.of(UnitOfMeasure.builder().abbreviation("KG").build()));

        List<UnitOfMeasure> result = useCase.searchByAbbreviation("K", true);

        assertThat(result).hasSize(1);
        verify(repository).findByAbbreviationContaining("K", true);
    }
}
