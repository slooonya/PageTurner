package com.slooonya.pageturner.logs;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record ReadingLogRequest(
    @NotBlank 
    @Size(max = 200) 
    String title,

    @NotBlank 
    @Size(max = 200) 
    String author,

    @NotNull 
    @PastOrPresent 
    LocalDate date,

    @Min(0)
    int timeSpent,
        
    @NotNull 
    @Min(1) 
    Integer currentPage,
        
    @NotNull 
    @Min(1) 
    Integer totalPages,
        
    @Size(max = 65535) 
    String notes
) { }