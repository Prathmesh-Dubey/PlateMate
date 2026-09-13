package com.mess.app.service.impl;

import com.mess.app.dto.request.BulkAttendanceRequest;
import com.mess.app.dto.request.DailyAttendanceRequest;
import com.mess.app.dto.response.AttendanceSummaryResponse;
import com.mess.app.dto.response.DailyAttendanceResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.entity.DailyAttendance;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CandidateRepository;
import com.mess.app.repository.DailyAttendanceRepository;
import com.mess.app.service.DailyAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyAttendanceServiceImpl implements DailyAttendanceService {

        private final DailyAttendanceRepository attendanceRepository;
        private final CandidateRepository candidateRepository;

        @Override
        @Transactional
        public DailyAttendanceResponse markAttendance(DailyAttendanceRequest request) {
                log.info("Marking attendance for candidate: {} on date: {}",
                                request.getCandidateId(), request.getAttendanceDate());

                Candidate candidate = candidateRepository.findById(request.getCandidateId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + request.getCandidateId()));

                // Check if attendance already exists for this candidate on this date
                if (attendanceRepository.existsByCandidateAndAttendanceDate(
                                candidate, request.getAttendanceDate())) {
                        throw new RuntimeException("Attendance already marked for this candidate on this date");
                }

                DailyAttendance attendance = DailyAttendance.builder()
                                .candidate(candidate)
                                .attendanceDate(request.getAttendanceDate() != null ? request.getAttendanceDate()
                                                : LocalDate.now())
                                .status(DailyAttendance.AttendanceStatus.valueOf(
                                                request.getStatus().toUpperCase()))
                                .inTime(request.getInTime())
                                .outTime(request.getOutTime())
                                .remarks(request.getRemarks())
                                .isPresent(request.getStatus().equalsIgnoreCase("IN") ||
                                                request.getStatus().equalsIgnoreCase("HALF_DAY"))
                                .build();

                DailyAttendance saved = attendanceRepository.save(attendance);
                log.info("Attendance marked successfully for candidate: {}", candidate.getCandidateId());

                return convertToResponse(saved);
        }

        @Override
        @Transactional
        public DailyAttendanceResponse updateAttendance(String id, DailyAttendanceRequest request) {
                log.info("Updating attendance with ID: {}", id);

                DailyAttendance attendance = attendanceRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Attendance not found with ID: " + id));

                if (request.getStatus() != null) {
                        attendance.setStatus(DailyAttendance.AttendanceStatus.valueOf(
                                        request.getStatus().toUpperCase()));
                        attendance.setPresent(request.getStatus().equalsIgnoreCase("IN") ||
                                        request.getStatus().equalsIgnoreCase("HALF_DAY"));
                }
                if (request.getInTime() != null) {
                        attendance.setInTime(request.getInTime());
                }
                if (request.getOutTime() != null) {
                        attendance.setOutTime(request.getOutTime());
                }
                if (request.getRemarks() != null) {
                        attendance.setRemarks(request.getRemarks());
                }

                DailyAttendance updated = attendanceRepository.save(attendance);
                log.info("Attendance updated successfully");

                return convertToResponse(updated);
        }

        @Override
        @Transactional 
        public DailyAttendanceResponse getAttendanceById(String id) {
                DailyAttendance attendance = attendanceRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Attendance not found with ID: " + id));
                return convertToResponse(attendance);
        }

        @Override
        @Transactional 
        public List<DailyAttendanceResponse> getAttendanceByDate(LocalDate date) {
                return attendanceRepository.findByAttendanceDate(date).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyAttendanceResponse> getAttendanceByCandidate(String candidateId) {
                Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + candidateId));

                return attendanceRepository.findByCandidateOrderByAttendanceDateDesc(candidate)
                                .stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyAttendanceResponse> getAttendanceByCandidateAndDateRange(
                        String candidateId, LocalDate startDate, LocalDate endDate) {

                Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + candidateId));

                return attendanceRepository.findByCandidateAndAttendanceDateBetween(
                                candidate, startDate, endDate)
                                .stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyAttendanceResponse> getPresentCandidatesByDate(LocalDate date) {
                return attendanceRepository.findPresentCandidatesByDate(date).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyAttendanceResponse> getAbsentCandidatesByDate(LocalDate date) {
                return attendanceRepository.findAbsentCandidatesByDate(date).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void bulkMarkAttendance(BulkAttendanceRequest request) {
                log.info("Bulk marking attendance for date: {}", request.getAttendanceDate());

                LocalDate attendanceDate = request.getAttendanceDate() != null ? request.getAttendanceDate()
                                : LocalDate.now();

                // Mark present candidates
                if (request.getPresentCandidateIds() != null) {
                        for (String candidateId : request.getPresentCandidateIds()) {
                                try {
                                        Candidate candidate = candidateRepository.findById(candidateId)
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Candidate not found: " + candidateId));

                                        if (!attendanceRepository.existsByCandidateAndAttendanceDate(
                                                        candidate, attendanceDate)) {
                                                DailyAttendance attendance = DailyAttendance.builder()
                                                                .candidate(candidate)
                                                                .attendanceDate(attendanceDate)
                                                                .status(DailyAttendance.AttendanceStatus.IN)
                                                                .inTime(LocalTime.now())
                                                                .isPresent(true)
                                                                .build();
                                                attendanceRepository.save(attendance);
                                        }
                                } catch (Exception e) {
                                        log.error("Error marking attendance for candidate: {}", candidateId, e);
                                }
                        }
                }

                // Mark absent candidates
                if (request.getAbsentCandidateIds() != null) {
                        for (String candidateId : request.getAbsentCandidateIds()) {
                                try {
                                        Candidate candidate = candidateRepository.findById(candidateId)
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Candidate not found: " + candidateId));

                                        if (!attendanceRepository.existsByCandidateAndAttendanceDate(
                                                        candidate, attendanceDate)) {
                                                DailyAttendance attendance = DailyAttendance.builder()
                                                                .candidate(candidate)
                                                                .attendanceDate(attendanceDate)
                                                                .status(DailyAttendance.AttendanceStatus.OUT)
                                                                .isPresent(false)
                                                                .build();
                                                attendanceRepository.save(attendance);
                                        }
                                } catch (Exception e) {
                                        log.error("Error marking attendance for candidate: {}", candidateId, e);
                                }
                        }
                }

                log.info("Bulk attendance marking completed");
        }

        @Override
        @Transactional
        public void deleteAttendance(String id) {
                log.info("Deleting attendance with ID: {}", id);
                DailyAttendance attendance = attendanceRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Attendance not found with ID: " + id));
                attendanceRepository.delete(attendance);
                log.info("Attendance deleted successfully");
        }

        @Override
        @Transactional 
        public AttendanceSummaryResponse getDailySummary(LocalDate date) {
                LocalDate summaryDate = date != null ? date : LocalDate.now();

                // Get all active candidates
                List<Candidate> activeCandidates = candidateRepository.findByStatus(Candidate.Status.ACTIVE);
                long totalCandidates = activeCandidates.size();

                // Get attendance for the date
                List<DailyAttendance> attendances = attendanceRepository.findByAttendanceDate(summaryDate);

                long present = attendances.stream().filter(DailyAttendance::isPresent).count();
                long absent = totalCandidates - present;
                long halfDay = attendances.stream()
                                .filter(a -> a.getStatus() == DailyAttendance.AttendanceStatus.HALF_DAY)
                                .count();
                long onLeave = attendances.stream()
                                .filter(a -> a.getStatus() == DailyAttendance.AttendanceStatus.LEAVE)
                                .count();

                double attendancePercentage = totalCandidates > 0 ? (double) present / totalCandidates * 100 : 0.0;

                return AttendanceSummaryResponse.builder()
                                .date(summaryDate)
                                .totalCandidates(totalCandidates)
                                .present(present)
                                .absent(absent)
                                .halfDay(halfDay)
                                .onLeave(onLeave)
                                .attendancePercentage(Math.round(attendancePercentage * 100.0) / 100.0)
                                .build();
        }

        @Override
        @Transactional 
        public Map<String, Long> getTodayStats() {
                LocalDate today = LocalDate.now();
                long present = attendanceRepository.countPresentByDate(today);
                long absent = attendanceRepository.countAbsentByDate(today);

                Map<String, Long> stats = new HashMap<>();
                stats.put("present", present);
                stats.put("absent", absent);
                stats.put("total", present + absent);

                return stats;
        }

        @Override
        @Transactional 
        public Map<String, Object> getMonthlyAttendanceSummary(String candidateId, int month, int year) {
                Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + candidateId));

                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

                List<DailyAttendance> attendances = attendanceRepository.getMonthlyAttendance(
                                candidate, startDate, endDate);

                long present = attendances.stream().filter(DailyAttendance::isPresent).count();
                long absent = attendances.stream().filter(a -> !a.isPresent()).count();
                long halfDay = attendances.stream()
                                .filter(a -> a.getStatus() == DailyAttendance.AttendanceStatus.HALF_DAY)
                                .count();
                long onLeave = attendances.stream()
                                .filter(a -> a.getStatus() == DailyAttendance.AttendanceStatus.LEAVE)
                                .count();

                // Calculate working days in month (excluding Sundays or based on business
                // rules)
                // For simplicity, we'll use total days in month
                int totalDays = endDate.getDayOfMonth();

                Map<String, Object> summary = new HashMap<>();
                summary.put("candidateId", candidate.getCandidateId());
                summary.put("candidateName", candidate.getFullName());
                summary.put("month", month);
                summary.put("year", year);
                summary.put("totalDays", totalDays);
                summary.put("present", present);
                summary.put("absent", absent);
                summary.put("halfDay", halfDay);
                summary.put("onLeave", onLeave);
                summary.put("attendancePercentage",
                                totalDays > 0 ? Math.round((double) present / totalDays * 10000.0) / 100.0 : 0.0);
                summary.put("details", attendances.stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList()));

                return summary;
        }

        @Override
        @Transactional
        public void autoMarkAbsentForToday() {
                LocalDate today = LocalDate.now();
                log.info("Auto-marking absent candidates for today: {}", today);

                // Get all active candidates
                List<Candidate> activeCandidates = candidateRepository.findByStatus(Candidate.Status.ACTIVE);

                int markedAbsent = 0;
                for (Candidate candidate : activeCandidates) {
                        // Check if attendance already exists
                        if (!attendanceRepository.existsByCandidateAndAttendanceDate(candidate, today)) {
                                DailyAttendance attendance = DailyAttendance.builder()
                                                .candidate(candidate)
                                                .attendanceDate(today)
                                                .status(DailyAttendance.AttendanceStatus.OUT)
                                                .isPresent(false)
                                                .remarks("Auto-marked absent")
                                                .build();
                                attendanceRepository.save(attendance);
                                markedAbsent++;
                        }
                }

                log.info("Auto-marked {} candidates as absent for today", markedAbsent);
        }

        // Helper method to convert Entity to Response DTO
        private DailyAttendanceResponse convertToResponse(DailyAttendance attendance) {
                return DailyAttendanceResponse.builder()
                                .id(attendance.getId())
                                .candidateId(attendance.getCandidate().getCandidateId())
                                .candidateName(attendance.getCandidate().getFullName())
                                .attendanceDate(attendance.getAttendanceDate())
                                .status(attendance.getStatus().name())
                                .inTime(attendance.getInTime())
                                .outTime(attendance.getOutTime())
                                .isPresent(attendance.isPresent())
                                .remarks(attendance.getRemarks())
                                .createdAt(attendance.getCreatedAt())
                                .updatedAt(attendance.getUpdatedAt())
                                .build();
        }
}