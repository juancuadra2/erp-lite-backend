package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.domain.port.warehouse.WarehouseRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.warehouse.WarehouseEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.jpa.warehouse.WarehouseJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.warehouse.WarehouseEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.warehouse.WarehouseSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WarehouseRepositoryAdapter implements WarehouseRepository {

    private final WarehouseJpaRepository jpaRepository;
    private final WarehouseEntityMapper mapper;

    @Override
    public Warehouse save(Warehouse warehouse) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(warehouse)));
    }

    @Override
    public Optional<Warehouse> findByUuid(UUID uuid) {
        return jpaRepository.findByUuidAndDeletedAtIsNull(uuid.toString())
                .map(mapper::toDomain);
    }

    @Override
    public Page<Warehouse> findAll(Map<String, Object> filters, Pageable pageable) {
        return jpaRepository.findAll(WarehouseSpecificationUtil.buildSpecification(filters), pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findAllActive() {
        return jpaRepository.findByActiveTrueAndDeletedAtIsNull()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code) {
        return jpaRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public boolean existsByCodeIgnoreCaseAndUuidNot(String code, UUID excludeUuid) {
        return jpaRepository.existsByCodeIgnoreCaseAndUuidNot(code, excludeUuid.toString());
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameIgnoreCaseAndUuidNot(String name, UUID excludeUuid) {
        return jpaRepository.existsByNameIgnoreCaseAndUuidNot(name, excludeUuid.toString());
    }

    @Override
    public boolean existsActivePrincipalWarehouse() {
        return jpaRepository.existsByTypeAndActiveTrueAndDeletedAtIsNull(WarehouseType.PRINCIPAL);
    }

    @Override
    public boolean existsActivePrincipalWarehouseAndUuidNot(UUID excludeUuid) {
        return jpaRepository.existsByTypeAndActiveTrueAndDeletedAtIsNullAndUuidNot(
                WarehouseType.PRINCIPAL, excludeUuid.toString());
    }
}
