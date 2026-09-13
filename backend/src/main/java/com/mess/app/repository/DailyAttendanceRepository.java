package com.mess.app.repository;

import com.mess.app.entity.Candidate;
import com.mess.app.entity.DailyAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyAttendanceRepository extends JpaRepository<DailyAttendance, String> {

        Optional<DailyAttendance> findByCandidateAndAttendanceDate(Candidate candidate, LocalDate date);

        List<DailyAttendance> findByCandidateOrderByAttendanceDateDesc(Candidate candidate);

        List<DailyAttendance> findByAttendanceDate(LocalDate date);

        List<DailyAttendance> findByCandidateAndAttendanceDateBetween(
                        Candidate candidate, LocalDate startDate, LocalDate endDate);

        @Query("SELECT d FROM DailyAttendance d WHERE d.attendanceDate = :date AND d.isPresent = true")
        List<DailyAttendance> findPresentCandidatesByDate(@Param("date") LocalDate date);

        @Query("SELECT d FROM DailyAttendance d WHERE d.attendanceDate = :date AND d.isPresent = false")
        List<DailyAttendance> findAbsentCandidatesByDate(@Param("date") LocalDate date);

        @Query("SELECT COUNT(d) FROM DailyAttendance d WHERE d.attendanceDate = :date AND d.isPresent = true")
        long countPresentByDate(@Param("date") LocalDate date);

        @Query("SELECT COUNT(d) FROM DailyAttendance d WHERE d.attendanceDate = :date AND d.isPresent = false")
        long countAbsentByDate(@Param("date") LocalDate date);

        // ADD THIS MISSING METHOD - Count present days for a candidate in date range
        @Query("SELECT COUNT(d) FROM DailyAttendance d WHERE d.candidate = :candidate " +
                        "AND d.attendanceDate BETWEEN :startDate AND :endDate AND d.isPresent = true")
        int countPresentDaysForCandidate(@Param("candidate") Candidate candidate,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ADD THIS MISSING METHOD - Get daily attendance count
        @Query("SELECT d.attendanceDate, COUNT(d) FROM DailyAttendance d " +
                        "WHERE d.attendanceDate BETWEEN :startDate AND :endDate AND d.isPresent = true " +
                        "GROUP BY d.attendanceDate")
        List<Object[]> getDailyAttendanceCount(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ADD THIS MISSING METHOD - Get total attendance days in date range
        @Query("SELECT COUNT(DISTINCT d.attendanceDate) FROM DailyAttendance d " +
                        "WHERE d.attendanceDate BETWEEN :startDate AND :endDate")
        long getTotalAttendanceDays(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT d FROM DailyAttendance d WHERE d.candidate = :candidate " +
                        "AND d.attendanceDate BETWEEN :startDate AND :endDate")
        List<DailyAttendance> getMonthlyAttendance(
                        @Param("candidate") Candidate candidate,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT d.status, COUNT(d) FROM DailyAttendance d " +
                        "WHERE d.attendanceDate BETWEEN :startDate AND :endDate GROUP BY d.status")
        List<Object[]> getAttendanceCountByStatus(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        boolean existsByCandidateAndAttendanceDate(Candidate candidate, LocalDate date);

        @Query("SELECT COUNT(DISTINCT d.candidate) FROM DailyAttendance d " +
                        "WHERE d.attendanceDate = CURRENT_DATE AND d.isPresent = true")
        long getTodayPresentCount();

        @Query("SELECT COUNT(DISTINCT d.candidate) FROM DailyAttendance d " +
                        "WHERE d.attendanceDate = CURRENT_DATE AND d.isPresent = false")
        long getTodayAbsentCount();
}