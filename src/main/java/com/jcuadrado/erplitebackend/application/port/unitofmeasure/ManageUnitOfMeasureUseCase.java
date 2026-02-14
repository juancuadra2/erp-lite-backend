package com.jcuadrado.erplitebackend.application.port.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;

import java.util.UUID;

public interface ManageUnitOfMeasureUseCase {

    UnitOfMeasure create(UnitOfMeasure unitOfMeasure);

    UnitOfMeasure update(UUID uuid, UnitOfMeasure updates);

    UnitOfMeasure activate(UUID uuid);

    UnitOfMeasure deactivate(UUID uuid);

    void delete(UUID uuid);
}
