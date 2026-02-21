package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.port.security.CompareUserUseCase;
import com.jcuadrado.erplitebackend.domain.exception.security.UserNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RequiredArgsConstructor
public class CompareUserUseCaseImpl implements CompareUserUseCase {

    private final UserRepository userRepository;

    @Override
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id));
    }

    @Override
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
