package com.mess.app.repository;

import com.mess.app.entity.Candidate;
import com.mess.app.entity.FeeCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeCollectionRepository extends JpaRepository<FeeCollection, String> {

        List<FeeCollection> findByCandidateOrderByCollectionDateDesc(Candidate candidate);

        List<FeeCollection> findByPaymentMonthAndPaymentYear(String paymentMonth, int year);

        List<FeeCollection> findByPaymentMonthNumberAndPaymentYear(int month, int year);

        Optional<FeeCollection> findByCandidateAndPaymentMonthAndPaymentYear(
                        Candidate candidate, String paymentMonth, int year);

        List<FeeCollection> findByCollectionDateBetween(LocalDate startDate, LocalDate endDate);

        List<FeeCollection> findByIsPaid(boolean isPaid);

        // ADD THIS MISSING METHOD - Get total fees by date range
        @Query("SELECT SUM(f.amount) FROM FeeCollection f " +
                        "WHERE f.collectionDate BETWEEN :startDate AND :endDate AND f.isPaid = true")
        BigDecimal getTotalFeesByDateRange(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ADD THIS MISSING METHOD - Get total pending fees
        @Query("SELECT SUM(f.amount) FROM FeeCollection f WHERE f.isPaid = false")
        BigDecimal getTotalPendingFees();

        // ADD THIS MISSING METHOD - Get fees paid by candidate
        @Query("SELECT SUM(f.amount) FROM FeeCollection f " +
                        "WHERE f.candidate = :candidate AND f.collectionDate BETWEEN :startDate AND :endDate AND f.isPaid = true")
        BigDecimal getFeesPaidByCandidate(@Param("candidate") Candidate candidate,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ADD THIS MISSING METHOD - Get fees paid by candidate for month
        @Query("SELECT SUM(f.amount) FROM FeeCollection f " +
                        "WHERE f.candidate = :candidate AND f.paymentMonthNumber = :month " +
                        "AND f.paymentYear = :year AND f.isPaid = true")
        BigDecimal getFeesPaidByCandidateForMonth(@Param("candidate") Candidate candidate,
                        @Param("month") int month,
                        @Param("year") int year);

        @Query("SELECT SUM(f.amount) FROM FeeCollection f " +
                        "WHERE f.paymentMonthNumber = :month AND f.paymentYear = :year")
        BigDecimal getTotalFeesForMonth(@Param("month") int month, @Param("year") int year);

        @Query("SELECT SUM(f.amount) FROM FeeCollection f WHERE f.paymentYear = :year")
        BigDecimal getTotalFeesForYear(@Param("year") int year);

        @Query("SELECT SUM(f.amount) FROM FeeCollection f " +
                        "WHERE f.paymentMonthNumber = :month AND f.paymentYear = :year AND f.isPaid = true")
        BigDecimal getCollectedFeesForMonth(@Param("month") int month, @Param("year") int year);

        @Query("SELECT COUNT(f) FROM FeeCollection f " +
                        "WHERE f.paymentMonthNumber = :month AND f.paymentYear = :year AND f.isPaid = true")
        long getPaidCandidatesCountForMonth(@Param("month") int month, @Param("year") int year);

        @Query("SELECT COUNT(DISTINCT f.candidate) FROM FeeCollection f " +
                        "WHERE f.paymentMonthNumber = :month AND f.paymentYear = :year")
        long getTotalCandidatesWithFeeRecord(@Param("month") int month, @Param("year") int year);

        // ==================== FINANCIAL AGGREGATION (Billing dashboard & monthly P&L) ====================

        /**
         * Mess fees actually collected (isPaid = true) grouped by the month the fee is FOR
         * (paymentYear / paymentMonthNumber). Row: [year, month, sum(amount), count].
         */
        @Query("SELECT f.paymentYear, f.paymentMonthNumber, SUM(f.amount), COUNT(f) FROM FeeCollection f " +
                        "WHERE f.isPaid = true AND f.paymentYear BETWEEN :fromYear AND :toYear " +
                        "GROUP BY f.paymentYear, f.paymentMonthNumber")
        List<Object[]> sumCollectedFeesByYearMonth(@Param("fromYear") int fromYear, @Param("toYear") int toYear);

        }
