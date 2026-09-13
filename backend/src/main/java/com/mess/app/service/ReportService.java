package com.mess.app.service;

import com.mess.app.dto.request.ReportRequest;
import com.mess.app.dto.response.ComprehensiveReportResponse;

import java.time.LocalDate;
import java.util.Map;

public interface ReportService {

    // Comprehensive Reports
    ComprehensiveReportResponse generateComprehensiveReport(ReportRequest request);

    ComprehensiveReportResponse generateMonthlyReport(int month, int year);

    ComprehensiveReportResponse generateYearlyReport(int year);

    ComprehensiveReportResponse generateCustomReport(LocalDate startDate, LocalDate endDate);

    // Module Specific Reports
    Map<String, Object> getCandidateReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getAttendanceReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getExpenseReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getStockReport();

    Map<String, Object> getStaffReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getBillingReport(int month, int year);

    Map<String, Object> getPartyReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getMenuReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getProfitLossReport(int year);

    // Export Methods
    byte[] exportReportAsPDF(ComprehensiveReportResponse report);

    byte[] exportReportAsExcel(ComprehensiveReportResponse report);

    String exportReportAsCSV(ComprehensiveReportResponse report);
}