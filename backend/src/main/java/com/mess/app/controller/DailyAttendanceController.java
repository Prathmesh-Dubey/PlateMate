package com.mess.app.controller;

import com.mess.app.dto.request.BulkAttendanceRequest;
import com.mess.app.dto.request.DailyAttendanceRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.AttendanceSummaryResponse;
import com.mess.app.dto.response.DailyAttendanceResponse;
import com.mess.app.service.DailyAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class DailyAttendanceController {

    private final DailyAttendanceService attendanceService;

    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<DailyAttendanceResponse>> markAttendance(
            @Valid @RequestBody DailyAttendanceRequest request) {
        DailyAttendanceResponse response = attendanceService.markAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Attendance marked successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyAttendanceResponse>> updateAttendance(
            @PathVariable String id,
            @Valid @RequestBody DailyAttendanceRequest request) {
        DailyAttendanceResponse response = attendanceService.updateAttendance(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Attendance updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyAttendanceResponse>> getAttendanceById(
            @PathVariable String id) {
        DailyAttendanceResponse response = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Attendance retrieved successfully"));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<DailyAttendanceResponse>>> getAttendanceByDate(
            @PathVariable String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        List<DailyAttendanceResponse> responses = attendanceService.getAttendanceByDate(attendanceDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Attendance retrieved successfully"));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<DailyAttendanceResponse>>> getAttendanceByCandidate(
            @PathVariable String candidateId) {
        List<DailyAttendanceResponse> responses = attendanceService.getAttendanceByCandidate(candidateId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Attendance retrieved successfully"));
    }

    @GetMapping("/candidate/{candidateId}/range")
    public ResponseEntity<ApiResponse<List<DailyAttendanceResponse>>> getAttendanceByCandidateAndDateRange(
            @PathVariable String candidateId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<DailyAttendanceResponse> responses = attendanceService.getAttendanceByCandidateAndDateRange(
                candidateId, start, end);
        return ResponseEntity.ok(ApiResponse.success(responses, "Attendance retrieved successfully"));
    }

    @GetMapping("/present/{date}")
    public ResponseEntity<ApiResponse<List<DailyAttendanceResponse>>> getPresentCandidates(
            @PathVariable String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        List<DailyAttendanceResponse> responses = attendanceService.getPresentCandidatesByDate(attendanceDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Present candidates retrieved successfully"));
    }

    @GetMapping("/absent/{date}")
    public ResponseEntity<ApiResponse<List<DailyAttendanceResponse>>> getAbsentCandidates(
            @PathVariable String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        List<DailyAttendanceResponse> responses = attendanceService.getAbsentCandidatesByDate(attendanceDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Absent candidates retrieved successfully"));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Void>> bulkMarkAttendance(
            @Valid @RequestBody BulkAttendanceRequest request) {
        attendanceService.bulkMarkAttendance(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Bulk attendance marked successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable String id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance deleted successfully"));
    }

    @GetMapping("/summary/daily")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getDailySummary(
            @RequestParam(required = false) String date) {
        LocalDate summaryDate = date != null ? LocalDate.parse(date) : null;
        AttendanceSummaryResponse summary = attendanceService.getDailySummary(summaryDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Daily summary retrieved successfully"));
    }

    @GetMapping("/stats/today")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTodayStats() {
        Map<String, Long> stats = attendanceService.getTodayStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Today's stats retrieved successfully"));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlySummary(
            @RequestParam String candidateId,
            @RequestParam int month,
            @RequestParam int year) {
        Map<String, Object> summary = attendanceService.getMonthlyAttendanceSummary(
                candidateId, month, year);
        return ResponseEntity.ok(ApiResponse.success(summary, "Monthly summary retrieved successfully"));
    }

    @PostMapping("/auto-mark-absent")
    public ResponseEntity<ApiResponse<Void>> autoMarkAbsent() {
        attendanceService.autoMarkAbsentForToday();
        return ResponseEntity.ok(ApiResponse.success(null, "Auto-marked absent candidates successfully"));
    }
}