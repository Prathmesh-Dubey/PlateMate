package com.mess.app.service;

import com.mess.app.dto.request.ExpenseRequest;
import com.mess.app.dto.response.ExpenseResponse;
import com.mess.app.dto.response.ExpenseSummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseService {
    ExpenseResponse createExpense(ExpenseRequest request);

    ExpenseResponse updateExpense(String id, ExpenseRequest request);

    ExpenseResponse getExpenseById(String id);

    List<ExpenseResponse> getExpensesByDate(LocalDate date);

    List<ExpenseResponse> getExpensesByDateRange(LocalDate startDate, LocalDate endDate);

    List<ExpenseResponse> getExpensesByCategory(String category);

    List<ExpenseResponse> getExpensesByCategoryAndDateRange(
            String category, LocalDate startDate, LocalDate endDate);

    void deleteExpense(String id);

    BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate);

    BigDecimal getTodayTotalExpenses();

    ExpenseSummaryResponse getExpenseSummary(LocalDate startDate, LocalDate endDate);

    Map<String, BigDecimal> getCategoryWiseExpenses(LocalDate startDate, LocalDate endDate);

    Map<String, BigDecimal> getMonthlyExpensesForYear(int year);

    List<Map<String, Object>> getDailyExpensesForMonth(int month, int year);
}