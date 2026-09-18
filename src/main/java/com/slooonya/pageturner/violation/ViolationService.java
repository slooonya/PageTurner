package com.slooonya.pageturner.violation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.slooonya.pageturner.logs.ReadingLog;
import com.slooonya.pageturner.logs.ReadingLogRepository;
import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ReadingLogRepository readingLogRepository;
    private final ViolationRepository violationRepository;
    private final UserService userService;

    public List<ViolationLog> getAllLogs() {
        return violationRepository.findAllOrderByDateDesc();
    }

    public void recordViolation(ReadingLog readingLog, String reason) {
        ViolationLog violationLog = new ViolationLog(readingLog);
        violationLog.setReason(reason.trim());
        violationRepository.save(violationLog);
    }

    public ViolationLog getLogById(Long logId) {
        User currentUser = userService.getCurrentUser();

        requireAdmin(currentUser);

        return violationRepository.findById(logId)
            .orElseThrow(() ->
                new IllegalArgumentException("Violation log not found"));
    }

    @Transactional
    public ViolationLog createLog(ViolationLogDto dto) {
        User currentUser = userService.getCurrentUser();

        ViolationLog log = new ViolationLog();

        log.setUser(currentUser);
        log.setTitle(dto.getTitle());
        log.setAuthor(dto.getAuthor());
        log.setNotes(dto.getNotes());

        return violationRepository.save(log);
    }

    @Transactional
    public ViolationLog updateLog(Long logId, ViolationLogDto dto) {
        User currentUser = userService.getCurrentUser();

        requireAdmin(currentUser);

        ViolationLog log = violationRepository.findById(logId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Violation log not found"));

        validatePageCount(log, dto);

        log.setTitle(dto.getTitle());
        log.setAuthor(dto.getAuthor());
        log.setDate(dto.getDate());
        log.setTimeSpent(dto.getTimeSpent());
        log.setNotes(dto.getNotes());

        return violationRepository.save(log);
    }

    @Transactional
    public void restoreViolationLog(Long logId) {
        User currentUser = userService.getCurrentUser();

        requireAdmin(currentUser);

        ViolationLog violationLog = violationRepository.findById(logId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Violation log not found"));

        User flaggedUser = violationLog.getUser();

        ReadingLog restoredLog = new ReadingLog();

        restoredLog.setTitle(violationLog.getTitle());
        restoredLog.setAuthor(violationLog.getAuthor());
        restoredLog.setDate(violationLog.getDate());
        restoredLog.setTimeSpent(violationLog.getTimeSpent());
        restoredLog.setNotes(violationLog.getNotes());
        restoredLog.setUser(flaggedUser);
        restoredLog.setCurrentPage(violationLog.getCurrentPage());
        restoredLog.setTotalPages(violationLog.getTotalPages());
        restoredLog.setCreatedAt(violationLog.getCreatedAt());

        flaggedUser.unflag();

        readingLogRepository.save(restoredLog);
        violationRepository.delete(violationLog);
    }


    public List<ViolationLog> filterLogs(
        String query, LocalDate startDate, LocalDate endDate,
        Integer minTime, Integer maxTime, String sort) {

        User currentUser = userService.getCurrentUser();

        requireAdmin(currentUser);

        List<ViolationLog> logs = violationRepository.findAll();

        if (query != null && !query.isBlank()) {
            String searchTerm = query.toLowerCase().trim();

            logs = logs.stream()
                .filter(log ->
                    containsIgnoreCase(log.getTitle(), searchTerm)
                        || containsIgnoreCase(log.getAuthor(), searchTerm)
                        || containsIgnoreCase(log.getNotes(), searchTerm))
                .collect(Collectors.toList());
        }

        if (startDate != null || endDate != null) {
            logs = logs.stream()
                .filter(log -> {
                    if (log.getCreatedAt() == null) return false;

                    LocalDate date = log.getCreatedAt().toLocalDate();

                    return (startDate == null || !date.isBefore(startDate))
                            && (endDate == null || !date.isAfter(endDate));
                })
                .collect(Collectors.toList());
        }

        if (minTime != null || maxTime != null) {
            logs = logs.stream()
                .filter(log ->
                    (minTime == null || log.getTimeSpent() >= minTime)
                        && (maxTime == null || log.getTimeSpent() <= maxTime))
                .collect(Collectors.toList());
        }

        return sortLogs(logs, sort);
    }

    private void requireAdmin(User user) {
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));

        if (!isAdmin)
            throw new SecurityException("Access denied");
    }

    private void validatePageCount(ViolationLog violationLog, ViolationLogDto dto) {

        if (dto.getTotalPages() == null)
            return;

        Long userId = violationLog.getUser().getId();

        List<ReadingLog> otherLogs =
            readingLogRepository
                .findByUserIdAndTitleIgnoreCaseAndAuthorIgnoreCaseAndIdNot(
                    userId, dto.getTitle().trim(),
                    dto.getAuthor().trim(), violationLog.getId());

        if (otherLogs.isEmpty())
            return;

        Integer existingTotalPages = otherLogs.stream()
            .filter(log -> log.getTotalPages() != null)
            .findFirst()
            .map(ReadingLog::getTotalPages)
            .orElse(null);

        if (existingTotalPages != null
            && !existingTotalPages.equals(dto.getTotalPages())) {

            throw new IllegalArgumentException(
                "PAGE_COUNT_MISMATCH:" + existingTotalPages
                + ":" + dto.getTotalPages());
        }
    }

    private boolean containsIgnoreCase(String value, String searchTerm) {
        return value != null && value.toLowerCase().contains(searchTerm);
    }

    private List<ViolationLog> sortLogs(List<ViolationLog> logs, String sort) {
        if (sort == null || sort.isBlank())
            return logs;

        Comparator<ViolationLog> comparator;

        switch (sort) {
            case "date-asc":
                comparator = Comparator.comparing(
                    ViolationLog::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
                break;

            case "date-desc":
                comparator = Comparator.comparing(
                    ViolationLog::getCreatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder()));
                break;

            case "title-asc":
                comparator = Comparator.comparing(
                    ViolationLog::getTitle,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;

            case "title-desc":
                comparator = Comparator.comparing(
                    ViolationLog::getTitle,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                    .reversed();
                break;

            default:
                return logs;
        }

        return logs.stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    }
}
