package com.jcuadrado.erplitebackend.application.port.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;

import java.util.UUID;

/**
 * Input port for municipality command operations (CQRS - Command side).
 */
public interface ManageMunicipalityUseCase {

    Municipality create(Municipality municipality);

    Municipality update(UUID uuid, Municipality municipality);

    void delete(UUID uuid);

    void activate(UUID uuid);

    void deactivate(UUID uuid);
}
