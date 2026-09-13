package com.mess.app.dto.request;

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
public class StaffAdvanceRequest {
    private String staffId;
    private LocalDate advanceDate;
    private BigDecimal amount;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED, REPAID
    private LocalDate repaymentDate;
    private BigDecimal repaymentAmount;
    private int installmentMonths;
    private BigDecimal monthlyDeduction;
    private String remarks;
    private String approvedByCandidateId;
    private String enteredByCandidateId;
}