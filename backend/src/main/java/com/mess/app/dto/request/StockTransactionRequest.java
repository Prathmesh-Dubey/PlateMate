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
public class StockTransactionRequest {
    private String stockItemId;
    private String transactionType; // PURCHASE, USAGE, RETURN, ADJUSTMENT
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private LocalDate transactionDate;
    private String referenceNumber;
    private String referenceType;
    private String referenceId;
    private String remarks;
    private String enteredByCandidateId;
}