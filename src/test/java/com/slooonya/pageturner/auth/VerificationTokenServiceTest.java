package com.slooonya.pageturner.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.utils.EmailService;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationTokenService service;

    @Test
    void createVerificationForUser_shouldCreateTokenAndSendEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        service.createVerificationForUser(
            user,
            "http://localhost:8080/verify-email"
        );

        verify(verificationTokenRepository)
            .deleteAllByUser(user);

        verify(verificationTokenRepository)
            .save(any(VerificationToken.class));

        verify(emailService)
            .sendVerificationEmail(
                eq(user),
                contains("token=")
            );
    }

    @Test
    void verifyEmail_shouldMarkUserVerified() {
        User user = new User();

        VerificationToken token = VerificationToken.create(user, "abc");

        when(verificationTokenRepository.findByToken("abc"))
          .thenReturn(Optional.of(token));

        service.verifyEmail("abc");

        assertTrue(user.isVerified());

        assertNotNull(token.getConfirmedDateTime());

        verify(userRepository)
            .save(user);

        verify(verificationTokenRepository)
            .save(token);
    }

    @Test
    void verifyEmail_shouldThrowWhenTokenMissing() {
        when(verificationTokenRepository.findByToken("bad"))
          .thenReturn(Optional.empty());

        VerificationException exception = assertThrows(
            VerificationException.class,
            () -> service.verifyEmail("bad")
        );

        assertEquals("Invalid verification token", exception.getMessage());
    }

    @Test
    void verifyEmail_shouldRejectConfirmedToken() {
        User user = new User();

        VerificationToken token = VerificationToken.create(user, "abc");

        token.setConfirmedDateTime(LocalDateTime.now());

        when(verificationTokenRepository.findByToken("abc"))
          .thenReturn(Optional.of(token));

        VerificationException exception =
            assertThrows(
                VerificationException.class,
                () -> service.verifyEmail("abc")
            );

        assertEquals("Email has already been verified.", exception.getMessage());
    }

    @Test
    void resendVerification_shouldCreateNewToken() {
        User user = new User();

        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
          .thenReturn(Optional.of(user));

        service.resendVerification(
            "test@example.com",
            "http://localhost:8080/verify-email"
        );

        verify(verificationTokenRepository)
            .deleteAllByUser(user);

        verify(emailService)
            .sendVerificationEmail(
                eq(user),
                contains("token=")
            );
    }
}