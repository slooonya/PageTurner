package com.slooonya.pageturner.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.slooonya.pageturner.auth.AccountFrozenException;
import com.slooonya.pageturner.user.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpSessionSecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private SecurityService securityService;

    @Test
    void login_shouldAuthenticateAndSaveSecurityContext() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(authentication);

        when(userDetails.isFrozen())
            .thenReturn(false);


        securityService.login("test@example.com", "password", request, response);

        verify(authenticationManager)
            .authenticate(any(Authentication.class));

        verify(securityContextRepository)
            .saveContext(any(SecurityContext.class), eq(request), eq(response));

        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void login_shouldThrowException_whenUserFrozen(){
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authenticationManager.authenticate(any()))
            .thenReturn(authentication);

        when(userDetails.isFrozen())
            .thenReturn(true);

        assertThrows(AccountFrozenException.class,
            () ->
                securityService.login("test@example.com", "password", request, response)
        );


        verify(securityContextRepository, never())
          .saveContext(any(), any(), any());
    }
}