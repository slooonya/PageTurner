package com.slooonya.pageturner.logs;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminReadingLogDto(
    Long id,
    String title,
    String author,
    LocalDate date,
    int timeSpent,
    Integer currentPage,
    Integer totalPages,
    String notes,
    LocalDateTime createdAt,
    String userName
) {
    public static AdminReadingLogDto from(ReadingLog log) {
        return new AdminReadingLogDto(
            log.getId(),
            log.getTitle(),
            log.getAuthor(),
            log.getDate(),
            log.getTimeSpent(),
            log.getCurrentPage(),
            log.getTotalPages(),
            log.getNotes(),
            log.getCreatedAt(),
            log.getUser().getUsername()
        );
    }
}
