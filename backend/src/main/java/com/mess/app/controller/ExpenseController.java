package com.mess.app.controller;

import com.mess.app.dto.request.ExpenseRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.ExpenseResponse;
import com.mess.app.dto.response.ExpenseSummaryResponse;
import com.mess.app.service.ExpenseService;
import com.mess.app.util.FileUploadUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final FileUploadUtil fileUploadUtil;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadReceipt(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File cannot be empty"));
            }
            String fileUrl = fileUploadUtil.uploadExpenseReceipt(file);
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "receipt";
            Map<String, String> response = Map.of(
                    "fileUrl", fileUrl,
                    "fileName", originalFilename
            );
            return ResponseEntity.ok(ApiResponse.success(response, "Receipt uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload receipt: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Expense created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable String id,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Expense updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @PathVariable String id) {
        ExpenseResponse response = expenseService.getExpenseById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Expense retrieved successfully"));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByDate(
            @PathVariable String date) {
        LocalDate expenseDate = LocalDate.parse(date);
        List<ExpenseResponse> responses = expenseService.getExpensesByDate(expenseDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Expenses retrieved successfully"));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<ExpenseResponse> responses = expenseService.getExpensesByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success(responses, "Expenses retrieved successfully"));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByCategory(
            @PathVariable String category) {
        List<ExpenseResponse> responses = expenseService.getExpensesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(responses, "Expenses retrieved successfully"));
    }

    @GetMapping("/category/{category}/range")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByCategoryAndDateRange(
            @PathVariable String category,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<ExpenseResponse> responses = expenseService.getExpensesByCategoryAndDateRange(
                category, start, end);
        return ResponseEntity.ok(ApiResponse.success(responses, "Expenses retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted successfully"));
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalExpenses(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        BigDecimal total = expenseService.getTotalExpenses(start, end);
        return ResponseEntity.ok(ApiResponse.success(total, "Total expenses retrieved successfully"));
    }

    @GetMapping("/total/today")
    public ResponseEntity<ApiResponse<BigDecimal>> getTodayTotalExpenses() {
        BigDecimal total = expenseService.getTodayTotalExpenses();
        return ResponseEntity.ok(ApiResponse.success(total, "Today's total expenses retrieved successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ExpenseSummaryResponse>> getExpenseSummary(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        ExpenseSummaryResponse summary = expenseService.getExpenseSummary(start, end);
        return ResponseEntity.ok(ApiResponse.success(summary, "Expense summary retrieved successfully"));
    }

    @GetMapping("/category-wise")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getCategoryWiseExpenses(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        Map<String, BigDecimal> categoryWise = expenseService.getCategoryWiseExpenses(start, end);
        return ResponseEntity.ok(ApiResponse.success(categoryWise, "Category-wise expenses retrieved successfully"));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getMonthlyExpensesForYear(
            @RequestParam int year) {
        Map<String, BigDecimal> monthlyExpenses = expenseService.getMonthlyExpensesForYear(year);
        return ResponseEntity.ok(ApiResponse.success(monthlyExpenses, "Monthly expenses retrieved successfully"));
    }

    @GetMapping("/daily/{month}/{year}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyExpensesForMonth(
            @PathVariable int month,
            @PathVariable int year) {
        List<Map<String, Object>> dailyExpenses = expenseService.getDailyExpensesForMonth(month, year);
        return ResponseEntity.ok(ApiResponse.success(dailyExpenses, "Daily expenses retrieved successfully"));
    }
}