package com.slooonya.pageturner.logs;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.slooonya.pageturner.user.User;
import com.slooonya.pageturner.user.UserRepository;
import com.slooonya.pageturner.violation.ViolationService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class ReadingLogService {

    private final ReadingLogRepository readingLogRepository;
    private final UserRepository userRepository;
    private final ViolationService violationService;

    public List<ReadingLog> findAll(Principal principal) {
        return readingLogRepository.findByUserIdOrderByDateDescIdDesc(currentUser(principal).getId());
    }

    public List<ReadingLog> findAllForAdmin() {
        return readingLogRepository.findAllByOrderByDateDescIdDesc();
    }

    public ReadingLog findById(long id, Principal principal) {
        return ownedLog(id, principal);
    }

    public ReadingLog findByIdForAdmin(long id) {
        return readingLogRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Reading log not found."
            ));
    }

    @Transactional
    public ReadingLog create(@Valid ReadingLogRequest request, Principal principal) {
        validatePages(request);

        ReadingLog log = new ReadingLog();
        log.setUser(currentUser(principal));
        apply(log, request);

        log.setCurrent(true);
        log.setPreviousVersion(null);

        return readingLogRepository.save(log);
    }

    @Transactional
    public ReadingLog update(long id, @Valid ReadingLogRequest request, Principal principal) {
        validatePages(request);

        ReadingLog currentLog = ownedLog(id, principal);

        return createUpdatedVersion(currentLog, request);
    }

    @Transactional
    public ReadingLog updateForAdmin(long id, @Valid ReadingLogRequest request) {
        validatePages(request);

        return createUpdatedVersion(findByIdForAdmin(id), request);
    }

    private ReadingLog createUpdatedVersion(ReadingLog currentLog, ReadingLogRequest request) {

        String title = request.title().trim();
        String author = request.author().trim();

        List<ReadingLog> existingVersions = readingLogRepository
            .findByUserIdAndTitleIgnoreCaseAndAuthorIgnoreCase(
                currentLog.getUser().getId(), title, author
            );

        Integer existingTotalPages = existingVersions.stream()
                .filter(log -> !log.getId().equals(currentLog.getId()))
                .map(ReadingLog::getTotalPages)
                .filter(totalPages -> totalPages != null)
                .findFirst()
                .orElse(null);

        if (existingTotalPages != null && !existingTotalPages.equals(request.totalPages())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "PAGE_COUNT_MISMATCH:" + existingTotalPages + ":" + request.totalPages()
            );
        }

        ReadingLog newVersion = new ReadingLog();

        newVersion.setUser(currentLog.getUser());
        apply(newVersion, request);

        newVersion.setPreviousVersion(currentLog);
        newVersion.setCurrent(true);

        currentLog.setCurrent(false);
        readingLogRepository.save(currentLog);

        return readingLogRepository.save(newVersion);
    }

    @Transactional
    public void delete(long id, Principal principal) {
        deleteLog(ownedLog(id, principal));
    }

    @Transactional
    public void deleteForAdmin(long id, String reason) {
        ReadingLog log = findByIdForAdmin(id);
        violationService.recordViolation(log, reason);
        deleteLog(log);
    }

    private void deleteLog(ReadingLog log) {

        readingLogRepository
            .findByPreviousVersionId(log.getId())
            .ifPresent(nextVersion -> {
                nextVersion.setPreviousVersion(log.getPreviousVersion());
                    if (log.isCurrent()) nextVersion.setCurrent(true);
                    readingLogRepository.save(nextVersion);
                });

        readingLogRepository.delete(log);
    }

    public List<ReadingLogHistoryDto> getHistory(
        String title, String author, long currentLogId, Principal principal
        ) {
            User user = currentUser(principal);

            String trimmedTitle = title.trim();
            String trimmedAuthor = author.trim();

            List<ReadingLog> history = readingLogRepository
                .findByUserIdAndTitleIgnoreCaseAndAuthorIgnoreCase(
                    user.getId(), trimmedTitle, trimmedAuthor
                );

            return history.stream().sorted(Comparator
                    .comparing(ReadingLog::getDate)
                    .reversed()
                    .thenComparing(ReadingLog::getId, Comparator.reverseOrder())
                )
                .map(log -> new ReadingLogHistoryDto(log, currentLogId))
                .toList();
        }

    private ReadingLog ownedLog(long id, Principal principal) {
        return readingLogRepository
            .findByIdAndUserId(id, currentUser(principal).getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Reading log not found."
            )
        );
    }

    private User currentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authentication required."
            );
        }

        return userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "User not found."
            )
        );
    }

    private void validatePages(ReadingLogRequest request) {
        if (request.currentPage() > request.totalPages()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Current page cannot exceed total pages."
            );
        }
    }

    private void apply(ReadingLog log, ReadingLogRequest request) {
        log.setTitle(request.title().trim());
        log.setAuthor(request.author().trim());
        log.setDate(request.date());
        log.setTimeSpent(request.timeSpent());
        log.setCurrentPage(request.currentPage());
        log.setTotalPages(request.totalPages());
        log.setNotes(request.notes() == null || request.notes().isBlank() ? null : request.notes().trim());
    }
}
