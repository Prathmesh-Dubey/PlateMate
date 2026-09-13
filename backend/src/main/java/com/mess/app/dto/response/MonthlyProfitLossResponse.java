package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Live monthly Profit & Loss for one calendar year, calculated directly from
 * the transactional tables (fee_collections, parties, college_billing,
 * expenses, staff_payments). Nothing here comes from the monthly_billing
 * snapshot table.
 *
 * Formula (per month):
 *   revenue  = messFees + partyIncome + collegeBillingIncome
 *   expenses = dayToDayExpenses + staffSalary
 *   profit   = revenue - expenses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyProfitLossResponse {

    private int year;
    private String basis;
    private List<MonthEntry> monthlyData;
    private Totals totals;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthEntry {
        private int month;
        private String monthName;
        private String shortName;

        // revenue side
        private BigDecimal messFees;
        private BigDecimal partyIncome;
        private BigDecimal collegeBillingIncome;
        private BigDecimal income;      // total revenue (kept as "income" for backward compatibility)

        // expense side
        private BigDecimal dayToDayExpenses;
        private BigDecimal staffSalary;
        private BigDecimal expenses;    // total expenses

        private BigDecimal profit;      // income - expenses
        private BigDecimal profitMargin; // profit / income * 100 (0 when no income)
        private boolean hasData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Totals {
        private BigDecimal messFees;
        private BigDecimal partyIncome;
        private BigDecimal collegeBillingIncome;
        private BigDecimal income;
        private BigDecimal dayToDayExpenses;
        private BigDecimal staffSalary;
        private BigDecimal expenses;
        private BigDecimal profit;
        private int profitableMonths;
        private int lossMonths;
        private int monthsWithData;
    }
}
