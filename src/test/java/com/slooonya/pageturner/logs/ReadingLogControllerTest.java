package com.slooonya.pageturner.logs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.slooonya.pageturner.user.User;

@WebMvcTest(ReadingLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReadingLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingLogService readingLogService;

    @Test
    void getAll_shouldReturnLogs() throws Exception {
        User user = new User();
        user.setUsername("test");

        ReadingLog log = new ReadingLog();
        log.setId(1L);
        log.setUser(user);
        log.setTitle("Book");

        when(readingLogService.findAll(any()))
            .thenReturn(List.of(log));

        mockMvc.perform(
                get("/api/reading-logs")
                    .principal(
                        new UsernamePasswordAuthenticationToken(
                            "test@example.com",
                            null
                        )
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("Book"));
    }

    @Test
    void getOne_shouldReturnLog() throws Exception {
        ReadingLog log = new ReadingLog();
        log.setId(1L);
        log.setTitle("Book");

        when(readingLogService.findById(eq(1L), any()))
          .thenReturn(log);

        mockMvc.perform(
                get("/api/reading-logs/1")
                    .principal(
                        new UsernamePasswordAuthenticationToken(
                            "test@example.com",
                            null
                        )
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Book"));
    }

    @Test
    void create_shouldReturnCreatedLog() throws Exception {
        ReadingLog log = new ReadingLog();
        log.setId(1L);

        when(readingLogService.create(any(), any()))
          .thenReturn(log);

        mockMvc.perform(
                post("/api/reading-logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(
                        new UsernamePasswordAuthenticationToken(
                            "test@example.com",
                            null
                        )
                    )
                    .content("""
                        {
                            "title":"Book",
                            "author":"Author",
                            "date":"2026-09-19",
                            "timeSpent":30,
                            "currentPage":10,
                            "totalPages":300,
                            "notes":"notes"
                        }
                    """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));

        verify(readingLogService)
            .create(any(), any());
    }

    @Test
    void update_shouldReturnUpdatedLog() throws Exception {
        ReadingLog log = new ReadingLog();
        log.setId(1L);

        when(readingLogService.update(eq(1L), any(), any()))
          .thenReturn(log);

        mockMvc.perform(
                patch("/api/reading-logs/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(
                        new UsernamePasswordAuthenticationToken(
                            "test@example.com",
                            null
                        )
                    )
                    .content("""
                        {
                            "title":"Updated",
                            "author":"Author",
                            "date":"2026-09-19",
                            "timeSpent":20,
                            "currentPage":5,
                            "totalPages":100,
                            "notes":"changed"
                        }
                    """)
            )
            .andExpect(status().isOk());


        verify(readingLogService)
            .update(eq(1L), any(), any());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(
                delete("/api/reading-logs/1")
                    .principal(
                        new UsernamePasswordAuthenticationToken(
                            "test@example.com",
                            null
                        )
                    )
            )
            .andExpect(status().isNoContent());


        verify(readingLogService)
            .delete(eq(1L), any());
    }

    @Test
    void getHistory_shouldReturnHistory() throws Exception {
        when(readingLogService.getHistory(any(), any(), eq(1L), any()))
          .thenReturn(List.of());

        mockMvc.perform(
                get("/api/reading-logs/history")
                    .param("title", "Book")
                    .param("author", "Author")
                    .param("currentLogId", "1")
                    .principal(
                        new UsernamePasswordAuthenticationToken(
                            "test@example.com",
                            null
                        )
                    )
            )
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }
}
