package com.example.manicure_backend.controller;

import com.example.manicure_backend.DTO.AnalyticsEventRequestDTO;
import com.example.manicure_backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/events")
    public ResponseEntity<Void> registrarEvento(
            @RequestBody AnalyticsEventRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        analyticsService.registrarEvento(authHeader, dto);
        return ResponseEntity.ok().build();
    }
}
