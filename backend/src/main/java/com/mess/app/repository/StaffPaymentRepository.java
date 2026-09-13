package com.mess.app.repository;

import com.mess.app.entity.Staff;
import com.mess.app.entity.StaffPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffPaymentRepository extends JpaRepository<StaffPayment, String> {

        List<StaffPayment> findByStaffOrderByPaymentDateDesc(Staff staff);

        List<StaffPayment> findByStaffAndPaymentYear(Staff staff, int year);

        List<StaffPayment> findByStaffAndPaymentMonthNumberAndPaymentYear(
                        Staff staff, int month, int year);

        Optional<StaffPayment> findByStaffAndPaymentMonthAndPaymentYear(
                        Staff staff, String paymentMonth, int year);

        List<StaffPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

        // ADD THIS MISSING METHOD
        List<StaffPayment> findByStaffAndPaymentDateBetween(Staff staff, LocalDate startDate, LocalDate endDate);

        @Query("SELECT SUM(p.amount) FROM StaffPayment p WHERE p.staff = :staff " +
                        "AND p.paymentDate BETWEEN :startDate AND :endDate")
        BigDecimal getTotalPaymentsForStaff(
                        @Param("staff") Staff staff,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT SUM(p.netAmount) FROM StaffPayment p WHERE p.staff = :staff " +
                        "AND p.paymentDate BETWEEN :startDate AND :endDate")
        BigDecimal getTotalNetPaymentsForStaff(
                        @Param("staff") Staff staff,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT SUM(p.amount) FROM StaffPayment p " +
                        "WHERE p.paymentDate BETWEEN :startDate AND :endDate")
        BigDecimal getTotalPaymentsByDateRange(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ==================== FINANCIAL AGGREGATION (Billing dashboard & monthly P&L) ====================

        /**
         * Salary disbursed grouped by the salary month (paymentYear / paymentMonthNumber).
         * Row: [year, month, sum(amount), count].
         */
        @Query("SELECT p.paymentYear, p.paymentMonthNumber, SUM(p.amount), COUNT(p) FROM StaffPayment p " +
                        "WHERE p.paymentYear BETWEEN :fromYear AND :toYear " +
                        "GROUP BY p.paymentYear, p.paymentMonthNumber")
        List<Object[]> sumPaymentsByYearMonth(@Param("fromYear") int fromYear, @Param("toYear") int toYear);
}
