package com.slooonya.pageturner.auth;

import com.slooonya.pageturner.security.SecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private SecurityService securityService;

    private DefaultCsrfToken csrfToken() {
        return new DefaultCsrfToken(
            "X-CSRF-TOKEN",
            "_csrf",
            "test-token"
        );
    }

    @Test
    void getAuthPage_shouldReturnAuthView() throws Exception {
        mockMvc.perform(
                get("/auth")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attributeExists("user"));
    }


    @Test
    void getAuthPage_shouldAddLoginError() throws Exception {
        mockMvc.perform(
                get("/auth")
                    .param("error", "")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attribute(
                "loginError",
                "Invalid email or password."
            ));
    }


    @Test
    void getAuthPage_shouldAddLogoutMessage() throws Exception {
        mockMvc.perform(
                get("/auth")
                    .param("logout", "")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attribute(
                "logoutMessage",
                "You have been logged out successfully."
            ));
    }


    @Test
    void getAuthPage_shouldAddRegistrationMessage() throws Exception {
        mockMvc.perform(
                get("/auth")
                    .param("registered", "")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(view().name("auth"))
            .andExpect(model().attribute(
                "registrationMessage",
                "Registration successful. You can now sign in."
            ));
    }


    @Test
    void accountFrozen_shouldReturnAccountFrozenView() throws Exception {
        mockMvc.perform(get("/account-frozen"))
            .andExpect(status().isOk())
            .andExpect(view().name("account-frozen"));
    }
}