package com.slooonya.pageturner.auth;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.role.Role;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.role.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private VerificationTokenService verificationTokenService;

    @InjectMocks
    private AuthService authService;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
            authService,
            "adminRegistrationCode",
            "88888888"
        );
    }

    @Test
    void register_shouldCreateRegularUser() throws Exception {
        User user = new User();
        user.setEmail(" TEST@Example.COM ");
        user.setUsername(" testuser ");
        user.setPassword("Password123");

        Role userRole = new Role("ROLE_USER");

        when(userRepository.existsByEmail("test@example.com"))
            .thenReturn(false);

        when(userRepository.existsByUsername("testuser"))
            .thenReturn(false);

        when(passwordEncoder.encode("Password123"))
            .thenReturn("encoded-password");

        when(roleRepository.findByName("ROLE_USER"))
            .thenReturn(Optional.of(userRole));

        when(userRepository.save(user))
            .thenReturn(user);

        User result = authService.register(user, null, null);

        assertSame(user, result);

        assertEquals("test@example.com", user.getEmail());
        assertEquals("testuser", user.getUsername());
        assertEquals("encoded-password", user.getPassword());
        assertNull(user.getConfirmPassword());
        assertNull(user.getAvatarFileName());

        assertTrue(user.getRoles().contains(userRole));

        verify(userRepository).save(user);

        verify(verificationTokenService)
            .createVerificationForUser(
                user,
                "http://localhost:8080/verify-email"
            );
    }

    @Test
    void register_shouldThrowExceptionWhenUsernameAlreadyExists() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("Password123");

        when(userRepository.existsByEmail("test@example.com"))
            .thenReturn(false);

        when(userRepository.existsByUsername("testuser"))
            .thenReturn(true);

        AuthService.RegistrationException exception =
            assertThrows(
                AuthService.RegistrationException.class,
                () -> authService.register(user, null, null)
            );

        assertEquals(
            "This username is already taken.",
            exception.getMessage()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {
        User user = new User();
        user.setEmail(" Test@Example.com ");
        user.setUsername("testuser");
        user.setPassword("Password123");

        when(userRepository.existsByEmail("test@example.com"))
            .thenReturn(true);

        AuthService.RegistrationException exception =
            assertThrows(
                AuthService.RegistrationException.class,
                () -> authService.register(user, null, null)
            );

        assertEquals(
            "This email address is already registered.",
            exception.getMessage()
        );

        verify(userRepository, never()).save(any());
        verify(verificationTokenService, never())
            .createVerificationForUser(any(), any());
    }

    @Test
    void register_shouldUseExistingUserRole() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("Password123");

        Role role = new Role("ROLE_USER");

        when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

        when(userRepository.existsByUsername(anyString()))
            .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
            .thenReturn("encoded");

        when(roleRepository.findByName("ROLE_USER"))
            .thenReturn(Optional.of(role));

        when(userRepository.save(user))
            .thenReturn(user);

        authService.register(user, null, null);

        verify(roleRepository, never())
            .save(any(Role.class));

        assertTrue(user.getRoles().contains(role));
    }

    @Test
    void register_shouldCreateUserRoleWhenItDoesNotExist() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("Password123");

        Role role = new Role("ROLE_USER");

        when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

        when(userRepository.existsByUsername(anyString()))
            .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
            .thenReturn("encoded");

        when(roleRepository.findByName("ROLE_USER"))
            .thenReturn(Optional.empty());

        when(roleRepository.save(any(Role.class)))
            .thenReturn(role);

        when(userRepository.save(user))
            .thenReturn(user);

        authService.register(user, null, null);

        verify(roleRepository)
            .save(any(Role.class));

        assertTrue(user.getRoles().contains(role));
    }

    @Test
    void register_shouldAssignAdminRoleWithValidAdminCode() throws Exception {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setUsername("admin");
        user.setPassword("Password123");

        Role adminRole = new Role("ROLE_ADMIN");

        when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

        when(userRepository.existsByUsername(anyString()))
            .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
            .thenReturn("encoded");

        when(roleRepository.findByName("ROLE_ADMIN"))
            .thenReturn(Optional.of(adminRole));

        when(userRepository.save(user))
            .thenReturn(user);

        authService.register(user, null, "88888888");

        assertTrue(user.getRoles().contains(adminRole));

        verify(roleRepository)
            .findByName("ROLE_ADMIN");
    }

    @Test
    void register_shouldStoreAvatarWhenProvided() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("Password123");

        MockMultipartFile avatar = new MockMultipartFile(
            "avatar",
            "avatar.png",
            "image/png",
            "image-data".getBytes()
        );

        Role role = new Role("ROLE_USER");

        when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

        when(userRepository.existsByUsername(anyString()))
            .thenReturn(false);

        when(fileStorageService.storeAvatar(avatar, "testuser"))
            .thenReturn("testuser-avatar.png");

        when(passwordEncoder.encode(anyString()))
            .thenReturn("encoded");

        when(roleRepository.findByName("ROLE_USER"))
            .thenReturn(Optional.of(role));

        when(userRepository.save(user))
            .thenReturn(user);

        authService.register(user, avatar, null);

        assertEquals(
            "testuser-avatar.png",
            user.getAvatarFileName()
        );

        verify(fileStorageService)
            .storeAvatar(avatar, "testuser");
    }

    @Test
    void register_shouldPropagateIOExceptionFromAvatarStorage() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("Password123");

        MockMultipartFile avatar = new MockMultipartFile(
            "avatar",
            "avatar.png",
            "image/png",
            "image-data".getBytes()
        );

        when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

        when(userRepository.existsByUsername(anyString()))
            .thenReturn(false);

        when(fileStorageService.storeAvatar(avatar, "testuser"))
            .thenThrow(new IOException("Storage failed"));

        assertThrows(
            IOException.class,
            () -> authService.register(user, avatar, null)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void validateRegistration_shouldRejectMissingPasswordConfirmation() {
        User user = new User();
        user.setPassword("Password123");
        user.setConfirmPassword("");

        BindingResult result = mock(BindingResult.class);

        authService.validateRegistration(user, null, result);

        verify(result).rejectValue(
            eq("confirmPassword"),
            eq("required"),
            eq("Password confirmation is required.")
        );
    }


    @Test
    void validateRegistration_shouldRejectMismatchedPasswords() {
        User user = new User();
        user.setPassword("Password123");
        user.setConfirmPassword("Different123");

        BindingResult result = mock(BindingResult.class);

        authService.validateRegistration(user, null, result);

        verify(result).rejectValue(
            eq("confirmPassword"),
            eq("match"),
            eq("Passwords must match.")
        );
    }

    @Test
    void validateRegistration_shouldRejectDuplicateEmail() {
        User user = new User();
        user.setEmail(" TEST@example.com ");
        user.setPassword("Password123");
        user.setConfirmPassword("Password123");

        when(userRepository.existsByEmail("test@example.com"))
            .thenReturn(true);

        BindingResult result = mock(BindingResult.class);

        authService.validateRegistration(user, null, result);

        verify(result).rejectValue(
            eq("email"),
            eq("duplicate"),
            eq("This email address is already registered.")
        );
    }

    @Test
    void validateRegistration_shouldRejectDuplicateUsername() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("Password123");
        user.setConfirmPassword("Password123");

        when(userRepository.existsByUsername("testuser"))
            .thenReturn(true);

        BindingResult result = mock(BindingResult.class);

        authService.validateRegistration(user, null, result);

        verify(result).rejectValue(
            eq("username"),
            eq("duplicate"),
            eq("This username is already taken.")
        );
    }
}