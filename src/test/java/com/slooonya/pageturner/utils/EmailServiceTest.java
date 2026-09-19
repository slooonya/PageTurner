package com.slooonya.pageturner.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.slooonya.pageturner.user.User;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setup(){
        ReflectionTestUtils.setField(emailService, "senderEmail", "test@example.com");

        when(mailSender.createMimeMessage())
            .thenReturn(mimeMessage);
    }

    @Test
    void sendVerificationEmail_shouldSendEmail(){
        User user = new User();
        user.setEmail("user@test.com");
        user.setUsername("john");

        boolean result = emailService.sendVerificationEmail(user, "http://verify");

        assertTrue(result);

        verify(mailSender)
            .send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_shouldSendEmail(){
        User user = new User();
        user.setEmail("user@test.com");
        user.setUsername("john");

        emailService.sendPasswordResetEmail(user, "http://reset");

        verify(mailSender)
            .send(mimeMessage);
    }

    @Test
    void sendAccountFrozenEmail_shouldSendEmail(){
        User user = new User();
        user.setEmail("user@test.com");
        user.setUsername("john");

        assertTrue(emailService.sendAccountFrozenEmail(user));

        verify(mailSender)
            .send(mimeMessage);
    }
  }
