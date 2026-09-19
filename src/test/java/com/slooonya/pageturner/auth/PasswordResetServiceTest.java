package com.slooonya.pageturner.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.utils.EmailService;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void requestReset_shouldCreateTokenAndSendEmail() {
        User user = new User();
        user.setEmail("test@example.com");


        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));


        passwordResetService.requestReset(
            "TEST@example.com",
            "http://localhost:8080/password-reset"
        );


        verify(tokenRepository)
            .deleteAllByUser(user);

        verify(tokenRepository)
            .save(any(PasswordResetToken.class));

        verify(emailService)
            .sendPasswordResetEmail(
                eq(user),
                contains("token=")
            );
    }

    @Test
    void requestReset_shouldDoNothingWhenEmailDoesNotExist() {
        when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.empty());


        passwordResetService.requestReset(
            "missing@test.com",
            "url"
        );


        verify(tokenRepository, never())
            .save(any());

        verify(emailService, never())
            .sendPasswordResetEmail(any(), any());
    }

    @Test
    void validateToken_shouldThrowForMissingToken() {

        when(tokenRepository.findByToken("bad"))
            .thenReturn(Optional.empty());

        PasswordResetException exception =
            assertThrows(
                PasswordResetException.class,
                () -> passwordResetService.validateToken("bad")
            );

        assertEquals(
            "Invalid or expired password reset link.",
            exception.getMessage()
        );
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndMarkTokenUsed() {
        User user = new User();

        PasswordResetToken token = new PasswordResetToken(user);

        when(tokenRepository.findByToken("abc"))
            .thenReturn(Optional.of(token));

        when(passwordEncoder.encode("newPassword"))
            .thenReturn("encoded");

        passwordResetService.resetPassword(
            "abc",
            "newPassword"
        );

        assertEquals("encoded", user.getPassword());

        assertNotNull(token.getUsedDateTime());

        verify(userRepository)
            .save(user);

        verify(tokenRepository)
            .save(token);
    }
}