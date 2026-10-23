package com.example.backend.controller;

import com.example.backend.dto.PredictionRequest;
import com.example.backend.service.PredictionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prediction")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(
            PredictionService predictionService
    ) {
        this.predictionService = predictionService;
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(
            @RequestBody PredictionRequest request
    ) {
        try {
            return ResponseEntity.ok(
                    predictionService.predict(request)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }
}