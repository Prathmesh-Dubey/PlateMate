package com.mess.app.repository;

import com.mess.app.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {

        // Find by date
        List<Expense> findByExpenseDate(LocalDate date);

        // Find by date range
        List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

        // Find by category
        List<Expense> findByCategory(String category);

        // Find by category and date range
        List<Expense> findByCategoryAndExpenseDateBetween(
                        String category, LocalDate startDate, LocalDate endDate);

        // Find by vendor
        List<Expense> findByVendorName(String vendorName);

        // Get total expenses for a date range
        @Query("SELECT SUM(e.totalAmount) FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate")
        BigDecimal getTotalExpensesByDateRange(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get daily expenses for a month
        @Query("SELECT e.expenseDate, SUM(e.totalAmount) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.expenseDate ORDER BY e.expenseDate")
        List<Object[]> getDailyExpensesForMonth(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get category-wise expense summary
        @Query("SELECT e.category, SUM(e.totalAmount) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.category ORDER BY SUM(e.totalAmount) DESC")
        List<Object[]> getCategoryWiseExpenses(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get vendor-wise expense summary
        @Query("SELECT e.vendorName, SUM(e.totalAmount) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "AND e.vendorName IS NOT NULL " +
                        "GROUP BY e.vendorName ORDER BY SUM(e.totalAmount) DESC")
        List<Object[]> getVendorWiseExpenses(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get monthly expenses for a year
        @Query("SELECT EXTRACT(MONTH FROM e.expenseDate), SUM(e.totalAmount) FROM Expense e " +
                        "WHERE EXTRACT(YEAR FROM e.expenseDate) = :year " +
                        "GROUP BY EXTRACT(MONTH FROM e.expenseDate) ORDER BY EXTRACT(MONTH FROM e.expenseDate)")
        List<Object[]> getMonthlyExpensesForYear(@Param("year") int year);

        // Get today's total expenses
        @Query("SELECT SUM(e.totalAmount) FROM Expense e WHERE e.expenseDate = CURRENT_DATE")
        BigDecimal getTodayTotalExpenses();

        // Get expenses by payment method
        @Query("SELECT e.paymentMethod, SUM(e.totalAmount) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.paymentMethod")
        List<Object[]> getExpensesByPaymentMethod(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get top expense items
        @Query("SELECT e.itemName, SUM(e.totalAmount) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.itemName ORDER BY SUM(e.totalAmount) DESC")
        List<Object[]> getTopExpenseItems(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get expense count by category
        @Query("SELECT e.category, COUNT(e) FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.category")
        List<Object[]> getExpenseCountByCategory(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Get average daily expense
        @Query("SELECT AVG(daily_total) FROM (" +
                        "SELECT SUM(e.totalAmount) as daily_total FROM Expense e " +
                        "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.expenseDate) as daily_totals")
        Double getAverageDailyExpense(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ==================== FINANCIAL AGGREGATION (Billing dashboard & monthly P&L) ====================

        /** Number of expense rows dated on a given day (used for "Today's day-to-day expense"). */
        long countByExpenseDate(LocalDate date);

        /**
         * Expenses (all categories) grouped by calendar year/month of expenseDate.
         * Row: [year, month, sum(totalAmount), count].
         */
        @Query("SELECT EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate), SUM(e.totalAmount), COUNT(e) " +
                        "FROM Expense e WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate)")
        List<Object[]> sumExpensesByYearMonth(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
