package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.CreateDocumentTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.DocumentTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.UpdateDocumentTypeRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DocumentTypeDtoMapper
 */
class DocumentTypeDtoMapperTest {

    private DocumentTypeDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DocumentTypeDtoMapper.class);
    }

    // ==================== toDomain from CreateDocumentTypeRequestDto Tests ====================

    @Test
    void toDomain_shouldMapCreateRequestDtoToDomainModel() {
        // Given
        CreateDocumentTypeRequestDto dto = CreateDocumentTypeRequestDto.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento tributario para empresas")
                .build();

        // When
        DocumentType result = mapper.toDomain(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("NIT");
        assertThat(result.getName()).isEqualTo("Número de Identificación Tributaria");
        assertThat(result.getDescription()).isEqualTo("Documento tributario para empresas");
    }

    @Test
    void toDomain_shouldIgnoreSystemFieldsFromCreateRequestDto() {
        // Given
        CreateDocumentTypeRequestDto dto = CreateDocumentTypeRequestDto.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .build();

        // When
        DocumentType result = mapper.toDomain(dto);

        // Then
        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getActive()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullDescriptionInCreateRequestDto() {
        // Given
        CreateDocumentTypeRequestDto dto = CreateDocumentTypeRequestDto.builder()
                .code("CE")
                .name("Cédula de Extranjería")
                .description(null)
                .build();

        // When
        DocumentType result = mapper.toDomain(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CE");
        assertThat(result.getName()).isEqualTo("Cédula de Extranjería");
        assertThat(result.getDescription()).isNull();
    }

    // ==================== toDomain from UpdateDocumentTypeRequestDto Tests ====================

    @Test
    void toDomain_shouldMapUpdateRequestDtoToDomainModel() {
        // Given
        UpdateDocumentTypeRequestDto dto = UpdateDocumentTypeRequestDto.builder()
                .code("PA")
                .name("Pasaporte")
                .description("Documento internacional")
                .build();

        // When
        DocumentType result = mapper.toDomain(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("PA");
        assertThat(result.getName()).isEqualTo("Pasaporte");
        assertThat(result.getDescription()).isEqualTo("Documento internacional");
    }

    @Test
    void toDomain_shouldIgnoreSystemFieldsFromUpdateRequestDto() {
        // Given
        UpdateDocumentTypeRequestDto dto = UpdateDocumentTypeRequestDto.builder()
                .code("TI")
                .name("Tarjeta de Identidad")
                .build();

        // When
        DocumentType result = mapper.toDomain(dto);

        // Then
        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getActive()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullDescriptionInUpdateRequestDto() {
        // Given
        UpdateDocumentTypeRequestDto dto = UpdateDocumentTypeRequestDto.builder()
                .code("RC")
                .name("Registro Civil")
                .description(null)
                .build();

        // When
        DocumentType result = mapper.toDomain(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("RC");
        assertThat(result.getName()).isEqualTo("Registro Civil");
        assertThat(result.getDescription()).isNull();
    }

    // ==================== toResponseDto Tests ====================

    @Test
    void toResponseDto_shouldMapDomainModelToResponseDto() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        DocumentType documentType = DocumentType.builder()
                .id(1L)
                .uuid(uuid)
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento tributario")
                .active(true)
                .createdBy(100L)
                .updatedBy(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        DocumentTypeResponseDto result = mapper.toResponseDto(documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getCode()).isEqualTo("NIT");
        assertThat(result.getName()).isEqualTo("Número de Identificación Tributaria");
        assertThat(result.getDescription()).isEqualTo("Documento tributario");
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(100L);
        assertThat(result.getUpdatedBy()).isEqualTo(101L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toResponseDto_shouldMapAllAuditFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime deletedAt = LocalDateTime.now();

        DocumentType documentType = DocumentType.builder()
                .id(2L)
                .uuid(uuid)
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(false)
                .createdBy(200L)
                .updatedBy(201L)
                .deletedBy(202L)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();

        // When
        DocumentTypeResponseDto result = mapper.toResponseDto(documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedBy()).isEqualTo(200L);
        assertThat(result.getUpdatedBy()).isEqualTo(201L);
        assertThat(result.getDeletedBy()).isEqualTo(202L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    void toResponseDto_shouldHandleNullOptionalFields() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .id(3L)
                .uuid(UUID.randomUUID())
                .code("CE")
                .name("Cédula de Extranjería")
                .description(null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        DocumentTypeResponseDto result = mapper.toResponseDto(documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toResponseDto_shouldHandleInactiveDocumentType() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .id(4L)
                .uuid(UUID.randomUUID())
                .code("PA")
                .name("Pasaporte")
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        DocumentTypeResponseDto result = mapper.toResponseDto(documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getActive()).isFalse();
    }
}
