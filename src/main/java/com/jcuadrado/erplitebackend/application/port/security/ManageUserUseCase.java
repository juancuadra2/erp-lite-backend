package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.application.command.security.ChangePasswordCommand;
import com.jcuadrado.erplitebackend.application.command.security.CreateUserCommand;
import com.jcuadrado.erplitebackend.application.command.security.UpdateUserCommand;
import com.jcuadrado.erplitebackend.domain.model.security.User;

import java.util.List;
import java.util.UUID;

public interface ManageUserUseCase {

    User createUser(CreateUserCommand command);

    User updateUser(UUID id, UpdateUserCommand command);

    void deleteUser(UUID id);

    void unlockUser(UUID id);

    void changePassword(UUID id, ChangePasswordCommand command);

    void assignRoles(UUID userId, List<UUID> roleIds);
}
