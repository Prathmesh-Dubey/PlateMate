package com.mess.app.service;

import com.mess.app.dto.request.BulkAttendanceRequest;
import com.mess.app.dto.request.DailyAttendanceRequest;
import com.mess.app.dto.response.AttendanceSummaryResponse;
import com.mess.app.dto.response.DailyAttendanceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DailyAttendanceService {
    DailyAttendanceResponse markAttendance(DailyAttendanceRequest request);

    DailyAttendanceResponse updateAttendance(String id, DailyAttendanceRequest request);

    DailyAttendanceResponse getAttendanceById(String id);

    List<DailyAttendanceResponse> getAttendanceByDate(LocalDate date);

    List<DailyAttendanceResponse> getAttendanceByCandidate(String candidateId);

    List<DailyAttendanceResponse> getAttendanceByCandidateAndDateRange(
            String candidateId,
            LocalDate startDate,
            LocalDate endDate);

    List<DailyAttendanceResponse> getPresentCandidatesByDate(LocalDate date);

    List<DailyAttendanceResponse> getAbsentCandidatesByDate(LocalDate date);

    void bulkMarkAttendance(BulkAttendanceRequest request);

    void deleteAttendance(String id);

    AttendanceSummaryResponse getDailySummary(LocalDate date);

    Map<String, Long> getTodayStats();

    Map<String, Object> getMonthlyAttendanceSummary(String candidateId, int month, int year);

    void autoMarkAbsentForToday();
}