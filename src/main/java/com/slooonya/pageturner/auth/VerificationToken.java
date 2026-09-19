package com.slooonya.pageturner.auth;

import java.time.LocalDateTime;

import com.slooonya.pageturner.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter 
@Setter
@NoArgsConstructor
public class VerificationToken {

    public static VerificationToken create(User user, String token) {
        VerificationToken verificationToken = new VerificationToken();

        verificationToken.token = token;
        verificationToken.user = user;
        verificationToken.issuedDateTime = LocalDateTime.now();
        verificationToken.expiredDateTime =
                verificationToken.issuedDateTime.plusDays(1);

        return verificationToken;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    private LocalDateTime expiredDateTime;
    private LocalDateTime issuedDateTime;
    private LocalDateTime confirmedDateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
