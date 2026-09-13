package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMealResponse {
    private String id;
    private String candidateId;
    private String candidateName;
    private LocalDate mealDate;
    private String mealType;
    private boolean isTaken;
    private String mealPreference;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}