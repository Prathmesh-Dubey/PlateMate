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
public class StaffPaymentRequest {
    private String staffId;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentMonth;
    private int paymentYear;
    private int paymentMonthNumber;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private String paymentMethod;
    private String transactionId;
    private String referenceNumber;
    private String remarks;
    private String enteredByCandidateId;
}