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
public class FeeCollectionResponse {
    private String id;
    private String candidateId;
    private String candidateName;
    private LocalDate collectionDate;
    private BigDecimal amount;
    private String paymentMonth;
    private int paymentYear;
    private int paymentMonthNumber;
    private String paymentMethod;
    private String transactionId;
    private String receiptNumber;
    private boolean isPaid;
    private String remarks;
    private String collectedBy;
    private LocalDateTime createdAt;
}