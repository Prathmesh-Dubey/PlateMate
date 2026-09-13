package com.mess.app.controller;

import com.mess.app.dto.request.BulkMealRequest;
import com.mess.app.dto.request.DailyMealRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.CandidateMealSummary;
import com.mess.app.dto.response.DailyMealResponse;
import com.mess.app.dto.response.MealSummaryResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.service.DailyMealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class DailyMealController {

    private final DailyMealService mealService;

    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<DailyMealResponse>> markMeal(
            @Valid @RequestBody DailyMealRequest request) {
        DailyMealResponse response = mealService.markMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Meal marked successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyMealResponse>> updateMeal(
            @PathVariable String id,
            @Valid @RequestBody DailyMealRequest request) {
        DailyMealResponse response = mealService.updateMeal(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Meal updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyMealResponse>> getMealById(
            @PathVariable String id) {
        DailyMealResponse response = mealService.getMealById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Meal retrieved successfully"));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<DailyMealResponse>>> getMealsByDate(
            @PathVariable String date) {
        LocalDate mealDate = LocalDate.parse(date);
        List<DailyMealResponse> responses = mealService.getMealsByDate(mealDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Meals retrieved successfully"));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<DailyMealResponse>>> getMealsByCandidate(
            @PathVariable String candidateId) {
        List<DailyMealResponse> responses = mealService.getMealsByCandidate(candidateId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Meals retrieved successfully"));
    }

    @GetMapping("/candidate/{candidateId}/date/{date}")
    public ResponseEntity<ApiResponse<List<DailyMealResponse>>> getMealsByCandidateAndDate(
            @PathVariable String candidateId,
            @PathVariable String date) {
        LocalDate mealDate = LocalDate.parse(date);
        List<DailyMealResponse> responses = mealService.getMealsByCandidateAndDate(
                candidateId, mealDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Meals retrieved successfully"));
    }

    @GetMapping("/candidate/{candidateId}/range")
    public ResponseEntity<ApiResponse<List<DailyMealResponse>>> getMealsByCandidateAndDateRange(
            @PathVariable String candidateId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<DailyMealResponse> responses = mealService.getMealsByCandidateAndDateRange(
                candidateId, start, end);
        return ResponseEntity.ok(ApiResponse.success(responses, "Meals retrieved successfully"));
    }

    @GetMapping("/type/{mealType}/date/{date}")
    public ResponseEntity<ApiResponse<List<DailyMealResponse>>> getMealsByTypeAndDate(
            @PathVariable String mealType,
            @PathVariable String date) {
        LocalDate mealDate = LocalDate.parse(date);
        List<DailyMealResponse> responses = mealService.getMealsByTypeAndDate(
                mealType, mealDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Meals retrieved successfully"));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> bulkMarkMeals(
            @Valid @RequestBody BulkMealRequest request) {
        mealService.bulkMarkMeals(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Bulk meals marked successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMeal(@PathVariable String id) {
        mealService.deleteMeal(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Meal deleted successfully"));
    }

    @GetMapping("/summary/daily")
    public ResponseEntity<ApiResponse<MealSummaryResponse>> getDailyMealSummary(
            @RequestParam(required = false) String date) {
        LocalDate summaryDate = date != null ? LocalDate.parse(date) : null;
        MealSummaryResponse summary = mealService.getDailyMealSummary(summaryDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Daily meal summary retrieved successfully"));
    }

    @GetMapping("/stats/today")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTodayMealStats() {
        Map<String, Long> stats = mealService.getTodayMealStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Today's meal stats retrieved successfully"));
    }

    @GetMapping("/candidate-summary")
    public ResponseEntity<ApiResponse<CandidateMealSummary>> getCandidateMealSummary(
            @RequestParam String candidateId,
            @RequestParam int month,
            @RequestParam int year) {
        CandidateMealSummary summary = mealService.getCandidateMealSummary(
                candidateId, month, year);
        return ResponseEntity.ok(ApiResponse.success(summary, "Candidate meal summary retrieved successfully"));
    }

    @GetMapping("/missed")
    public ResponseEntity<ApiResponse<List<Candidate>>> getCandidatesMissedMeal(
            @RequestParam String date,
            @RequestParam String mealType) {
        LocalDate mealDate = LocalDate.parse(date);
        List<Candidate> candidates = mealService.getCandidatesMissedMeal(mealDate, mealType);
        return ResponseEntity.ok(ApiResponse.success(candidates, "Candidates who missed meal retrieved successfully"));
    }
}