package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalExpenses;
    private BigDecimal averageDailyExpense;
    private int totalExpenseCount;
    private Map<String, BigDecimal> categoryWiseExpenses;
    private Map<String, BigDecimal> vendorWiseExpenses;
    private Map<String, BigDecimal> paymentMethodWiseExpenses;
    private List<DailyExpense> dailyExpenses;
    private List<TopExpenseItem> topExpenseItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyExpense {
        private LocalDate date;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopExpenseItem {
        private String itemName;
        private BigDecimal totalAmount;
    }
}