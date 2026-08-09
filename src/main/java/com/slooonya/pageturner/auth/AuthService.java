package com.slooonya.pageturner.auth;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public void register(User user, MultipartFile avatar) throws IOException {
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
        userRepository.save(user);
    }

    public static class RegistrationException extends RuntimeException {
        public RegistrationException(String message) {
            super(message);
        }
    }
}
