package com.mess.app.repository;

import com.mess.app.entity.Staff;
import com.mess.app.entity.Staff.StaffStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {

    Optional<Staff> findByStaffId(String staffId);

    List<Staff> findByStatus(StaffStatus status);

    List<Staff> findByStatusOrderByStaffIdAsc(StaffStatus status);

    List<Staff> findByPosition(String position);

    @Query("SELECT s FROM Staff s ORDER BY s.staffId DESC LIMIT 1")
    Optional<Staff> findTopByOrderByStaffIdDesc();

    long countByStatus(StaffStatus status);

    boolean existsByStaffId(String staffId);

    List<Staff> findByJoiningDateBetween(LocalDate startDate, LocalDate endDate);

    List<Staff> findByLeavingDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Staff s WHERE s.joiningDate <= :date AND (s.leavingDate IS NULL OR s.leavingDate >= :date)")
    List<Staff> findActiveStaffOnDate(@Param("date") LocalDate date);

    /** Monthly payroll commitment: sum of base salary of ACTIVE staff. */
    @Query("SELECT SUM(s.baseSalary) FROM Staff s WHERE s.status = 'ACTIVE'")
    java.math.BigDecimal getTotalActiveBaseSalary();
}
