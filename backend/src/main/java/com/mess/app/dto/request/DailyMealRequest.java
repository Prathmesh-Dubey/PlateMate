package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMealRequest {
    private String candidateId;
    private LocalDate mealDate;
    private String mealType; // BREAKFAST, LUNCH, DINNER
    private boolean isTaken;
    private String mealPreference; // VEG, NON_VEG, SPECIAL
    private String remarks;
}