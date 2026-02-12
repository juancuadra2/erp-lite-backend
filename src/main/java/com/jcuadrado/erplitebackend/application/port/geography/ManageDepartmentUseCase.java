package com.jcuadrado.erplitebackend.application.port.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;

import java.util.UUID;

/**
 * Input port for department command operations (CQRS - Command side).
 */
public interface ManageDepartmentUseCase {

    Department create(Department department);

    Department update(UUID uuid, Department department);

    void delete(UUID uuid);

    void activate(UUID uuid);

    void deactivate(UUID uuid);
}
