package com.slooonya.pageturner.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.slooonya.pageturner.auth.AccountFrozenException;
import com.slooonya.pageturner.user.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository;

    public void login(String email, String password, HttpServletRequest request, HttpServletResponse response) {
        Authentication authenticated = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(email, password));

        CustomUserDetails userDetails = (CustomUserDetails) authenticated.getPrincipal();

        if (userDetails.isFrozen()) throw new AccountFrozenException();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
