package com.slooonya.pageturner.stats;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadingStatisticsController.class)
class ReadingStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingStatisticsService readingStatisticsService;

    @Test
    @WithMockUser(username = "test@example.com")
    void getReadingStatistics_shouldReturnStats() throws Exception {
        Map<String,Object> stats = Map.of("bookCount", 5);

        when(readingStatisticsService.getReadingStatistics(
            any(), eq("last_month"), isNull(), isNull()
          )
        )
          .thenReturn(stats);

        mockMvc.perform(
            get("/api/reading-statistics")
                .principal(() -> "test@example.com")
                .param("period", "last_month")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookCount").value(5));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getBookProgress_shouldReturnProgress() throws Exception {
        Map<String,Object> result = Map.of("bookProgress", Map.of("Book", 50.0));

        when(readingStatisticsService.getBookProgressStats(any()))
          .thenReturn(result);

        mockMvc.perform(
            get("/api/reading-statistics/book-progress")
            .principal(() -> "test@example.com")
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.bookProgress.Book")
            .value(50.0)
        );
    }
}
