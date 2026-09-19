package com.slooonya.pageturner.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticated(){
        User user = new User();
        user.setEmail("test@example.com");

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        User result = userService.getCurrentUser();

        assertEquals(user, result);
    }

    @Test
    void getCurrentUser_shouldThrow_whenNotAuthenticated(){
        SecurityContextHolder.clearContext();

        assertThrows(SecurityException.class,
            () -> userService.getCurrentUser()
        );
    }

    @Test
    void getCurrentUser_shouldThrow_whenUserMissing(){
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "test@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.empty());

        assertThrows(SecurityException.class,
            () -> userService.getCurrentUser()
        );
    }

    @AfterEach
    void cleanup(){
        SecurityContextHolder.clearContext();
    }
}
