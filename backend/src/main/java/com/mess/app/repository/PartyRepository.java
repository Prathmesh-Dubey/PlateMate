package com.mess.app.repository;

import com.mess.app.entity.Party;
import com.mess.app.entity.Party.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, String> {

        Optional<Party> findByPartyId(String partyId);

        List<Party> findByEventDate(LocalDate date);

        List<Party> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

        List<Party> findByPaymentStatus(PaymentStatus status);

        List<Party> findByPartyNameContainingIgnoreCase(String name);

        List<Party> findByPartyNameContainingIgnoreCaseAndEventDateBetween(
                        String name, LocalDate startDate, LocalDate endDate);

        List<Party> findByPaymentStatusAndEventDateBetween(
                        PaymentStatus status, LocalDate startDate, LocalDate endDate);

        // ==================== NEW: DEPARTMENT FILTER ====================
        List<Party> findByDepartmentCode(String departmentCode);

        @Query("SELECT p FROM Party p ORDER BY p.partyId DESC LIMIT 1")
        Optional<Party> findTopByOrderByPartyIdDesc();

        @Query("SELECT SUM(p.totalBill) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate")
        BigDecimal getTotalPartyBilling(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT SUM(p.totalBill) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate " +
                        "AND p.paymentStatus = 'PAID'")
        BigDecimal getTotalPaidBilling(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT SUM(p.totalBill) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate " +
                        "AND p.paymentStatus IN ('PENDING', 'PARTIAL')")
        BigDecimal getTotalPendingBilling(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT SUM(p.pendingAmount) FROM Party p WHERE p.paymentStatus IN ('PENDING', 'PARTIAL')")
        BigDecimal getTotalOutstandingAmount();

        @Query("SELECT SUM(p.pendingAmount) FROM Party p WHERE p.paymentStatus IN ('PENDING', 'PARTIAL') " +
                        "AND p.eventDate <= :date")
        BigDecimal getTotalOutstandingAmountUpToDate(@Param("date") LocalDate date);

        // ADD THIS MISSING METHOD
        @Query("SELECT COUNT(p) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate")
        long countPartiesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(p) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate " +
                        "AND p.paymentStatus = 'PAID'")
        long countPaidPartiesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT p.mealType, COUNT(p) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY p.mealType")
        List<Object[]> getMealTypeWiseCount(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT p.mealType, SUM(p.totalBill) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY p.mealType")
        List<Object[]> getMealTypeWiseRevenue(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT EXTRACT(MONTH FROM p.eventDate), SUM(p.totalBill) FROM Party p " +
                        "WHERE EXTRACT(YEAR FROM p.eventDate) = :year " +
                        "GROUP BY EXTRACT(MONTH FROM p.eventDate) ORDER BY EXTRACT(MONTH FROM p.eventDate)")
        List<Object[]> getMonthlyPartyBilling(@Param("year") int year);

        @Query("SELECT EXTRACT(MONTH FROM p.eventDate), COUNT(p) FROM Party p " +
                        "WHERE EXTRACT(YEAR FROM p.eventDate) = :year " +
                        "GROUP BY EXTRACT(MONTH FROM p.eventDate) ORDER BY EXTRACT(MONTH FROM p.eventDate)")
        List<Object[]> getMonthlyPartyCount(@Param("year") int year);

        @Query("SELECT p FROM Party p WHERE p.eventDate >= :date AND p.paymentStatus IN ('PENDING', 'PARTIAL')")
        List<Party> findPendingPaymentsFromDate(@Param("date") LocalDate date);

        @Query("SELECT SUM(p.numberOfPeople) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate")
        Long getTotalPeopleByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT AVG(p.totalBill) FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate")
        BigDecimal getAverageBillByDateRange(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT p FROM Party p WHERE p.eventDate >= :startDate ORDER BY p.eventDate DESC")
        List<Party> findRecentParties(@Param("startDate") LocalDate startDate);

        // ==================== FINANCIAL AGGREGATION (Billing dashboard & monthly P&L) ====================

        /**
         * Party billing grouped by calendar year/month of the event date.
         * "Received" treats a party marked PAID as fully received (its paidAmount may be
         * stale when the status was changed directly), otherwise uses paidAmount.
         * Row: [year, month, sum(totalBill), sum(received), count].
         */
        @Query("SELECT EXTRACT(YEAR FROM p.eventDate), EXTRACT(MONTH FROM p.eventDate), SUM(p.totalBill), " +
                        "SUM(CASE WHEN p.paymentStatus = 'PAID' THEN p.totalBill ELSE COALESCE(p.paidAmount, 0) END), COUNT(p) " +
                        "FROM Party p WHERE p.eventDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY EXTRACT(YEAR FROM p.eventDate), EXTRACT(MONTH FROM p.eventDate)")
        List<Object[]> sumBillingByYearMonth(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}
