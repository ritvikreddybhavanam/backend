package com.example.backend.service;

import com.example.backend.dto.PredictionRequest;
import com.example.backend.dto.PredictionResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String pythonApiUrl =
            "http://127.0.0.1:8000/predict";

    public PredictionResponse predict(PredictionRequest request) {

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("title", request.getTitle());
        requestBody.put("budget", request.getBudget());
        requestBody.put("runtime", request.getRuntime());
        requestBody.put("release_year", request.getReleaseYear());
        requestBody.put("release_month", request.getReleaseMonth());
        requestBody.put(
                "original_language",
                request.getOriginalLanguage()
        );
        requestBody.put(
                "main_country",
                request.getMainCountry()
        );
        requestBody.put("genres", request.getGenres());

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody);

            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            pythonApiUrl,
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                    );

            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null) {
                throw new RuntimeException("Received empty response from prediction service");
            }

            String title = (String) responseBody.getOrDefault("title", request.getTitle());

            Number rev = (Number) responseBody.get("predicted_revenue");
            Number revMillions = (Number) responseBody.get("predicted_revenue_millions");

            double predictedRevenue = rev != null ? rev.doubleValue() : 0.0;
            double predictedRevenueMillions = revMillions != null ? revMillions.doubleValue() : 0.0;

            return new PredictionResponse(
                    title,
                    predictedRevenue,
                    predictedRevenueMillions
            );

        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Prediction service is unavailable. Please ensure the Python FastAPI server is running on port 8000.",
                    e
            );
        }
    }
}