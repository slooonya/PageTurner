package com.slooonya.pageturner.stats;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reading-statistics")
@RequiredArgsConstructor
public class ReadingStatisticsController {

    private final ReadingStatisticsService readingStatisticsService;

    @GetMapping
    public Map<String, Object> getReadingStatistics(
            Principal principal,
            @RequestParam(required = false) String period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return readingStatisticsService.getReadingStatistics(
                principal, period, startDate, endDate
        );
    }

    @GetMapping("/book-progress")
    public Map<String, Object> getBookProgress(Principal principal) {
        return readingStatisticsService.getBookProgressStats(principal);
    }
}