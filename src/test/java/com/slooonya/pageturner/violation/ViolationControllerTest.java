package com.slooonya.pageturner.violation;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ViolationController.class)
@AutoConfigureMockMvc
class ViolationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ViolationService violationService;

    @Test
    @WithMockUser(username="admin@example.com")
    void getViolationLogs_shouldReturnLogs() throws Exception {
        List<ViolationLog> logs = List.of(new ViolationLog());

        when(violationService.getAllLogs())
            .thenReturn(logs);

        mockMvc.perform(
            get("/api/violation-logs")
        )
        .andExpect(status().isOk());

        verify(violationService)
            .getAllLogs();
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void getLogById_shouldReturnLog() throws Exception {
        ViolationLog log = new ViolationLog();
        log.setId(1L);

        when(violationService.getLogById(1L))
            .thenReturn(log);

        mockMvc.perform(
            get("/api/violation-logs/1")
        )
        .andExpect(status().isOk());

        verify(violationService)
            .getLogById(1L);
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void getLogById_shouldReturn404_whenMissing() throws Exception {
        when(violationService.getLogById(1L))
            .thenThrow(
                new IllegalArgumentException("Violation log not found"
                )
            );

        mockMvc.perform(
            get("/api/violation-logs/1")
        )
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.error")
            .value("Violation log not found")
        );
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void getLogById_shouldReturn403_whenNotAdmin() throws Exception {
        when(violationService.getLogById(1L))
            .thenThrow(new SecurityException("Access denied"));

        mockMvc.perform(
            get("/api/violation-logs/1")
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void deleteLog_shouldRestoreLog() throws Exception {
        mockMvc.perform(
            delete("/api/violation-logs/1")
                .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.message")
            .value(
              "Violation log deleted and restored successfully"
            )
        );

        verify(violationService)
            .restoreViolationLog(1L);
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void deleteLog_shouldReturn404_whenMissing() throws Exception {
        doThrow(new IllegalArgumentException("Violation log not found"))
          .when(violationService)
          .restoreViolationLog(1L);

        mockMvc.perform(
            delete("/api/violation-logs/1")
                .with(csrf())
        )
        .andExpect(status().isNotFound());
    }
}