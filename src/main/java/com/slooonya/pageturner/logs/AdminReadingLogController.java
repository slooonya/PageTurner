package com.slooonya.pageturner.logs;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reading-logs")
@RequiredArgsConstructor
public class AdminReadingLogController {

    private final ReadingLogService readingLogService;

    @GetMapping
    public List<AdminReadingLogDto> getAll() {
        return readingLogService.findAllForAdmin().stream()
            .map(AdminReadingLogDto::from)
            .toList();
    }

    @GetMapping("/{id}")
    public AdminReadingLogDto getOne(@PathVariable long id) {
        return AdminReadingLogDto.from(readingLogService.findByIdForAdmin(id));
    }

    @PatchMapping("/{id}")
    public AdminReadingLogDto update(
        @PathVariable long id,
        @Valid @RequestBody ReadingLogRequest request
    ) {
        return AdminReadingLogDto.from(readingLogService.updateForAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable long id,
        @RequestParam String reason
    ) {
        readingLogService.deleteForAdmin(id, reason);
        return ResponseEntity.noContent().build();
    }
}
