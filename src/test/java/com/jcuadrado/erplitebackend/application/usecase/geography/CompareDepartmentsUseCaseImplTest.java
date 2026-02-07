package com.jcuadrado.erplitebackend.application.usecase.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
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
class CompareDepartmentsUseCaseImplTest {

    @Mock
    private DepartmentRepository repository;

    @InjectMocks
    private CompareDepartmentsUseCaseImpl useCase;

    private Department sampleDepartment1;
    private Department sampleDepartment2;
    private UUID sampleUuid1;
    private UUID sampleUuid2;

    @BeforeEach
    void setUp() {
        sampleUuid1 = UUID.randomUUID();
        sampleUuid2 = UUID.randomUUID();

        sampleDepartment1 = Department.builder()
                .id(1L).uuid(sampleUuid1).code("05")
                .name("Antioquia").enabled(true).build();

        sampleDepartment2 = Department.builder()
                .id(2L).uuid(sampleUuid2).code("08")
                .name("Atlántico").enabled(true).build();
    }

    // ==================== getByUuid ====================

    @Test
    void getByUuid_shouldReturnDepartmentWhenFound() {
        when(repository.findByUuid(sampleUuid1)).thenReturn(Optional.of(sampleDepartment1));

        Department result = useCase.getByUuid(sampleUuid1);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("05");
        assertThat(result.getName()).isEqualTo("Antioquia");
        verify(repository).findByUuid(sampleUuid1);
    }

    @Test
    void getByUuid_shouldThrowWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(repository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getByUuid(uuid))
                .isInstanceOf(DepartmentNotFoundException.class);
        verify(repository).findByUuid(uuid);
    }

    // ==================== getByCode ====================

    @Test
    void getByCode_shouldReturnDepartmentWhenFound() {
        when(repository.findByCode("05")).thenReturn(Optional.of(sampleDepartment1));

        Department result = useCase.getByCode("05");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Antioquia");
        verify(repository).findByCode("05");
    }

    @Test
    void getByCode_shouldThrowWhenNotFound() {
        when(repository.findByCode("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getByCode("XX"))
                .isInstanceOf(DepartmentNotFoundException.class);
        verify(repository).findByCode("XX");
    }

    // ==================== getAllActive ====================

    @Test
    void getAllActive_shouldReturnActiveDepartments() {
        when(repository.findAllEnabled()).thenReturn(List.of(sampleDepartment1, sampleDepartment2));

        List<Department> result = useCase.getAllActive();

        assertThat(result).hasSize(2);
        verify(repository).findAllEnabled();
    }

    @Test
    void getAllActive_shouldReturnEmptyListWhenNoneActive() {
        when(repository.findAllEnabled()).thenReturn(Collections.emptyList());

        List<Department> result = useCase.getAllActive();

        assertThat(result).isEmpty();
    }

    // ==================== findAll ====================

    @Test
    void findAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = new HashMap<>();
        List<Department> content = List.of(sampleDepartment1, sampleDepartment2);
        Page<Department> page = new PageImpl<>(content, pageable, 2);
        when(repository.findAll(filters, pageable)).thenReturn(page);

        Page<Department> result = useCase.findAll(filters, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(repository).findAll(filters, pageable);
    }

    @Test
    void findAll_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = new HashMap<>();
        Page<Department> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(repository.findAll(filters, pageable)).thenReturn(emptyPage);

        Page<Department> result = useCase.findAll(filters, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findAll_shouldPassFiltersToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        filters.put("search", "Anti");
        Page<Department> page = new PageImpl<>(List.of(sampleDepartment1), pageable, 1);
        when(repository.findAll(filters, pageable)).thenReturn(page);

        Page<Department> result = useCase.findAll(filters, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(repository).findAll(filters, pageable);
    }
}
