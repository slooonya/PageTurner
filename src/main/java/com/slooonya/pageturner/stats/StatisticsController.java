package com.slooonya.pageturner.stats;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StatisticsController {

    @GetMapping("/stats")
    public String getStatsPage() {
        return "stats";
    }
  
}