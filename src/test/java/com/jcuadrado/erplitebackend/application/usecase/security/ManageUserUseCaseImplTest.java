package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.CreateUserCommand;
import com.jcuadrado.erplitebackend.application.port.security.PasswordEncoder;
import com.jcuadrado.erplitebackend.domain.exception.security.DuplicateEmailException;
import com.jcuadrado.erplitebackend.domain.exception.security.DuplicateUsernameException;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import com.jcuadrado.erplitebackend.domain.service.security.UserDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ManageUserUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageUserUseCaseImpl(
                userRepository,
                roleRepository,
                auditLogRepository,
                userDomainService,
                passwordEncoder);
    }

    @Test
    @DisplayName("createUser should validate credentials, encode password, save user, and assign roles")
    void createUser_shouldValidateAndSave() {
        UUID roleId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        CreateUserCommand command = new CreateUserCommand(
                "john_doe", "john@example.com", "Secure@1",
                "John", "Doe", null, "12345678",
                List.of(roleId), createdBy);

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("john_doe")
                .email("john@example.com")
                .active(true)
                .failedAttempts(0)
                .build();

        Role role = Role.create("USER", "Standard user");

        doNothing().when(userDomainService).validateUsername("john_doe");
        doNothing().when(userDomainService).validateEmail("john@example.com");
        doNothing().when(userDomainService).validatePassword("Secure@1");
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secure@1")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(roleRepository.findByIds(List.of(roleId))).thenReturn(List.of(role));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = useCase.createUser(command);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john_doe");
        verify(userDomainService).validateUsername("john_doe");
        verify(userDomainService).validateEmail("john@example.com");
        verify(userDomainService).validatePassword("Secure@1");
        verify(passwordEncoder).encode("Secure@1");
        verify(userRepository).save(any(User.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("createUser should throw DuplicateUsernameException when username already exists")
    void createUser_shouldThrowDuplicateUsername_whenUsernameExists() {
        CreateUserCommand command = new CreateUserCommand(
                "john_doe", "john@example.com", "Secure@1",
                "John", "Doe", null, null, List.of(), null);

        doNothing().when(userDomainService).validateUsername("john_doe");
        doNothing().when(userDomainService).validateEmail("john@example.com");
        doNothing().when(userDomainService).validatePassword("Secure@1");
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createUser(command))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    @DisplayName("createUser should throw DuplicateEmailException when email already exists")
    void createUser_shouldThrowDuplicateEmail_whenEmailExists() {
        CreateUserCommand command = new CreateUserCommand(
                "john_doe", "john@example.com", "Secure@1",
                "John", "Doe", null, null, List.of(), null);

        doNothing().when(userDomainService).validateUsername("john_doe");
        doNothing().when(userDomainService).validateEmail("john@example.com");
        doNothing().when(userDomainService).validatePassword("Secure@1");
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createUser(command))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("deleteUser should call softDelete on the user and save it")
    void deleteUser_shouldSoftDelete() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.deleteUser(userId);

        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.isDeleted()).isTrue();
        verify(userRepository).save(user);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("unlockUser should call unlock on the user and save it")
    void unlockUser_shouldUnlockAndSave() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("admin")
                .active(false)
                .failedAttempts(5)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.unlockUser(userId);

        assertThat(user.isActive()).isTrue();
        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getLockedAt()).isNull();
        verify(userRepository).save(user);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("assignRoles should find roles by ids and complete without error")
    void assignRoles_shouldSaveRoles() {
        UUID userId = UUID.randomUUID();
        UUID roleId1 = UUID.randomUUID();
        UUID roleId2 = UUID.randomUUID();
        List<UUID> roleIds = List.of(roleId1, roleId2);

        Role role1 = Role.create("ADMIN", "Admin");
        Role role2 = Role.create("USER", "User");

        when(roleRepository.findByIds(roleIds)).thenReturn(List.of(role1, role2));

        useCase.assignRoles(userId, roleIds);

        verify(roleRepository).findByIds(roleIds);
    }
}
