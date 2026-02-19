package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.port.security.CompareUserUseCase;
import com.jcuadrado.erplitebackend.domain.exception.security.UserNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CompareUserUseCaseImpl implements CompareUserUseCase {

    private final UserRepository userRepository;

    @Override
    public User getById(UUID id) {
        log.debug("Buscando usuario por id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id));
    }

    @Override
    public Page<User> list(Pageable pageable) {
        log.debug("Listando usuarios, página: {}", pageable.getPageNumber());
        return userRepository.findAll(pageable);
    }
}
