package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.PermissionAction;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPermissionsUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private UserPermissionsUseCaseImpl useCase;

    @Test
    @DisplayName("getPermissionStrings should return formatted ENTITY:ACTION strings for the user")
    void getPermissionStrings_shouldReturnFormattedStrings_whenUserHasPermissions() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("alice").active(true).failedAttempts(0).build();
        Permission p1 = Permission.builder().id(UUID.randomUUID()).entity("WAREHOUSE")
                .action(PermissionAction.READ).build();
        Permission p2 = Permission.builder().id(UUID.randomUUID()).entity("WAREHOUSE")
                .action(PermissionAction.CREATE).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(permissionRepository.findByUserId(userId)).thenReturn(List.of(p1, p2));

        List<String> result = useCase.getPermissionStrings("alice");

        assertThat(result).containsExactlyInAnyOrder("WAREHOUSE:READ", "WAREHOUSE:CREATE");
        verify(permissionRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("getPermissionStrings should return empty list when user has no permissions")
    void getPermissionStrings_shouldReturnEmptyList_whenUserHasNoPermissions() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("bob").active(true).failedAttempts(0).build();

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(permissionRepository.findByUserId(userId)).thenReturn(List.of());

        List<String> result = useCase.getPermissionStrings("bob");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPermissionStrings should return empty list when user is not found")
    void getPermissionStrings_shouldReturnEmptyList_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        List<String> result = useCase.getPermissionStrings("unknown");

        assertThat(result).isEmpty();
    }
}
