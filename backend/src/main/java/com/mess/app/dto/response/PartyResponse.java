package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyResponse {

    // ==================== IDENTITY ====================
    private String id;
    private String partyId; // PRT01, PRT02, ...
    private String invoiceNumber;

    // ==================== PARTY / CLIENT ====================
    private String partyName;
    private String contactPerson;
    private String phoneNumber;
    private String email;
    private String address;

    // ==================== EVENT ====================
    private LocalDate eventDate;
    private String eventTime;
    private String partyType;
    private Integer numberOfPeople;
    private String mealType; // VEG, NON_VEG, BOTH

    // ==================== BILLING ====================
    private BigDecimal thaliRate;
    private BigDecimal totalThaliAmount;
    private BigDecimal extraItemsAmount;
    private BigDecimal discount;
    private String discountType; // PERCENTAGE, FIXED
    private BigDecimal totalBill;

    // ==================== PAYMENT ====================
    private String paymentStatus; // PAID, PENDING, PARTIAL
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String paymentMethod;
    private String transactionId;

    // ==================== NOTES ====================
    private String specialRequests;
    private String remarks;

    // ==================== EXTRA ITEMS ====================
    private List<ExtraItemResponse> extraItems;

    // ==================== ENTERED BY ====================
    private String enteredByCandidateId;
    private String enteredByName;

    // ==================== DEPARTMENT ====================
    private String departmentId; // Department UUID
    private String departmentCode; // denormalized code, e.g. "CSE"
    private String departmentName; // resolved name, e.g. "Computer Science & Engineering"
    private String additionalDepartments; // free-text, e.g. "ECE, MECH"

    // ==================== AUDIT ====================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== NESTED DTO ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraItemResponse {
        private String id;
        private String itemName;
        private Integer quantity;
        private BigDecimal rate;
        private BigDecimal totalAmount; // quantity * rate
        private String unit;
    }
}