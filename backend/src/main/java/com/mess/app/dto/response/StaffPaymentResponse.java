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
public class StaffPaymentResponse {
    private String id;
    private String staffId;
    private String staffName;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentMonth;
    private int paymentYear;
    private int paymentMonthNumber;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal netAmount;
    private String paymentMethod;
    private String transactionId;
    private String referenceNumber;
    private String remarks;
    private String enteredBy;
    private LocalDateTime createdAt;
}