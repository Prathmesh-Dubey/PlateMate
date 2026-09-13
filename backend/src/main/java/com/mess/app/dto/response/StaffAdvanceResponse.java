package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAdvanceResponse {
    private String id;
    private String staffId;
    private String staffName;
    private LocalDate advanceDate;
    private BigDecimal amount;
    private String reason;
    private String status;
    private LocalDate repaymentDate;
    private BigDecimal repaymentAmount;
    private BigDecimal remainingAmount;
    private int installmentMonths;
    private BigDecimal monthlyDeduction;
    private String remarks;
    private String approvedBy;
    private String enteredBy;
    private LocalDateTime createdAt;
}