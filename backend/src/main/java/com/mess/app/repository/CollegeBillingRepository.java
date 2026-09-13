package com.mess.app.repository;

import com.mess.app.entity.CollegeBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeBillingRepository extends JpaRepository<CollegeBilling, String> {

    boolean existsByCollegeIgnoreCaseAndMonthAndYear(String college, int month, int year);

    boolean existsByCollegeIgnoreCaseAndMonthAndYearAndIdNot(String college, int month, int year, String id);

    List<CollegeBilling> findAllByOrderByYearDescMonthDescCollegeAsc();

    @Query("SELECT c FROM CollegeBilling c WHERE " +
           "(:college IS NULL OR :college = '' OR LOWER(c.college) = LOWER(:college)) AND " +
           "(:month IS NULL OR c.month = :month) AND " +
           "(:year IS NULL OR c.year = :year) " +
           "ORDER BY c.year DESC, c.month DESC, c.college ASC")
    List<CollegeBilling> findWithFilters(
            @Param("college") String college,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("SELECT DISTINCT c.college FROM CollegeBilling c ORDER BY c.college ASC")
    List<String> findDistinctColleges();

    // ==================== FINANCIAL AGGREGATION (Billing dashboard & monthly P&L) ====================

    /**
     * College billing grouped by the billing month (year / month columns).
     * Row: [year, month, sum(totalBillableAmount), sum(amountReceived), sum(outstandingBalance), count].
     */
    @Query("SELECT c.year, c.month, SUM(c.totalBillableAmount), SUM(c.amountReceived), SUM(c.outstandingBalance), COUNT(c) " +
           "FROM CollegeBilling c WHERE c.year BETWEEN :fromYear AND :toYear GROUP BY c.year, c.month")
    List<Object[]> sumByYearMonth(@Param("fromYear") int fromYear, @Param("toYear") int toYear);
}
