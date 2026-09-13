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
public class ExpenseResponse {
    private String id;
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal rate;
    private BigDecimal totalAmount;
    private String category;
    private String subCategory;
    private String vendorName;
    private String vendorPhone;
    private String invoiceNumber;
    private LocalDate expenseDate;
    private String paymentMethod;
    private String paymentStatus;
    private String attachmentUrl;
    private String attachmentName;
    private String remarks;
    private String enteredBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}