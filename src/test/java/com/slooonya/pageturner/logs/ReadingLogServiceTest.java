package com.slooonya.pageturner.logs;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.violation.ViolationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadingLogServiceTest {

    @Mock
    private ReadingLogRepository readingLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViolationService violationService;

    @InjectMocks
    private ReadingLogService service;

    private Principal principal;

    private User user;

    @BeforeEach
    void setup(){
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        principal = () -> "test@example.com";
    }

    @Test
    void findAll_shouldReturnUsersLogs(){
        when(userRepository.findByEmail("test@example.com"))
          .thenReturn(Optional.of(user));

        List<ReadingLog> logs = List.of(new ReadingLog());

        when(readingLogRepository
          .findByUserIdOrderByDateDescIdDesc(1L)
        )
        .thenReturn(logs);

        assertEquals(logs, service.findAll(principal));
    }

    @Test
    void create_shouldAssignCurrentUser(){
        when(userRepository.findByEmail(principal.getName()))
          .thenReturn(Optional.of(user));

        ReadingLogRequest request = new ReadingLogRequest(
    "Book", "Author", LocalDate.now(), 20, 10, 100, "notes"
        );

        when(readingLogRepository.save(any()))
          .thenAnswer(i -> i.getArgument(0));

        ReadingLog result = service.create(request, principal);

        assertEquals(user, result.getUser());
        assertTrue(result.isCurrent());
    }

    @Test
    void update_shouldModifyExistingLog(){
        when(userRepository.findByEmail("test@example.com"))
          .thenReturn(Optional.of(user));

        ReadingLog log = new ReadingLog();

        log.setId(1L);

        when(readingLogRepository.findByIdAndUserId(1L,1L))
          .thenReturn(Optional.of(log));

        when(readingLogRepository.save(any()))
          .thenAnswer(i -> i.getArgument(0));

        ReadingLogRequest request = new ReadingLogRequest(
            "Updated", "Author", LocalDate.now(), 10, 5, 100, "notes"
        );

        ReadingLog result = service.update(1L, request, principal);

        assertEquals("Updated", result.getTitle());
    }

    @Test
    void create_shouldRejectCurrentPageGreaterThanTotal(){
        ReadingLogRequest request = new ReadingLogRequest(
            "Book", "Author", LocalDate.now(), 10, 200, 100, null
        );

        ResponseStatusException exception =
            assertThrows(ResponseStatusException.class,
                () -> service.create(request,principal)
            );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deleteForAdmin_shouldRecordViolation(){
        ReadingLog log = new ReadingLog();
        log.setId(1L);

        when(readingLogRepository.findById(1L))
          .thenReturn(Optional.of(log));

        service.deleteForAdmin(1L, "Spam");

        verify(violationService)
            .recordViolation(log,"Spam");

        verify(readingLogRepository)
            .delete(log);
    }

    @Test
    void getHistory_shouldReturnHistory(){
        when(userRepository.findByEmail("test@example.com"))
          .thenReturn(Optional.of(user));

        ReadingLog oldLog = new ReadingLog();
        oldLog.setId(1L);
        oldLog.setDate(LocalDate.of(2026,1,1));

        ReadingLog newLog = new ReadingLog();
        newLog.setId(2L);
        newLog.setDate(LocalDate.of(2026,2,1));

        when(
            readingLogRepository
                .findByUserIdAndTitleIgnoreCaseAndAuthorIgnoreCase(1L, "Book", "Author")
        )
        .thenReturn(List.of(oldLog,newLog));

        List<ReadingLogHistoryDto> result =
            service.getHistory(" Book ", " Author ", 2L, principal);

        assertEquals(2, result.size());
    }
}
