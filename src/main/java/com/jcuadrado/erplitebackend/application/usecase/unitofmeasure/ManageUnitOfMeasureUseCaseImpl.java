package com.jcuadrado.erplitebackend.application.usecase.unitofmeasure;

import com.jcuadrado.erplitebackend.application.port.unitofmeasure.ManageUnitOfMeasureUseCase;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;
import com.jcuadrado.erplitebackend.domain.service.unitofmeasure.UnitOfMeasureDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ManageUnitOfMeasureUseCaseImpl implements ManageUnitOfMeasureUseCase {

    private static final Long SYSTEM_USER_ID = 0L;

    private final UnitOfMeasureRepository repository;
    private final UnitOfMeasureDomainService domainService;

    @Override
    public UnitOfMeasure create(UnitOfMeasure unitOfMeasure) {
        domainService.prepareForCreation(unitOfMeasure);

        if (unitOfMeasure.getUuid() == null) {
            unitOfMeasure.setUuid(UUID.randomUUID());
        }

        if (unitOfMeasure.getEnabled() == null) {
            unitOfMeasure.setEnabled(true);
        }

        unitOfMeasure.setCreatedAt(LocalDateTime.now());
        unitOfMeasure.setCreatedBy(SYSTEM_USER_ID);

        return repository.save(unitOfMeasure);
    }

    @Override
    public UnitOfMeasure update(UUID uuid, UnitOfMeasure updates) {
        UnitOfMeasure existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new UnitOfMeasureNotFoundException(uuid));

        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }

        if (updates.getAbbreviation() != null) {
            existing.setAbbreviation(updates.getAbbreviation());
        }

        domainService.prepareForUpdate(existing, uuid);

        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(SYSTEM_USER_ID);

        return repository.save(existing);
    }

    @Override
    public UnitOfMeasure activate(UUID uuid) {
        UnitOfMeasure existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new UnitOfMeasureNotFoundException(uuid));

        existing.activate(SYSTEM_USER_ID);
        return repository.save(existing);
    }

    @Override
    public UnitOfMeasure deactivate(UUID uuid) {
        UnitOfMeasure existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new UnitOfMeasureNotFoundException(uuid));

        long usageCount = repository.countProductsWithUnitOfMeasure(uuid);
        domainService.ensureCanBeDeactivated(usageCount);

        existing.deactivate(SYSTEM_USER_ID);
        return repository.save(existing);
    }

    @Override
    public void delete(UUID uuid) {
        deactivate(uuid);
    }
}
