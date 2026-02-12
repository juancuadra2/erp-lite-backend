package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.DepartmentJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.DepartmentEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography.DepartmentEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.geography.DepartmentSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DepartmentRepositoryAdapter implements DepartmentRepository {

    private final DepartmentJpaRepository jpaRepository;
    private final DepartmentEntityMapper mapper;

    @Override
    public Department save(Department department) {
        DepartmentEntity entity = mapper.toEntity(department);
        DepartmentEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Department> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Department> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<Department> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Page<Department> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<DepartmentEntity> spec = DepartmentSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public List<Department> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeExcludingUuid(String code, UUID excludeUuid) {
        return jpaRepository.existsByCodeAndUuidNot(code, excludeUuid);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        jpaRepository.findByUuid(uuid).ifPresent(jpaRepository::delete);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
