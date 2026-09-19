package com.slooonya.pageturner.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.utils.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Transactional
    public void createVerificationForUser(User user, String verificationURL) {
        verificationTokenRepository.deleteAllByUser(user);

        String tokenValue = UUID.randomUUID().toString();

        VerificationToken token = VerificationToken.create(user, tokenValue);

        verificationTokenRepository.save(token);

        String url = verificationURL + "?token=" + tokenValue;

        emailService.sendVerificationEmail(user, url);
    }

    @Transactional
    public void verifyEmail(String tokenValue) {
        VerificationToken token = verificationTokenRepository.findByToken(tokenValue)
            .orElseThrow(() ->
                new VerificationException("Invalid verification token")
            );

        validateToken(token);

        User user = token.getUser();
        user.setVerified(true);

        token.setConfirmedDateTime(LocalDateTime.now());

        userRepository.save(user);
        verificationTokenRepository.save(token);
    }

    private void validateToken(VerificationToken token) {
        if (token.getExpiredDateTime().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(token);
            throw new VerificationException("Token expired. Please request a new verification email.");
        }

        if (token.getConfirmedDateTime() != null)
            throw new VerificationException("Email has already been verified.");
    }

    public String getEmailFromToken(String tokenValue) {
        return verificationTokenRepository.findByToken(tokenValue)
            .map(token -> token.getUser().getEmail())
            .orElse(null);
    }

    @Transactional
    public void resendVerification(String email, String verificationUrl) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new VerificationException("User not found"));

        if (user.isVerified()) {
            throw new VerificationException(
                "Email is already verified."
            );
        }

        createVerificationForUser(user, verificationUrl);
    }
}
