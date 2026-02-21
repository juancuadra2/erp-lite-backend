package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.application.command.security.LoginCommand;
import com.jcuadrado.erplitebackend.application.command.security.LoginResponse;
import com.jcuadrado.erplitebackend.application.command.security.LogoutCommand;
import com.jcuadrado.erplitebackend.application.command.security.RefreshTokenCommand;

public interface AuthUseCase {

    LoginResponse login(LoginCommand command);

    LoginResponse refreshToken(RefreshTokenCommand command);

    void logout(LogoutCommand command);
}
