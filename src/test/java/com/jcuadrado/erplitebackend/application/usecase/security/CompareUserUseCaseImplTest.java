package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.domain.exception.security.UserNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompareUserUseCaseImpl useCase;

    @Test
    @DisplayName("getById should return the user when found")
    void getById_shouldReturnUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = useCase.getById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("admin");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("getById should throw UserNotFoundException when user is not found")
    void getById_shouldThrowNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("list should return a page of users from the repository")
    void list_shouldReturnPage() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user1 = User.builder().id(userId1).username("alice").active(true).failedAttempts(0).build();
        User user2 = User.builder().id(userId2).username("bob").active(true).failedAttempts(0).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = useCase.list(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(user1, user2);
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("list should return an empty page when no users exist")
    void list_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<User> result = useCase.list(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }
}
