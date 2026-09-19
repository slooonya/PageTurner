package com.slooonya.pageturner.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordResetService passwordResetService;

    private DefaultCsrfToken csrfToken() {
        return new DefaultCsrfToken(
            "X-CSRF-TOKEN",
            "_csrf",
            "test-token"
        );
    }

    @Test
    void forgotPassword_shouldReturnView() throws Exception {
        mockMvc.perform(
            get("/forgot-password")
                .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(view().name("forgot-password"));
    }

    @Test
    void resetPasswordPage_shouldReturnPasswordResetView() throws Exception {
        mockMvc.perform(
                get("/password-reset")
                    .param("token", "abc")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(view().name("password-reset"))
            .andExpect(
                model().attribute("token", "abc")
            );

        verify(passwordResetService)
            .validateToken("abc");
    }

    @Test
    void resetPasswordPage_shouldRedirectWhenTokenInvalid() throws Exception {
        doThrow(new PasswordResetException("Invalid token"))
          .when(passwordResetService)
          .validateToken("abc");

        mockMvc.perform(
          get("/password-reset")
                    .param("token", "abc")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/forgot-password")
            );
    }

    @Test
    void resetPassword_shouldResetAndRedirect() throws Exception {
        mockMvc.perform(
                post("/password-reset")
                    .param("token", "abc")
                    .param("password", "Password123")
                    .param("confirmPassword", "Password123")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/auth")
            );

        verify(passwordResetService)
            .resetPassword("abc", "Password123");
    }

    @Test
    void resetPassword_shouldRedirectWhenPasswordsMismatch() throws Exception {
        mockMvc.perform(
                post("/password-reset")
                    .param("token", "abc")
                    .param("password", "Password123")
                    .param("confirmPassword", "Different123")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/password-reset?token=abc")
            );

        verify(passwordResetService, never())
            .resetPassword(any(), any());
    }
}
