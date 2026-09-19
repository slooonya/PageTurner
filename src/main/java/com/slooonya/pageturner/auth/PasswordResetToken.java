package com.slooonya.pageturner.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import com.slooonya.pageturner.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter 
@Setter 
public final class PasswordResetToken {

    public PasswordResetToken(User user){
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.issuedDateTime = LocalDateTime.now();
        this.expiredDateTime = this.issuedDateTime.plusHours(1);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime issuedDateTime;

    @Column(nullable = false)
    private LocalDateTime expiredDateTime;

    private LocalDateTime usedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

