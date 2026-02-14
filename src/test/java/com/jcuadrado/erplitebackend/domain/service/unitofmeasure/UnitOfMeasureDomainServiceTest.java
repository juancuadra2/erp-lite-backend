package com.jcuadrado.erplitebackend.domain.service.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureInUseException;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureDomainServiceTest {

    @Mock
    private UnitOfMeasureValidationService validationService;

    private final UnitOfMeasureValidator validator = new UnitOfMeasureValidator();

    @Test
    void prepareForCreation_shouldNormalizeAndValidateUniqueness() {
        UnitOfMeasureDomainService service = new UnitOfMeasureDomainService(validator, validationService);
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .name("Kilogramo")
                .abbreviation("kg")
                .build();

        service.prepareForCreation(unit);

        assertThat(unit.getAbbreviation()).isEqualTo("KG");
        verify(validationService).ensureNameIsUnique("Kilogramo", null);
        verify(validationService).ensureAbbreviationIsUnique("KG", null);
    }

    @Test
    void prepareForUpdate_shouldValidateWithExcludedUuid() {
        UnitOfMeasureDomainService service = new UnitOfMeasureDomainService(validator, validationService);
        UUID uuid = UUID.randomUUID();
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .name("Caja")
                .abbreviation("cj")
                .build();

        service.prepareForUpdate(unit, uuid);

        verify(validationService).ensureNameIsUnique("Caja", uuid);
        verify(validationService).ensureAbbreviationIsUnique("CJ", uuid);
    }

    @Test
    void ensureCanBeDeactivated_shouldThrowWhenUsageExists() {
        UnitOfMeasureDomainService service = new UnitOfMeasureDomainService(validator, validationService);

        assertThatThrownBy(() -> service.ensureCanBeDeactivated(2L))
                .isInstanceOf(UnitOfMeasureInUseException.class);
    }

    @Test
    void ensureCanBeDeactivated_shouldPassWhenNoUsage() {
        UnitOfMeasureDomainService service = new UnitOfMeasureDomainService(validator, validationService);

        assertThatCode(() -> service.ensureCanBeDeactivated(0L)).doesNotThrowAnyException();
    }

    @Test
    void normalizeAbbreviation_shouldReturnNullWhenInputIsNull() {
        UnitOfMeasureDomainService service = new UnitOfMeasureDomainService(validator, validationService);

        assertThat(service.normalizeAbbreviation(null)).isNull();
    }
}
