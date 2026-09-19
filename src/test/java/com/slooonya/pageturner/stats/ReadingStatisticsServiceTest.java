package com.slooonya.pageturner.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

import com.slooonya.pageturner.logs.ReadingLog;
import com.slooonya.pageturner.logs.ReadingLogRepository;
import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReadingStatisticsServiceTest {

    @Mock
    private ReadingLogRepository readingLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReadingStatisticsService service;

    private User user;

    @BeforeEach
    void setup(){
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    void getReadingStatistics_shouldReturnEmpty_whenNoLogs(){
        when(readingLogRepository.findByUserId(1L))
            .thenReturn(List.of());

        Map<String,Object> result = service.getReadingStatistics(1L);

        assertEquals(0, result.get("bookCount"));
        assertEquals(List.of(), result.get("dates"));
    }

    @Test
    void getReadingStatistics_shouldCalculateStats(){
        ReadingLog log = new ReadingLog();

        log.setTitle("Book");
        log.setDate(LocalDate.of(2026,9,1));
        log.setTimeSpent(30);

        when(readingLogRepository.findByUserId(1L))
          .thenReturn(List.of(log));

        Map<String,Object> result = service.getReadingStatistics(1L);

        assertEquals(1, result.get("bookCount"));
        assertEquals(30L, result.get("totalReadingTime"));
    }


    @Test
    void getReadingStatisticsByDateRange_shouldRejectInvalidDates(){
        assertThrows(
            IllegalArgumentException.class,
            () ->
                service.getReadingStatisticsByDateRange(
                    1L,
                    LocalDate.of(2026,9,20),
                    LocalDate.of(2026,9,1)
                )
        );
    }

    @Test
    void getUserIdFromPrincipal_shouldReturnUserId(){
        Principal principal = () -> "test@example.com";

        when(userRepository.findByEmail("test@example.com"))
          .thenReturn(Optional.of(user));

        Long id = service.getUserIdFromPrincipal(principal);

        assertEquals(1L, id);
    }

    @Test
    void getBookProgressStats_shouldCalculateProgress(){
        Principal principal = () -> "test@example.com";

        ReadingLog log = new ReadingLog();

        log.setTitle("Book");
        log.setCurrentPage(50);
        log.setTotalPages(100);

        when(userRepository.findByEmail("test@example.com"))
          .thenReturn(Optional.of(user));

        when(readingLogRepository.findByUserId(1L))
          .thenReturn(List.of(log));

        Map<String,Object> result = service.getBookProgressStats(principal);

        Map<String,Double> progress = (Map<String,Double>) result.get("bookProgress");

        assertEquals(50.0, progress.get("Book"));
    }
}
