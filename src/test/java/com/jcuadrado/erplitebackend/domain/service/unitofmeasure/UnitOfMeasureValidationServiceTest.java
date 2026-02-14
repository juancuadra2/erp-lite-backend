package com.jcuadrado.erplitebackend.domain.service.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.DuplicateUnitOfMeasureAbbreviationException;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.DuplicateUnitOfMeasureNameException;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureValidationServiceTest {

    @Mock
    private UnitOfMeasureRepository repository;

    @Test
    void ensureNameIsUnique_shouldThrowWhenDuplicate() {
        UnitOfMeasureValidationService service = new UnitOfMeasureValidationService(repository);
        when(repository.existsByNameIgnoreCase("Kilogramo")).thenReturn(true);

        assertThatThrownBy(() -> service.ensureNameIsUnique("Kilogramo", null))
                .isInstanceOf(DuplicateUnitOfMeasureNameException.class);
    }

    @Test
    void ensureAbbreviationIsUnique_shouldThrowWhenDuplicateOnUpdate() {
        UnitOfMeasureValidationService service = new UnitOfMeasureValidationService(repository);
        UUID uuid = UUID.randomUUID();
        when(repository.existsByAbbreviationIgnoreCaseAndUuidNot("KG", uuid)).thenReturn(true);

        assertThatThrownBy(() -> service.ensureAbbreviationIsUnique("KG", uuid))
                .isInstanceOf(DuplicateUnitOfMeasureAbbreviationException.class);
    }

    @Test
    void ensureNameIsUnique_shouldThrowWhenDuplicateOnUpdate() {
        UnitOfMeasureValidationService service = new UnitOfMeasureValidationService(repository);
        UUID uuid = UUID.randomUUID();
        when(repository.existsByNameIgnoreCaseAndUuidNot("Caja", uuid)).thenReturn(true);

        assertThatThrownBy(() -> service.ensureNameIsUnique("Caja", uuid))
                .isInstanceOf(DuplicateUnitOfMeasureNameException.class);
    }

    @Test
    void uniquenessChecks_shouldPassWhenNoDuplicates() {
        UnitOfMeasureValidationService service = new UnitOfMeasureValidationService(repository);
        when(repository.existsByNameIgnoreCase("Caja")).thenReturn(false);
        when(repository.existsByAbbreviationIgnoreCase("CJ")).thenReturn(false);

        assertThatCode(() -> {
            service.ensureNameIsUnique("Caja", null);
            service.ensureAbbreviationIsUnique("CJ", null);
        }).doesNotThrowAnyException();
    }
}
