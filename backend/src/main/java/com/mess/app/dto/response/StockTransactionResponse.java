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
public class StockTransactionResponse {
    private String id;
    private String stockItemId;
    private String stockItemName;  // ✅ Add this field
    private String transactionType;
    private BigDecimal quantity;
    private String unit;           // ✅ Add this field
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal stockBefore;
    private BigDecimal stockAfter;
    private LocalDate transactionDate;
    private String referenceNumber;
    private String referenceType;
    private String referenceId;
    private String remarks;
    private String enteredBy;
    private LocalDateTime createdAt;
}