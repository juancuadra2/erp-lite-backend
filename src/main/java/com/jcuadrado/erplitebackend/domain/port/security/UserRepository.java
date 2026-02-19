package com.jcuadrado.erplitebackend.domain.port.security;

import com.jcuadrado.erplitebackend.domain.model.security.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
