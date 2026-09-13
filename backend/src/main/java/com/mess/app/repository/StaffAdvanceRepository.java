package com.mess.app.repository;

import com.mess.app.entity.Staff;
import com.mess.app.entity.StaffAdvance;
import com.mess.app.entity.StaffAdvance.AdvanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffAdvanceRepository extends JpaRepository<StaffAdvance, String> {

        List<StaffAdvance> findByStaffOrderByAdvanceDateDesc(Staff staff);

        List<StaffAdvance> findByStaffAndStatus(Staff staff, AdvanceStatus status);

        List<StaffAdvance> findByStatus(AdvanceStatus status);

        @Query("SELECT SUM(a.amount) FROM StaffAdvance a WHERE a.staff = :staff " +
                        "AND a.status IN ('PENDING', 'APPROVED')")
        BigDecimal getOutstandingAdvanceForStaff(@Param("staff") Staff staff);

        // ADD THIS MISSING METHOD - Get total outstanding advances
        @Query("SELECT SUM(a.amount - COALESCE(a.repaymentAmount, 0)) FROM StaffAdvance a " +
                        "WHERE a.status IN ('PENDING', 'APPROVED')")
        BigDecimal getTotalOutstandingAdvances();

        @Query("SELECT SUM(a.amount) FROM StaffAdvance a WHERE a.staff = :staff " +
                        "AND a.status = 'APPROVED'")
        BigDecimal getApprovedAdvanceTotal(@Param("staff") Staff staff);

        @Query("SELECT SUM(a.amount) FROM StaffAdvance a WHERE a.staff = :staff " +
                        "AND a.status = 'REPAID'")
        BigDecimal getRepaidAdvanceTotal(@Param("staff") Staff staff);

        List<StaffAdvance> findByAdvanceDateBetween(LocalDate startDate, LocalDate endDate);
}