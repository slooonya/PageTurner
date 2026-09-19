package com.slooonya.pageturner.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

   @Test
    void getHomePage_shouldReturnHomeForRegularUser() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

        mockMvc.perform(
                get("/home")
                    .principal(authentication)
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
    }

    @Test
    void getHomePage_shouldRedirectAdminToAdminHome() throws Exception {
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

        mockMvc.perform(
                get("/home")
                    .principal(authentication)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin-home"));
    }

    @Test
    void getHomePage_shouldRedirectModeratorToAdminHome() throws Exception {
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                "moderator",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MODERATOR"))
            );

        mockMvc.perform(
                get("/home")
                    .principal(authentication)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin-home"));
    }

    @Test
    void showErrorPage_shouldReturnErrorView() throws Exception {
        mockMvc.perform(get("/error"))
            .andExpect(status().isOk())
            .andExpect(view().name("error"));
    }

    private DefaultCsrfToken csrfToken() {
        return new DefaultCsrfToken(
            "X-CSRF-TOKEN",
            "_csrf",
            "test-token"
        );
    }
}