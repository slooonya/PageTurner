package com.slooonya.pageturner.violation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;

import com.slooonya.pageturner.logs.ReadingLog;
import com.slooonya.pageturner.logs.ReadingLogRepository;
import com.slooonya.pageturner.role.Role;
import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserService;
import com.slooonya.pageturner.utils.EmailService;

@ExtendWith(MockitoExtension.class)
class ViolationServiceTest {

    @Mock
    private ReadingLogRepository readingLogRepository;

    @Mock
    private ViolationRepository violationRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ViolationService service;

    private User admin;

    @BeforeEach
    void setup(){
        admin = new User();

        Role role = new Role();
        role.setName("ROLE_ADMIN");

        admin.setRoles(Set.of(role));
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void getLogById_shouldReturnLogForAdmin(){
        when(userService.getCurrentUser())
          .thenReturn(admin);

        ViolationLog log = new ViolationLog();
        log.setId(1L);

        when(violationRepository.findById(1L))
            .thenReturn(Optional.of(log));

        ViolationLog result = service.getLogById(1L);

        assertEquals(log,result);
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void getLogById_shouldRejectNonAdmin(){
        when(userService.getCurrentUser())
          .thenReturn(admin);

        User user = new User();
        user.setRoles(Set.of());

        when(userService.getCurrentUser())
            .thenReturn(user);

        assertThrows(
            SecurityException.class,
            () -> service.getLogById(1L)
        );
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void recordViolation_shouldSaveViolation(){
        User user = new User();
        user.setUsername("testuser");

        ReadingLog readingLog = new ReadingLog();
        readingLog.setUser(user);

        service.recordViolation(readingLog, " bad content ");

        ArgumentCaptor<ViolationLog> captor = ArgumentCaptor.forClass(ViolationLog.class);

        verify(violationRepository)
            .save(captor.capture());

        assertEquals("bad content", captor.getValue().getReason());
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void createLog_shouldCreateViolation(){
        when(userService.getCurrentUser())
          .thenReturn(admin);

        ViolationLogDto dto = new ViolationLogDto();

        dto.setTitle("Book");
        dto.setAuthor("Author");
        dto.setNotes("Notes");

        ViolationLog saved = new ViolationLog();
        saved.setId(5L);

        when(violationRepository.save(any()))
            .thenReturn(saved);

        ViolationLog result =
            service.createLog(dto);

        verify(emailService)
          .sendViolationNotificationEmail(
              eq(admin.getEmail()),
              any(ViolationLog.class)
          );

        verify(violationRepository)
            .save(any(ViolationLog.class));
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void filterLogs_shouldFilterByTitle(){
        when(userService.getCurrentUser())
          .thenReturn(admin);

        ViolationLog log = new ViolationLog();

        log.setTitle("Harry Potter");

        when(violationRepository.findAll())
            .thenReturn(List.of(log));

        List<ViolationLog> result =
            service.filterLogs(
                "harry", null, null, null, null, null
            );

        assertEquals(1, result.size());
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void filterLogs_shouldSortByTitle(){
        when(userService.getCurrentUser())
          .thenReturn(admin);

        ViolationLog a = new ViolationLog();
        a.setTitle("Apple");

        ViolationLog b = new ViolationLog();
        b.setTitle("Book");

        when(violationRepository.findAll())
            .thenReturn(
                List.of(b,a)
            );

        List<ViolationLog> result =
            service.filterLogs(
                null, null, null, null, null, "title-asc"
            );

        assertEquals("Apple", result.get(0).getTitle());
    }

    @Test
    @WithMockUser(username="admin@example.com")
    void restoreViolationLog_shouldRestoreAndDelete(){
        when(userService.getCurrentUser())
          .thenReturn(admin);

        ViolationLog violation = new ViolationLog();

        User user = new User();

        violation.setUser(user);
        violation.setTitle("Book");

        when(violationRepository.findById(1L))
            .thenReturn(Optional.of(violation));

        service.restoreViolationLog(1L);

        verify(readingLogRepository)
            .save(any(ReadingLog.class));

        verify(violationRepository)
            .delete(violation);
    }
}