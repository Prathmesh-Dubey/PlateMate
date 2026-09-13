package com.mess.app.repository;

import com.mess.app.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ReportRepository {

    // Candidate Reports
    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.status = 'ACTIVE'")
    long countActiveCandidates();

    @Query("SELECT COUNT(c) FROM Candidate c")
    long countTotalCandidates();

    @Query("SELECT c.status, COUNT(c) FROM Candidate c GROUP BY c.status")
    List<Object[]> getCandidateStatusCount();

    @Query("SELECT EXTRACT(MONTH FROM c.joiningDate), COUNT(c) FROM Candidate c " +
            "WHERE EXTRACT(YEAR FROM c.joiningDate) = :year GROUP BY EXTRACT(MONTH FROM c.joiningDate)")
    List<Object[]> getMonthlyJoining(@Param("year") int year);

    // Attendance Reports
    @Query("SELECT COUNT(DISTINCT d.attendanceDate) FROM DailyAttendance d " +
            "WHERE d.attendanceDate BETWEEN :startDate AND :endDate")
    long getTotalAttendanceDays(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT d.attendanceDate, COUNT(d) FROM DailyAttendance d " +
            "WHERE d.attendanceDate BETWEEN :startDate AND :endDate AND d.isPresent = true " +
            "GROUP BY d.attendanceDate")
    List<Object[]> getDailyAttendance(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Expense Reports
    @Query("SELECT SUM(e.totalAmount) FROM Expense e " +
            "WHERE e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpenses(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category, SUM(e.totalAmount) FROM Expense e " +
            "WHERE e.expenseDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category")
    List<Object[]> getCategoryWiseExpenses(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Stock Reports
    @Query("SELECT COUNT(s) FROM StockItem s WHERE s.active = true")
    long countActiveStockItems();

    @Query("SELECT SUM(s.currentStock * s.unitPrice) FROM StockItem s WHERE s.active = true")
    BigDecimal getTotalStockValue();

    @Query("SELECT s.category, COUNT(s) FROM StockItem s WHERE s.active = true GROUP BY s.category")
    List<Object[]> getStockCategoryCount();

    // Staff Reports
    @Query("SELECT COUNT(s) FROM Staff s WHERE s.status = 'ACTIVE'")
    long countActiveStaff();

    @Query("SELECT SUM(s.baseSalary) FROM Staff s WHERE s.status = 'ACTIVE'")
    BigDecimal getTotalSalary();

    @Query("SELECT SUM(sp.amount) FROM StaffPayment sp " +
            "WHERE sp.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalStaffPayments(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Billing Reports
    @Query("SELECT SUM(f.amount) FROM FeeCollection f " +
            "WHERE f.paymentMonthNumber = :month AND f.paymentYear = :year")
    BigDecimal getCollectedFees(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(f) FROM FeeCollection f " +
            "WHERE f.paymentMonthNumber = :month AND f.paymentYear = :year AND f.isPaid = true")
    long getPaidCandidateCount(@Param("month") int month, @Param("year") int year);

    // Party Reports
    @Query("SELECT COUNT(p) FROM Party p " +
            "WHERE p.eventDate BETWEEN :startDate AND :endDate")
    long getPartyCount(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(p.totalBill) FROM Party p " +
            "WHERE p.eventDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPartyRevenue(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Menu Reports
    @Query("SELECT COUNT(m) FROM Menu m " +
            "WHERE m.menuDate BETWEEN :startDate AND :endDate")
    long getMenuCount(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(mi) FROM MenuItem mi " +
            "WHERE mi.menu.menuDate BETWEEN :startDate AND :endDate")
    long getMenuItemCount(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}