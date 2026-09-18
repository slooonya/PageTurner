package com.slooonya.pageturner.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private final boolean frozen;

    public CustomUserDetails(
        String username, String password,
        Collection<? extends GrantedAuthority> authorities, boolean frozen
    ) {
        super(username, password, authorities);
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }
}