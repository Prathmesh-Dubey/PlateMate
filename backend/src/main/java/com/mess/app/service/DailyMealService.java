package com.mess.app.service;

import com.mess.app.dto.request.BulkMealRequest;
import com.mess.app.dto.request.DailyMealRequest;
import com.mess.app.dto.response.CandidateMealSummary;
import com.mess.app.dto.response.DailyMealResponse;
import com.mess.app.dto.response.MealSummaryResponse;
import com.mess.app.entity.Candidate; // ADD THIS IMPORT

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DailyMealService {

    DailyMealResponse markMeal(DailyMealRequest request);

    DailyMealResponse updateMeal(String id, DailyMealRequest request);

    DailyMealResponse getMealById(String id);

    List<DailyMealResponse> getMealsByDate(LocalDate date);

    List<DailyMealResponse> getMealsByCandidate(String candidateId);

    List<DailyMealResponse> getMealsByCandidateAndDate(String candidateId, LocalDate date);

    List<DailyMealResponse> getMealsByCandidateAndDateRange(
            String candidateId, LocalDate startDate, LocalDate endDate);

    List<DailyMealResponse> getMealsByTypeAndDate(String mealType, LocalDate date);

    void bulkMarkMeals(BulkMealRequest request);

    void deleteMeal(String id);

    MealSummaryResponse getDailyMealSummary(LocalDate date);

    Map<String, Long> getTodayMealStats();

    CandidateMealSummary getCandidateMealSummary(String candidateId, int month, int year);

    List<Candidate> getCandidatesMissedMeal(LocalDate date, String mealType); // This line had the error
}