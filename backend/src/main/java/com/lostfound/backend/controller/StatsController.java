package com.lostfound.backend.controller;

import com.lostfound.backend.dto.StatsResponse;
import com.lostfound.backend.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<StatsResponse> overview() {
        return ResponseEntity.ok(statsService.overview());
    }
}
