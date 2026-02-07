package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.MunicipalityJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.MunicipalityEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography.MunicipalityEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.geography.MunicipalitySpecificationUtil;
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
public class MunicipalityRepositoryAdapter implements MunicipalityRepository {

    private final MunicipalityJpaRepository jpaRepository;
    private final MunicipalityEntityMapper mapper;

    @Override
    public Municipality save(Municipality municipality) {
        MunicipalityEntity entity = mapper.toEntity(municipality);
        MunicipalityEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Municipality> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Municipality> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<Municipality> findByCodeAndDepartmentId(String code, Long departmentId) {
        return jpaRepository.findByCodeAndDepartmentId(code, departmentId).map(mapper::toDomain);
    }

    @Override
    public Page<Municipality> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<MunicipalityEntity> spec = MunicipalitySpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public List<Municipality> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCodeAndDepartmentId(String code, Long departmentId) {
        return jpaRepository.existsByCodeAndDepartmentId(code, departmentId);
    }

    @Override
    public boolean existsByCodeAndDepartmentIdExcludingUuid(String code, Long departmentId, UUID excludeUuid) {
        return jpaRepository.existsByCodeAndDepartmentIdAndUuidNot(code, departmentId, excludeUuid);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        jpaRepository.findByUuid(uuid).ifPresent(jpaRepository::delete);
    }

    @Override
    public long countByDepartmentId(Long departmentId) {
        return jpaRepository.countByDepartmentId(departmentId);
    }
}
