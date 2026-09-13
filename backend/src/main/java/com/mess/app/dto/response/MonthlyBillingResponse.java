package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBillingResponse {
    private String id;
    private int month;
    private int year;
    private String monthName;
    private LocalDate monthStartDate;
    private LocalDate monthEndDate;
    private BigDecimal messFeesIncome;
    private BigDecimal partyIncome;
    private BigDecimal otherIncome;
    private BigDecimal totalIncome;
    private BigDecimal stockExpenses;
    private BigDecimal staffSalaryExpenses;
    private BigDecimal otherExpenses;
    private BigDecimal totalExpenses;
    private BigDecimal profit;
    private BigDecimal loss;
    private BigDecimal netProfitLoss;
    private BigDecimal profitMarginPercentage;
    private int totalCandidates;
    private int totalStaff;
    private int totalParties;
    private String remarks;
    private boolean isGenerated;
    private LocalDate generatedDate;
}