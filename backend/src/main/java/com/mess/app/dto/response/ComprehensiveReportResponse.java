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
public class ComprehensiveReportResponse {
    private String reportType;
    private LocalDate reportDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String generatedBy;
    private String generatedAt;

    // Summary
    private Summary summary;

    // Candidate Section
    private CandidateReport candidateReport;

    // Attendance Section
    private AttendanceReport attendanceReport;

    // Expense Section
    private ExpenseReport expenseReport;

    // Stock Section
    private StockReport stockReport;

    // Staff Section
    private StaffReport staffReport;

    // Billing Section
    private BillingReport billingReport;

    // Party Section
    private PartyReport partyReport;

    // Menu Section
    private MenuReport menuReport;

    // Profit & Loss
    private ProfitLossReport profitLossReport;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalCandidates;
        private int activeCandidates;
        private int totalStaff;
        private int activeStaff;
        private int totalParties;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
        private BigDecimal totalStockValue;
        private BigDecimal pendingFees;
        private int totalMealsServed;
        private BigDecimal totalPartyRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateReport {
        private int totalCandidates;
        private int activeCandidates;
        private int leftCandidates;
        private List<CandidateDetail> details;
        private Map<String, Integer> monthlyJoining;
        private Map<String, Integer> monthlyLeaving;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CandidateDetail {
            private String candidateId;
            private String name;
            private String phone;
            private String status;
            private LocalDate joiningDate;
            private BigDecimal monthlyRate;
            private int daysPresent;
            private int totalMeals;
            private BigDecimal feesPaid;
            private BigDecimal pendingFees;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceReport {
        private int totalDays;
        private int totalCandidates;
        private int totalAttendance;
        private double averageAttendance;
        private Map<String, Integer> dailyAttendance;
        private Map<String, Integer> mealTypeCount;
        private List<AttendanceDetail> details;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AttendanceDetail {
            private String date;
            private int present;
            private int absent;
            private double percentage;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseReport {
        private BigDecimal totalExpenses;
        private BigDecimal averageDailyExpense;
        private Map<String, BigDecimal> categoryWise;
        private Map<String, BigDecimal> vendorWise;
        private List<ExpenseDetail> details;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ExpenseDetail {
            private String date;
            private String item;
            private String category;
            private BigDecimal amount;
            private String vendor;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockReport {
        private int totalItems;
        private int lowStockItems;
        private int overStockItems;
        private BigDecimal totalStockValue;
        private Map<String, Integer> categoryWiseCount;
        private Map<String, BigDecimal> categoryWiseValue;
        private List<StockDetail> details;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StockDetail {
            private String itemName;
            private String category;
            private BigDecimal currentStock;
            private BigDecimal minStock;
            private BigDecimal maxStock;
            private BigDecimal unitPrice;
            private BigDecimal stockValue;
            private String status; // LOW, NORMAL, OVER
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffReport {
        private int totalStaff;
        private int activeStaff;
        private BigDecimal totalSalaryPaid;
        private BigDecimal totalBonus;
        private BigDecimal totalDeductions;
        private BigDecimal totalAdvance;
        private List<StaffDetail> details;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StaffDetail {
            private String staffId;
            private String name;
            private String position;
            private BigDecimal baseSalary;
            private BigDecimal totalPaid;
            private BigDecimal bonus;
            private BigDecimal deductions;
            private BigDecimal advance;
            private String status;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingReport {
        private BigDecimal totalCollected;
        private BigDecimal totalPending;
        private int paidCandidates;
        private int pendingCandidates;
        private BigDecimal collectionPercentage;
        private List<BillingDetail> details;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BillingDetail {
            private String candidateId;
            private String name;
            private BigDecimal monthlyRate;
            private BigDecimal paid;
            private BigDecimal pending;
            private String status; // PAID, PARTIAL, PENDING
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyReport {
        private int totalParties;
        private int totalPeople;
        private BigDecimal totalRevenue;
        private BigDecimal collected;
        private BigDecimal pending;
        private Map<String, Integer> mealTypeCount;
        private List<PartyDetail> details;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PartyDetail {
            private String partyId;
            private String partyName;
            private LocalDate eventDate;
            private int people;
            private String mealType;
            private BigDecimal totalBill;
            private String paymentStatus;
            private BigDecimal paidAmount;
            private BigDecimal pendingAmount;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuReport {
        private int totalMenus;
        private int totalItems;
        private Map<String, Integer> mealTypeCount;
        private Map<String, Integer> categoryWiseCount;
        private List<MenuDetail> details;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MenuDetail {
            private String date;
            private String mealType;
            private String menuName;
            private List<String> items;
            private BigDecimal totalPrice;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitLossReport {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
        private BigDecimal profitMargin;
        private Map<String, BigDecimal> incomeBreakdown;
        private Map<String, BigDecimal> expenseBreakdown;
        private List<MonthlyProfitLoss> monthlyData;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MonthlyProfitLoss {
            private String month;
            private BigDecimal income;
            private BigDecimal expenses;
            private BigDecimal profit;
            private BigDecimal margin;
        }
    }
}