package com.mess.app.repository;

import com.mess.app.entity.MonthlyBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyBillingRepository extends JpaRepository<MonthlyBilling, String> {

    Optional<MonthlyBilling> findByMonthAndYear(int month, int year);

    List<MonthlyBilling> findByYearOrderByMonthAsc(int year);

    List<MonthlyBilling> findByYearBetween(int startYear, int endYear);

    @Query("SELECT m FROM MonthlyBilling m WHERE m.year = :year AND m.month <= :month ORDER BY m.month DESC")
    List<MonthlyBilling> findYearToDate(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(m.totalIncome) FROM MonthlyBilling m WHERE m.year = :year")
    BigDecimal getTotalIncomeForYear(@Param("year") int year);

    @Query("SELECT SUM(m.totalExpenses) FROM MonthlyBilling m WHERE m.year = :year")
    BigDecimal getTotalExpensesForYear(@Param("year") int year);

    @Query("SELECT SUM(m.profit) FROM MonthlyBilling m WHERE m.year = :year AND m.profit > 0")
    BigDecimal getTotalProfitForYear(@Param("year") int year);

    @Query("SELECT SUM(m.loss) FROM MonthlyBilling m WHERE m.year = :year AND m.loss > 0")
    BigDecimal getTotalLossForYear(@Param("year") int year);

    @Query("SELECT m FROM MonthlyBilling m ORDER BY m.year DESC, m.month DESC")
    List<MonthlyBilling> findAllOrderByYearMonthDesc();
}