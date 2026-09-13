package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffSalarySummary {
    private String staffId;
    private String staffName;
    private String position;
    private BigDecimal baseSalary;
    private BigDecimal totalPaid;
    private BigDecimal totalDeductions;
    private BigDecimal totalBonus;
    private BigDecimal outstandingAdvance;
    private int monthsWorked;
    private LocalDate joiningDate;
    private LocalDate lastPaymentDate;
}