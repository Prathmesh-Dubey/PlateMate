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
public class ExpenseRequest {
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal rate;
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
    private String enteredByCandidateId; // Added this field
}