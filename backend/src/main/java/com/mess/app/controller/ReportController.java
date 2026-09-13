package com.mess.app.controller;

import com.mess.app.dto.request.ReportRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.ComprehensiveReportResponse;
import com.mess.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ReportController {

    private final ReportService reportService;

    // ==================== COMPREHENSIVE REPORTS ====================

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ComprehensiveReportResponse>> generateReport(
            @RequestBody ReportRequest request) {
        ComprehensiveReportResponse response = reportService.generateComprehensiveReport(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Report generated successfully"));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<ComprehensiveReportResponse>> getMonthlyReport(
            @RequestParam int month,
            @RequestParam int year) {
        ComprehensiveReportResponse response = reportService.generateMonthlyReport(month, year);
        return ResponseEntity.ok(ApiResponse.success(response, "Monthly report generated successfully"));
    }

    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<ComprehensiveReportResponse>> getYearlyReport(
            @RequestParam int year) {
        ComprehensiveReportResponse response = reportService.generateYearlyReport(year);
        return ResponseEntity.ok(ApiResponse.success(response, "Yearly report generated successfully"));
    }

    @GetMapping("/custom")
    public ResponseEntity<ApiResponse<ComprehensiveReportResponse>> getCustomReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ComprehensiveReportResponse response = reportService.generateCustomReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Custom report generated successfully"));
    }

    // ==================== MODULE SPECIFIC REPORTS ====================

    @GetMapping("/candidate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCandidateReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getCandidateReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Candidate report generated successfully"));
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getAttendanceReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Attendance report generated successfully"));
    }

    @GetMapping("/expense")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExpenseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getExpenseReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Expense report generated successfully"));
    }

    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStockReport() {
        Map<String, Object> report = reportService.getStockReport();
        return ResponseEntity.ok(ApiResponse.success(report, "Stock report generated successfully"));
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStaffReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getStaffReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Staff report generated successfully"));
    }

    @GetMapping("/billing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBillingReport(
            @RequestParam int month,
            @RequestParam int year) {
        Map<String, Object> report = reportService.getBillingReport(month, year);
        return ResponseEntity.ok(ApiResponse.success(report, "Billing report generated successfully"));
    }

    @GetMapping("/party")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPartyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getPartyReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Party report generated successfully"));
    }

    @GetMapping("/menu")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMenuReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = reportService.getMenuReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Menu report generated successfully"));
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfitLossReport(
            @RequestParam int year) {
        Map<String, Object> report = reportService.getProfitLossReport(year);
        return ResponseEntity.ok(ApiResponse.success(report, "Profit & Loss report generated successfully"));
    }

    // ==================== EXPORT REPORTS ====================

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPDF(@RequestBody ReportRequest request) {
        ComprehensiveReportResponse report = reportService.generateComprehensiveReport(request);
        byte[] pdfBytes = reportService.exportReportAsPDF(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody ReportRequest request) {
        ComprehensiveReportResponse report = reportService.generateComprehensiveReport(request);
        byte[] excelBytes = reportService.exportReportAsExcel(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "report.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/export/csv")
    public ResponseEntity<String> exportCSV(@RequestBody ReportRequest request) {
        ComprehensiveReportResponse report = reportService.generateComprehensiveReport(request);
        String csvData = reportService.exportReportAsCSV(report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "report.csv");

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}