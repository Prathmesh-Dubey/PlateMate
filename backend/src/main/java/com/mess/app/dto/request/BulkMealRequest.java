package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkMealRequest {
    private LocalDate mealDate;
    private String mealType; // BREAKFAST, LUNCH, DINNER
    private List<String> candidateIds; // List of candidate IDs who took the meal
    private String mealPreference;
}