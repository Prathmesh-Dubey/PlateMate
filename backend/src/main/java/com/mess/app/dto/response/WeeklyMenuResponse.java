package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMenuResponse {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Map<LocalDate, DayMenu> dailyMenu;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayMenu {
        private LocalDate date;
        private String dayName;
        private List<MenuResponse> meals;
    }
}