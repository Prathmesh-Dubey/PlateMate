package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Financial summary shown in the dashboard boxes at the top of the Billing page.
 *
 * Every figure is computed live from the underlying tables (no snapshots, no
 * hard-coded values). See {@code BillingServiceImpl#getBillingSummary} for the
 * exact calculation of each box.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingSummaryResponse {

    /** MONTH, YEAR or ALL */
    private String scope;
    private Integer month;
    private Integer year;
    private String periodLabel;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime generatedAt;

    private CollegeBillingBox collegeBilling;
    private StaffSalaryBox staffSalary;
    private ExpenseBox expenses;
    private TodayExpenseBox todayExpense;
    private CombinedExpenseBox combinedExpenses;
    private MessFeesBox messFees;
    private OutstandingBox outstanding;
    private PartyIncomeBox partyIncome;
    private ProfitLossBox profitLoss;

    /** Box 1 - amounts billed to colleges (college_billing table). This is revenue receivable, not an expense. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollegeBillingBox {
        private BigDecimal billableAmount;
        private BigDecimal amountReceived;
        private BigDecimal outstandingBalance;
        private long recordCount;
    }

    /** Box 2 - staff payroll (staff + staff_payments tables). */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffSalaryBox {
        /** Sum of base salary of all ACTIVE staff = monthly payroll commitment. */
        private BigDecimal monthlyPayroll;
        private long activeStaffCount;
        /** Salary actually disbursed (staff_payments.amount) inside the selected period. */
        private BigDecimal paidInPeriod;
        private long paymentCount;
    }

    /** Box 3 - expenses table (all categories, both "Day-to-day" and "Total Expenses" pages write here). */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseBox {
        private BigDecimal total;
        private long count;
        private Map<String, BigDecimal> categoryBreakdown;
    }

    /** Box 4 - expenses dated today (server date). */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayExpenseBox {
        private LocalDate date;
        private BigDecimal amount;
        private long count;
    }

    /** Box 5 - expenses + staff salary paid, each counted exactly once. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CombinedExpenseBox {
        private BigDecimal total;
        private BigDecimal expenses;
        private BigDecimal staffSalaryPaid;
        private String formula;
    }

    /** Box 6 - mess fee collections (fee_collections table, isPaid = true). */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessFeesBox {
        private BigDecimal collected;
        private long collectionCount;
        /** Only for MONTH scope: sum of monthlyRate of ACTIVE candidates. */
        private BigDecimal expectedMonthly;
        /** Only for MONTH scope: max(expectedMonthly - collected, 0). */
        private BigDecimal pending;
        /** Only for MONTH scope: collected / expectedMonthly * 100. */
        private BigDecimal collectionRate;
    }

    /** Box 7 - receivables still outstanding (college outstanding + party pending). */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutstandingBox {
        private BigDecimal total;
        private BigDecimal collegeOutstanding;
        private BigDecimal partyPending;
        /** (college received + party received) / (college billed + party billed) * 100 */
        private BigDecimal collectionRate;
        private BigDecimal totalBilledReceivables;
        private BigDecimal totalReceivedReceivables;
    }

    /** Box 8 - party / catering (parties table). */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyIncomeBox {
        private BigDecimal totalBilled;
        private BigDecimal received;
        private BigDecimal pending;
        private long partyCount;
    }

    /** Net result for the selected period using the same formula as the monthly P&L chart. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitLossBox {
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netProfitLoss;
        private BigDecimal messFeesRevenue;
        private BigDecimal partyIncome;
        private BigDecimal collegeBillingIncome;
        private BigDecimal expenses;
        private BigDecimal staffSalary;
    }
}
