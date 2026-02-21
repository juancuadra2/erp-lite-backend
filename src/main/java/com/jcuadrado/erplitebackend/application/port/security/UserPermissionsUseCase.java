package com.jcuadrado.erplitebackend.application.port.security;

import java.util.List;

public interface UserPermissionsUseCase {

    List<String> getPermissionStrings(String username);
}
