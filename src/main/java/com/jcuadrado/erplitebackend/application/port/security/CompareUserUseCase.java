package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.domain.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CompareUserUseCase {

    User getById(UUID id);

    Page<User> list(Pageable pageable);
}
