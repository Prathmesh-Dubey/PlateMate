package com.mess.app.controller;

import com.mess.app.dto.request.BulkFeeCollectionRequest;
import com.mess.app.dto.request.FeeCollectionRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.BillingSummaryResponse;
import com.mess.app.dto.response.FeeCollectionResponse;
import com.mess.app.dto.response.FeeSummaryResponse;
import com.mess.app.dto.response.MonthlyBillingResponse;
import com.mess.app.dto.response.MonthlyProfitLossResponse;
import com.mess.app.dto.response.YearlyFinancialReport;
import com.mess.app.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class BillingController {

    private final BillingService billingService;

    // ==================== FEE COLLECTION ====================

    @PostMapping("/fees")
    public ResponseEntity<ApiResponse<FeeCollectionResponse>> collectFee(
            @Valid @RequestBody FeeCollectionRequest request) {
        FeeCollectionResponse response = billingService.collectFee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Fee collected successfully"));
    }

    @PostMapping("/fees/bulk")
    public ResponseEntity<ApiResponse<Void>> bulkCollectFees(
            @Valid @RequestBody BulkFeeCollectionRequest request) {
        billingService.bulkCollectFees(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Bulk fees collected successfully"));
    }

    @GetMapping("/fees/{id}")
    public ResponseEntity<ApiResponse<FeeCollectionResponse>> getFeeCollectionById(
            @PathVariable String id) {
        FeeCollectionResponse response = billingService.getFeeCollectionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee collection retrieved successfully"));
    }

    @GetMapping("/fees/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<FeeCollectionResponse>>> getFeesByCandidate(
            @PathVariable String candidateId) {
        List<FeeCollectionResponse> responses = billingService.getFeesByCandidate(candidateId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Fee collections retrieved successfully"));
    }

    @GetMapping("/fees/month")
    public ResponseEntity<ApiResponse<List<FeeCollectionResponse>>> getFeesByMonth(
            @RequestParam int month,
            @RequestParam int year) {
        List<FeeCollectionResponse> responses = billingService.getFeesByMonth(month, year);
        return ResponseEntity.ok(ApiResponse.success(responses, "Fee collections retrieved successfully"));
    }

    @GetMapping("/fees/summary")
    public ResponseEntity<ApiResponse<FeeSummaryResponse>> getFeeSummary(
            @RequestParam int month,
            @RequestParam int year) {
        FeeSummaryResponse response = billingService.getFeeSummary(month, year);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee summary retrieved successfully"));
    }

    @DeleteMapping("/fees/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeeCollection(@PathVariable String id) {
        billingService.deleteFeeCollection(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee collection deleted successfully"));
    }

    // ==================== MONTHLY BILLING SNAPSHOTS ====================

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<MonthlyBillingResponse>> generateMonthlyBilling(
            @RequestParam int month,
            @RequestParam int year) {
        MonthlyBillingResponse response = billingService.generateMonthlyBilling(month, year);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Monthly billing generated successfully"));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyBillingResponse>> getMonthlyBilling(
            @RequestParam int month,
            @RequestParam int year) {
        MonthlyBillingResponse response = billingService.getMonthlyBilling(month, year);
        return ResponseEntity.ok(ApiResponse.success(response, "Monthly billing retrieved successfully"));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<ApiResponse<List<MonthlyBillingResponse>>> getYearlyBilling(
            @PathVariable int year) {
        List<MonthlyBillingResponse> responses = billingService.getYearlyBilling(year);
        return ResponseEntity.ok(ApiResponse.success(responses, "Yearly billing retrieved successfully"));
    }

    @GetMapping("/report/yearly/{year}")
    public ResponseEntity<ApiResponse<YearlyFinancialReport>> getYearlyFinancialReport(
            @PathVariable int year) {
        YearlyFinancialReport report = billingService.getYearlyFinancialReport(year);
        return ResponseEntity.ok(ApiResponse.success(report, "Yearly financial report retrieved successfully"));
    }

    // ==================== LIVE DASHBOARD & P&L ====================

    /**
     * Summary boxes for the Billing page. All values are computed live from the
     * database (college_billing, staff, staff_payments, expenses, fee_collections, parties).
     *
     * scope = MONTH (default, uses month + year), YEAR (uses year) or ALL.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<BillingSummaryResponse>> getBillingSummary(
            @RequestParam(defaultValue = "MONTH") String scope,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        BillingSummaryResponse response = billingService.getBillingSummary(scope, month, year);
        return ResponseEntity.ok(ApiResponse.success(response, "Billing summary retrieved successfully"));
    }

    /** Live monthly profit and loss for the given year (revenue vs expenses per month). */
    @GetMapping("/profit-loss-chart")
    public ResponseEntity<ApiResponse<MonthlyProfitLossResponse>> getProfitLossChart(
            @RequestParam(required = false) Integer year) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        MonthlyProfitLossResponse chartData = billingService.getMonthlyProfitLoss(resolvedYear);
        return ResponseEntity.ok(ApiResponse.success(chartData, "Profit/Loss chart data retrieved successfully"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardData() {
        Map<String, Object> dashboard = billingService.getDashboardData();
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard data retrieved successfully"));
    }

    @GetMapping("/income-expense-comparison")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIncomeExpenseComparison(
            @RequestParam int year) {
        Map<String, Object> comparison = billingService.getIncomeExpenseComparison(year);
        return ResponseEntity.ok(ApiResponse.success(comparison, "Income/Expense comparison retrieved successfully"));
    }

    @GetMapping("/current-month-profit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentMonthProfit() {
        Map<String, Object> response = new HashMap<>();
        response.put("profit", billingService.getCurrentMonthProfit());
        response.put("month", LocalDate.now().getMonthValue());
        response.put("year", LocalDate.now().getYear());
        return ResponseEntity.ok(ApiResponse.success(response, "Current month profit retrieved successfully"));
    }

    @GetMapping("/current-year-profit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentYearProfit() {
        Map<String, Object> response = new HashMap<>();
        response.put("profit", billingService.getCurrentYearProfit());
        response.put("year", LocalDate.now().getYear());
        return ResponseEntity.ok(ApiResponse.success(response, "Current year profit retrieved successfully"));
    }

    @PostMapping("/generate-all")
    public ResponseEntity<ApiResponse<Void>> generateMonthlyBillingForAll() {
        billingService.generateMonthlyBillingForAll();
        return ResponseEntity.ok(ApiResponse.success(null, "Monthly billing generated for all months"));
    }
}
