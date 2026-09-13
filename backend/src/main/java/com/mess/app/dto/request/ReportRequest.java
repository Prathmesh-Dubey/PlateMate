package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private String reportType; // MONTHLY, YEARLY, CUSTOM
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer month;
    private Integer year;
    private String format; // PDF, EXCEL, CSV
    private String category; // For expense reports
    private String candidateId; // For candidate reports
    private String staffId; // For staff reports
}