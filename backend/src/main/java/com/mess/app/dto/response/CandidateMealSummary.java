package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateMealSummary {
    private String candidateId;
    private String candidateName;
    private int totalDays;
    private int breakfastCount;
    private int lunchCount;
    private int dinnerCount;
    private int totalMeals;
    private double averageMealsPerDay;
}