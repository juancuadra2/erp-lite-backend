package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.CreateUnitOfMeasureRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UnitOfMeasureResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UpdateUnitOfMeasureRequestDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfMeasureDtoMapperTest {

    private final UnitOfMeasureDtoMapper mapper = Mappers.getMapper(UnitOfMeasureDtoMapper.class);

    @Test
    void toCreateCommand_shouldMapFields() {
        CreateUnitOfMeasureRequestDto dto = new CreateUnitOfMeasureRequestDto();
        dto.setName("Caja");
        dto.setAbbreviation("CJ");

        UnitOfMeasure model = mapper.toDomain(dto);

        assertThat(model.getName()).isEqualTo("Caja");
        assertThat(model.getAbbreviation()).isEqualTo("CJ");
    }

    @Test
    void toUpdateCommand_shouldMapFields() {
        UpdateUnitOfMeasureRequestDto dto = new UpdateUnitOfMeasureRequestDto();
        dto.setName("Kilogramo");
        dto.setAbbreviation("KG");

        UnitOfMeasure model = mapper.toDomain(dto);

        assertThat(model.getName()).isEqualTo("Kilogramo");
        assertThat(model.getAbbreviation()).isEqualTo("KG");
    }

    @Test
    void toResponse_shouldMapFields() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UnitOfMeasure model = UnitOfMeasure.builder()
                .uuid(uuid)
                .name("Metro")
                .abbreviation("M")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UnitOfMeasureResponseDto response = mapper.toResponseDto(model);

        assertThat(response.uuid()).isEqualTo(uuid);
        assertThat(response.name()).isEqualTo("Metro");
        assertThat(response.abbreviation()).isEqualTo("M");
        assertThat(response.enabled()).isTrue();
    }

    @Test
    void toDomain_shouldIgnoreAuditFields() {
        CreateUnitOfMeasureRequestDto dto = new CreateUnitOfMeasureRequestDto();
        dto.setName("Litro");
        dto.setAbbreviation("L");

        UnitOfMeasure model = mapper.toDomain(dto);

        assertThat(model.getId()).isNull();
        assertThat(model.getUuid()).isNull();
        assertThat(model.getCreatedAt()).isNull();
        assertThat(model.getUpdatedAt()).isNull();
        assertThat(model.getDeletedAt()).isNull();
    }

    @Test
    void mapper_shouldReturnNullWhenInputIsNull() {
        assertThat(mapper.toDomain((CreateUnitOfMeasureRequestDto) null)).isNull();
        assertThat(mapper.toDomain((UpdateUnitOfMeasureRequestDto) null)).isNull();
        assertThat(mapper.toResponseDto(null)).isNull();
    }
}
