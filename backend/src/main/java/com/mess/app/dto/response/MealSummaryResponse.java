package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealSummaryResponse {
    private LocalDate date;
    private int totalCandidates;
    private Map<String, Integer> mealCount; // Breakfast, Lunch, Dinner counts
    private Map<String, Double> mealPercentage; // Percentage for each meal type
    private int totalMealsServed;
}