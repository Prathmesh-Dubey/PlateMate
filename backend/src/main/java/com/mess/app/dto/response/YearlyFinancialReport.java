package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyFinancialReport {
    private int year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal totalProfit;
    private BigDecimal totalLoss;
    private BigDecimal netProfitLoss;
    private BigDecimal averageMonthlyProfit;
    private int profitableMonths;
    private int lossMonths;
    private List<MonthlyBillingResponse> monthlyData;
    private Map<String, BigDecimal> incomeBreakdown;
    private Map<String, BigDecimal> expenseBreakdown;
}