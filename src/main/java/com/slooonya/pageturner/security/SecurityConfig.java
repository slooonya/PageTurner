package com.slooonya.pageturner.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.slooonya.pageturner.user.CustomUserDetails;
import com.slooonya.pageturner.user.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return email -> users.findByEmail(email.trim().toLowerCase())
            .map(user -> new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .toList(),
                user.isFrozen(),
                user.isVerified()
            ))
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found."));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
        HttpSessionSecurityContextRepository repository) throws Exception {

        return http
            .securityContext(context ->
                context.securityContextRepository(repository))

            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/auth",
                    "/sign-in",
                    "/sign-up",
                    "/account-frozen",
                    "/forgot-password",
                    "/verify-email",
                    "/verification-pending",
                    "/verification-error",
                    "/resend-verification",
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                .requestMatchers(
                    "/admin-home",
                    "/api/admin/**",
                    "/violations",
                    "/user-list",
                    "/admin-profile",
                    "/user-list/**"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
                )

                .formLogin(AbstractHttpConfigurer::disable)

                .logout(logout -> logout
                    .logoutSuccessUrl("/auth?logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .permitAll()
                )

            .build();
    }
}
