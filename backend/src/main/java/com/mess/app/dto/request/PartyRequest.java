package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyRequest {
    private String partyName;
    private String contactPerson;
    private String phoneNumber;
    private String email;
    private String address;
    private LocalDate eventDate;
    private String eventTime;
    private String partyType; // BIRTHDAY, FAREWELL, FESTIVAL, CORPORATE, OTHER
    private Integer numberOfPeople;
    private String mealType; // VEG, NON_VEG, BOTH
    private BigDecimal thaliRate;
    private BigDecimal discount;
    private String discountType; // PERCENTAGE, FIXED
    private String paymentStatus; // PAID, PENDING, PARTIAL
    private BigDecimal paidAmount;
    private String paymentMethod;
    private String transactionId;
    private String specialRequests;
    private String remarks;
    private String enteredByCandidateId;
    private List<ExtraItemRequest> extraItems;

    // ==================== DEPARTMENT (NEW) ====================
    private String departmentId; // optional: Department UUID, if linking to the master list
    private String departmentCode; // optional: Department code, e.g. "CSE"
    private String departmentName; // simple free-text department name typed on the booking
    private String additionalDepartments; // free-text for joint events, e.g. "ECE, MECH"

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraItemRequest {
        private String itemName;
        private Integer quantity;
        private BigDecimal rate;
        private String unit;
    }
}