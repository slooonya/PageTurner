package com.slooonya.pageturner.logs;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reading-logs")
@RequiredArgsConstructor
public class ReadingLogController {

    private final ReadingLogService readingLogService;

    @GetMapping
    public List<ReadingLog> getAll(Principal principal) { 
        return readingLogService.findAll(principal); 
    }

    @GetMapping("/{id}")
    public ReadingLog getOne(@PathVariable long id, Principal principal) { 
        return readingLogService.findById(id, principal); 
    }

    @PostMapping
    public ResponseEntity<ReadingLog> create(@Valid @RequestBody ReadingLogRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readingLogService.create(request, principal));
    }

    @PatchMapping("/{id}")
    public ReadingLog update(@PathVariable long id, @Valid @RequestBody ReadingLogRequest request, Principal principal) {
        return readingLogService.update(id, request, principal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, Principal principal) {
        readingLogService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReadingLogHistoryDto>> getHistory(
        @RequestParam String title, @RequestParam String author, 
        @RequestParam long currentLogId, Principal principal
    ) {
        List<ReadingLogHistoryDto> history = readingLogService.getHistory(
            title, author, currentLogId, principal
        );

        return ResponseEntity.ok(history);
    }
}
