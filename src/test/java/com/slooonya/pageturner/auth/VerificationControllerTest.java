package com.slooonya.pageturner.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class VerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationTokenService verificationTokenService;

    private DefaultCsrfToken csrfToken() {
        return new DefaultCsrfToken(
            "X-CSRF-TOKEN",
            "_csrf",
            "test-token"
        );
    }

    @Test
    void verifyEmail_shouldRedirectToAuthWhenSuccessful() throws Exception {
        doNothing()
            .when(verificationTokenService)
            .verifyEmail("abc-token");

        mockMvc.perform(
                get("/verify-email")
                    .param("token", "abc-token")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl("/auth?verified=true")
            );


        verify(verificationTokenService)
            .verifyEmail("abc-token");
    }

    @Test
    void verifyEmail_shouldRedirectToErrorPageWhenInvalid() throws Exception {

        doThrow(new VerificationException("Invalid verification token"))
          .when(verificationTokenService)
          .verifyEmail("bad-token");


        when(verificationTokenService.getEmailFromToken("bad-token"))
          .thenReturn("test@example.com");


        mockMvc.perform(
                get("/verify-email")
                    .param("token", "bad-token")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl(
                    "/verification-error?error=Invalid+verification+token&email=test%40example.com"
                )
            );
    }

    @Test
    void verificationError_shouldReturnView() throws Exception {
        mockMvc.perform(
                get("/verification-error")
                  .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(
                view().name("verification-error")
            );
    }

    @Test
    void verificationPending_shouldReturnViewWithAttributes() throws Exception {
        mockMvc.perform(
                get("/verification-pending")
                    .param("email", "test@example.com")
                    .param("resent", "true")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(
                view().name("verification-pending")
            )
            .andExpect(
                model().attribute("email", "test@example.com")
            )
            .andExpect(
                model().attribute("resent", true)
            );
    }

    @Test
    void resendVerification_shouldRedirectToPending() throws Exception {
        mockMvc.perform(
                post("/resend-verification")
                    .param("email", "test@example.com")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl(
                    "/verification-pending?email=test%40example.com&resent=true"
                )
            );


        verify(verificationTokenService)
            .resendVerification(
                eq("test@example.com"),
                contains("/verify-email")
            );
    }

    @Test
    void resendVerification_shouldRedirectToError() throws Exception {
        doThrow(new VerificationException("User not found"))
          .when(verificationTokenService)
          .resendVerification(any(), any());


        mockMvc.perform(
                post("/resend-verification")
                    .param("email", "missing@example.com")
                    .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(
                redirectedUrl(
                    "/verification-error?error=User+not+found"
                )
            );
    }
}
