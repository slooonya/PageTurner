package com.slooonya.pageturner.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.utils.EmailService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void requestReset(String email, String resetUrl) {

        User user = userRepository.findByEmail(
                email.trim().toLowerCase()
        ).orElse(null);

        if (user == null) return;

        tokenRepository.deleteAllByUser(user);

        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);

        String verificationUrl = resetUrl + "?token=" + token.getToken();

        emailService.sendPasswordResetEmail(user, verificationUrl);
    }

    public void validateToken(String tokenValue) {

        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() ->
                        new PasswordResetException("Invalid or expired password reset link."));

        if (token.getUsedDateTime() != null)
            throw new PasswordResetException("This password reset link has already been used.");

        if (token.getExpiredDateTime().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);

            throw new PasswordResetException("This password reset link has expired.");
        }
    }

    @Transactional
    public void resetPassword(String tokenValue, String password) {

        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() ->
                        new PasswordResetException("Invalid or expired password reset link."));

        if (token.getUsedDateTime() != null)
            throw new PasswordResetException("This password reset link has already been used.");

        if (token.getExpiredDateTime().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);

            throw new PasswordResetException("This password reset link has expired.");
        }

        User user = token.getUser();

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        token.setUsedDateTime(LocalDateTime.now());
        tokenRepository.save(token);
    }
}
