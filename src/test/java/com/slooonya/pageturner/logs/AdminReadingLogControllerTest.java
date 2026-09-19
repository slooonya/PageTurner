package com.slooonya.pageturner.logs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.slooonya.pageturner.user.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReadingLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReadingLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingLogService readingLogService;
    
    private DefaultCsrfToken csrfToken() {
        return new DefaultCsrfToken(
            "X-CSRF-TOKEN",
            "_csrf",
            "test-token"
        );
    }

    private ReadingLog createReadingLog() {
      User user = new User();
      user.setUsername("test-user");

      ReadingLog log = new ReadingLog();
      log.setId(1L);
      log.setUser(user);
      log.setTitle("Test Book");
      log.setAuthor("Test Author");
      log.setDate(LocalDate.now());
      log.setTimeSpent(30);
      log.setCurrentPage(10);
      log.setTotalPages(300);
      log.setNotes("Test notes");

      return log;
  }

    @Test
    void getAll_shouldReturnLogs() throws Exception {
        when(readingLogService.findAllForAdmin())
          .thenReturn(List.of());

        mockMvc.perform(
                get("/api/admin/reading-logs")
                  .requestAttr("_csrf", csrfToken())
            )
            .andExpect(status().isOk())
            .andExpect(
                content().json("[]")
            );
    }

    @Test
    void getOne_shouldReturnLog() throws Exception {
        ReadingLog log = createReadingLog();

        when(readingLogService.findByIdForAdmin(1L))
            .thenReturn(log);

        mockMvc.perform(
                get("/api/admin/reading-logs/1")
            )
            .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturnUpdatedLog() throws Exception {
        ReadingLog log = createReadingLog();

        when(readingLogService.updateForAdmin(eq(1L), any(ReadingLogRequest.class)))
          .thenReturn(log);

        mockMvc.perform(
                patch("/api/admin/reading-logs/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "title": "New Book",
                            "author": "Author",
                            "date": "2026-09-19",
                            "timeSpent": 30,
                            "currentPage": 10,
                            "totalPages": 300,
                            "notes": "Good chapter"
                        }
                    """)
            )
            .andExpect(status().isOk());


        verify(readingLogService)
            .updateForAdmin(eq(1L), any(ReadingLogRequest.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(
                delete("/api/admin/reading-logs/1")
                    .param("reason", "Violation")
            )
            .andExpect(
                status().isNoContent()
            );


        verify(readingLogService)
            .deleteForAdmin(1L, "Violation");
    }
}
