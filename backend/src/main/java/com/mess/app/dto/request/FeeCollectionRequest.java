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
public class FeeCollectionRequest {
    private String candidateId;
    private LocalDate collectionDate;
    private BigDecimal amount;
    private String paymentMonth;
    private int paymentYear;
    private int paymentMonthNumber;
    private String paymentMethod;
    private String transactionId;
    private String receiptNumber;
    private String remarks;
    private String collectedByCandidateId;
}