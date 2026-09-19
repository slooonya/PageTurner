package com.slooonya.pageturner.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.slooonya.pageturner.auth.FileStorageService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private FileStorageService fileStorageService;

    @BeforeEach
    void setup(){
        SecurityContextHolder.clearContext();
    }

    @Test
    void profile_shouldReturnProfileView() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        mockMvc.perform(
            get("/profile")
                .with(user("test@example.com"))
                .principal(() -> "test@example.com")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("profile"))
        .andExpect(model().attribute("user", user));
    }

    @Test
    void updateProfile_shouldUpdateUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("oldName");

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        mockMvc.perform(
            patch("/api/profile")
                .with(user("test@example.com"))
                .with(csrf())
                .principal(() -> "test@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username":"newName",
                        "bio":"My bio"
                    }
                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("newName"));

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldRejectInvalidUsername() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        mockMvc.perform(
            patch("/api/profile")
                .with(user("test@example.com"))
                .with(csrf())
                .principal(() -> "test@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username":"ab!",
                        "bio":"test"
                    }
                """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error")
            .value("Username must use 4-20 letters, numbers, or underscores."));
    }

    @Test
    void updateProfile_shouldRejectDuplicateUsername() throws Exception {
        User user = new User();
        user.setUsername("old");

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        when(userRepository.existsByUsername("taken"))
            .thenReturn(true);

        mockMvc.perform(
            patch("/api/profile")
              .with(user("test@example.com"))
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                {
                    "username":"taken"
                }
            """)
        )
        .andExpect(status().isConflict());
    }

    @Test
    void changePassword_shouldUpdatePassword() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-old");

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
            "oldPassword",
            "encoded-old"
        ))
        .thenReturn(true);

        when(passwordEncoder.encode("NewPassword1"))
            .thenReturn("encoded-new");

        mockMvc.perform(
            post("/api/profile/password")
                .with(user("test@example.com"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "currentPassword":"oldPassword",
                        "newPassword":"NewPassword1",
                        "confirmPassword":"NewPassword1"
                    }
                """)
        )
        .andExpect(status().isNoContent());

        verify(userRepository).save(user);
    }

    @Test
    @WithMockUser(username="test@example.com")
    void changePassword_shouldRejectWrongCurrentPassword() throws Exception {
        User user = new User();
        user.setPassword("encoded");

        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(any(), any()))
            .thenReturn(false);

        mockMvc.perform(
            post("/api/profile/password")
              .with(user("test@example.com"))
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
              {
                "currentPassword":"wrong",
                "newPassword":"Newpass1",
                "confirmPassword":"Newpass1"
              }
              """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error")
            .value("Your current password is incorrect."));
    }

    @Test
    void updateAvatar_shouldRejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "avatar", "", "image/png", new byte[0]
        );

        mockMvc.perform(
            multipart("/api/profile/avatar")
                .file(file)
                .with(user("test@example.com"))
                .with(csrf())
        )
        .andExpect(status().isBadRequest());
    }
}