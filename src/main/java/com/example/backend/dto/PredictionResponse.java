package com.example.backend.dto;

public class PredictionResponse {

    private String title;
    private double predictedRevenue;
    private double predictedRevenueMillions;

    public PredictionResponse(
            String title,
            double predictedRevenue,
            double predictedRevenueMillions
    ) {
        this.title = title;
        this.predictedRevenue = predictedRevenue;
        this.predictedRevenueMillions = predictedRevenueMillions;
    }

    public String getTitle() {
        return title;
    }

    public double getPredictedRevenue() {
        return predictedRevenue;
    }

    public double getPredictedRevenueMillions() {
        return predictedRevenueMillions;
    }
}