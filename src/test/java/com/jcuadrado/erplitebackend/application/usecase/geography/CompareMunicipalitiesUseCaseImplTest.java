package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.MunicipalityNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
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

@ExtendWith(MockitoExtension.class)
class CompareMunicipalitiesUseCaseImplTest {

    @Mock
    private MunicipalityRepository repository;

    @InjectMocks
    private CompareMunicipalitiesUseCaseImpl useCase;

    private Municipality sampleMunicipality1;
    private Municipality sampleMunicipality2;
    private UUID sampleUuid1;
    private UUID sampleUuid2;

    @BeforeEach
    void setUp() {
        sampleUuid1 = UUID.randomUUID();
        sampleUuid2 = UUID.randomUUID();
        Department dept = Department.builder().id(1L).code("05").name("Antioquia").build();

        sampleMunicipality1 = Municipality.builder()
                .id(1L).uuid(sampleUuid1).code("05001")
                .name("Medellín").department(dept).enabled(true).build();

        sampleMunicipality2 = Municipality.builder()
                .id(2L).uuid(sampleUuid2).code("05079")
                .name("Bello").department(dept).enabled(true).build();
    }

    // ==================== getByUuid ====================

    @Test
    void getByUuid_shouldReturnMunicipalityWhenFound() {
        when(repository.findByUuid(sampleUuid1)).thenReturn(Optional.of(sampleMunicipality1));

        Municipality result = useCase.getByUuid(sampleUuid1);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("05001");
        assertThat(result.getName()).isEqualTo("Medellín");
        verify(repository).findByUuid(sampleUuid1);
    }

    @Test
    void getByUuid_shouldThrowWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getByUuid(uuid))
                .isInstanceOf(MunicipalityNotFoundException.class);
        verify(repository).findByUuid(uuid);
    }

    // ==================== getAllActive ====================

    @Test
    void getAllActive_shouldReturnActiveMunicipalities() {
        when(repository.findAllEnabled()).thenReturn(List.of(sampleMunicipality1, sampleMunicipality2));

        List<Municipality> result = useCase.getAllActive();

        assertThat(result).hasSize(2);
        verify(repository).findAllEnabled();
    }

    @Test
    void getAllActive_shouldReturnEmptyListWhenNoneActive() {
        when(repository.findAllEnabled()).thenReturn(Collections.emptyList());

        List<Municipality> result = useCase.getAllActive();

        assertThat(result).isEmpty();
    }

    // ==================== findAll ====================

    @Test
    void findAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = new HashMap<>();
        List<Municipality> content = List.of(sampleMunicipality1, sampleMunicipality2);
        Page<Municipality> page = new PageImpl<>(content, pageable, 2);
        when(repository.findAll(filters, pageable)).thenReturn(page);

        Page<Municipality> result = useCase.findAll(filters, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(repository).findAll(filters, pageable);
    }

    @Test
    void findAll_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = new HashMap<>();
        Page<Municipality> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(repository.findAll(filters, pageable)).thenReturn(emptyPage);

        Page<Municipality> result = useCase.findAll(filters, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findAll_shouldPassDepartmentIdFilter() {
        Pageable pageable = PageRequest.of(0, 5);
        Map<String, Object> filters = new HashMap<>();
        filters.put("departmentId", 1L);
        Page<Municipality> page = new PageImpl<>(List.of(sampleMunicipality1), pageable, 1);
        when(repository.findAll(filters, pageable)).thenReturn(page);

        Page<Municipality> result = useCase.findAll(filters, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll(filters, pageable);
    }
}
