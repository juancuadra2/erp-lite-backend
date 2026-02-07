package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.documenttypes.DocumentTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DocumentTypeEntityMapper
 */
class DocumentTypeEntityMapperTest {

    private DocumentTypeEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DocumentTypeEntityMapper.class);
    }

    // ==================== toEntity Tests ====================

    @Test
    void toEntity_shouldMapDomainModelToEntity() {
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
        DocumentTypeEntity result = mapper.toEntity(documentType);

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
    void toEntity_shouldMapAllAuditFields() {
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
        DocumentTypeEntity result = mapper.toEntity(documentType);

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
    void toEntity_shouldHandleNullOptionalFields() {
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
        DocumentTypeEntity result = mapper.toEntity(documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toEntity_shouldMapInactiveDocument() {
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
        DocumentTypeEntity result = mapper.toEntity(documentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getActive()).isFalse();
    }

    // ==================== toDomain Tests ====================

    @Test
    void toDomain_shouldMapEntityToDomainModel() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        DocumentTypeEntity entity = DocumentTypeEntity.builder()
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
        DocumentType result = mapper.toDomain(entity);

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
    void toDomain_shouldMapAllAuditFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime deletedAt = LocalDateTime.now();

        DocumentTypeEntity entity = DocumentTypeEntity.builder()
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
        DocumentType result = mapper.toDomain(entity);

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
    void toDomain_shouldHandleNullOptionalFields() {
        // Given
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(3L)
                .uuid(UUID.randomUUID())
                .code("CE")
                .name("Cédula de Extranjería")
                .description(null)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        DocumentType result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toDomain_shouldMapInactiveEntity() {
        // Given
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(4L)
                .uuid(UUID.randomUUID())
                .code("PA")
                .name("Pasaporte")
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        DocumentType result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getActive()).isFalse();
    }

    // ==================== Bidirectional Mapping Tests ====================

    @Test
    void shouldMapBidirectionallyWithoutDataLoss() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        DocumentType original = DocumentType.builder()
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
        DocumentTypeEntity entity = mapper.toEntity(original);
        DocumentType result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getUuid()).isEqualTo(original.getUuid());
        assertThat(result.getCode()).isEqualTo(original.getCode());
        assertThat(result.getName()).isEqualTo(original.getName());
        assertThat(result.getDescription()).isEqualTo(original.getDescription());
        assertThat(result.getActive()).isEqualTo(original.getActive());
        assertThat(result.getCreatedBy()).isEqualTo(original.getCreatedBy());
        assertThat(result.getUpdatedBy()).isEqualTo(original.getUpdatedBy());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }
}
