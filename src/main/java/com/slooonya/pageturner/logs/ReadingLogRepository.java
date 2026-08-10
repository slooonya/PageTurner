package com.slooonya.pageturner.logs;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {
    List<ReadingLog> findByUserIdOrderByDateDescIdDesc(long userId);
    Optional<ReadingLog> findByIdAndUserId(long id, long userId);
    List<ReadingLog> findByUserIdAndTitleIgnoreCaseAndAuthorIgnoreCase(
        long userId, String title, String author
    );
    List<ReadingLog> findByUserIdAndTitleIgnoreCaseAndAuthorIgnoreCaseAndIdNot(
        long userId, String title, String author, long id
    );
    List<ReadingLog> findByUserIdAndTitleIgnoreCaseAndIsCurrent(
        long userId, String title, boolean isCurrent
    );
    Optional<ReadingLog> findByPreviousVersionId(long previousVersionId);
}
