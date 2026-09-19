package com.slooonya.pageturner.violation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/violation-logs")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping
    public ResponseEntity<List<ViolationLog>> getViolationLogs() {
        return ResponseEntity.ok(
            violationService.getAllLogs()
        );
    }

    @GetMapping("/{logId}")
    public ResponseEntity<?> getLogById(@PathVariable Long logId) {
        try {
            return ResponseEntity.ok(
                violationService.getLogById(logId)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{logId}")
    public ResponseEntity<?> updateLog(
      @PathVariable Long logId, @RequestBody @Valid ViolationLogDto dto) {
        try {
            ViolationLog updatedLog = violationService.updateLog(logId, dto);

            return ResponseEntity.ok(
                Map.of(
                    "id", updatedLog.getId(),
                    "message", "Violation log updated successfully"
                )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));

        } catch (SecurityException e) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createLog(
            @RequestBody @Valid ViolationLogDto dto) {
        try {
            ViolationLog log = violationService.createLog(dto);

            return ResponseEntity.ok(
                Map.of(
                        "id", log.getId(),
                        "message", "Violation log created successfully"
                )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<?> deleteLog(@PathVariable Long logId) {
        try {
            violationService.restoreViolationLog(logId);

            return ResponseEntity.ok(
                Map.of(
                    "message",
                    "Violation log deleted and restored successfully"
                )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));

        } catch (SecurityException e) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ViolationLog>> filterViolationLogs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate endDate,
            @RequestParam(required = false) Integer minTime,
            @RequestParam(required = false) Integer maxTime,
            @RequestParam(required = false) String sort) {

        return ResponseEntity.ok(
                violationService.filterLogs(
                    query, startDate, endDate, minTime, maxTime, sort
                )
        );
    }
}
