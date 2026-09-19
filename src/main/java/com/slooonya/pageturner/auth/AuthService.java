package com.slooonya.pageturner.auth;

import java.io.IOException;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.slooonya.pageturner.role.Role;
import com.slooonya.pageturner.role.RoleRepository;
import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final RoleRepository roleRepository;
    private final VerificationTokenService verificationTokenService;

    @Value("${app.admin.registration-code}")
    private String adminRegistrationCode;

    @Transactional
    public User register(User user, MultipartFile avatar, String adminCode) throws IOException {
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setUsername(user.getUsername().trim());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RegistrationException("This email address is already registered.");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RegistrationException("This username is already taken.");
        }

        String avatarFileName = null;
        if (avatar != null && !avatar.isEmpty()) {
            avatarFileName = fileStorageService.storeAvatar(avatar, user.getUsername());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setConfirmPassword(null);
        user.setAvatarFileName(avatarFileName);

        Role role;

        if (adminCode != null && !adminCode.isBlank() && adminCode.equals(adminRegistrationCode)) {
            if (!adminCode.equals(adminRegistrationCode))
                throw new RegistrationException("Invalid administrator registration code.");

            role = getOrCreateRole("ROLE_ADMIN");
        } else {
            role = getOrCreateRole("ROLE_USER");
        }

        user.setRoles(new HashSet<>());
        user.getRoles().add(role);

        User savedUser = userRepository.save(user);

        verificationTokenService.createVerificationForUser(savedUser, "http://localhost:8080/verify-email");       

        return savedUser;
    }

    public void validateRegistration(User user, MultipartFile avatar, BindingResult result) {
        if (user.getConfirmPassword() == null || user.getConfirmPassword().isBlank()) {
            result.rejectValue("confirmPassword", "required", "Password confirmation is required.");
        } else if (!user.isPasswordMatch()) {
            result.rejectValue("confirmPassword", "match", "Passwords must match.");
        }
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail().trim().toLowerCase())) {
            result.rejectValue("email", "duplicate", "This email address is already registered.");
        }
        if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername().trim())) {
            result.rejectValue("username", "duplicate", "This username is already taken.");
        }
        if (avatar != null && !avatar.isEmpty()) {
            try {
                fileStorageService.validateFile(avatar);
            } catch (IOException exception) {
                result.rejectValue("avatarFile", "invalid", exception.getMessage());
            }
        }
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    public static class RegistrationException extends RuntimeException {
        public RegistrationException(String message) {
            super(message);
        }

        public RegistrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

     public class EmailVerificationException extends RuntimeException {
        public EmailVerificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
