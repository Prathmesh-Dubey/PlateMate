package com.mess.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 10)
    private String partyId; // PRT01, PRT02, etc.

    @Column(nullable = false)
    private String partyName;

    private String contactPerson;
    private String phoneNumber;
    private String email;
    private String address;

    @Column(nullable = false)
    private LocalDate eventDate;

    private String eventTime; // e.g. "18:30"

    private String partyType; // BIRTHDAY, FAREWELL, FESTIVAL, CORPORATE, OTHER

    @Column(nullable = false)
    private Integer numberOfPeople;

    @Column(nullable = false)
    private String mealType; // VEG, NON_VEG, BOTH

    @Column(nullable = false)
    private BigDecimal thaliRate;

    @Column(nullable = false)
    private BigDecimal totalThaliAmount;

    private String extraItems; // JSON or comma separated
    private BigDecimal extraItemsAmount;

    private BigDecimal discount;
    private String discountType; // PERCENTAGE, FIXED

    @Column(nullable = false)
    private BigDecimal totalBill;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // PAID, PENDING, PARTIAL

    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;

    private String paymentMethod;
    private String transactionId;
    private String invoiceNumber;

    private String specialRequests;
    private String remarks;

    // ==================== DEPARTMENT (plain columns, no FK) ====================

    @Column(name = "department_id", length = 36)
    private String departmentId; // UUID of Department (nullable = General / Not specified)

    @Column(name = "department_code", length = 20)
    private String departmentCode; // denormalized code, e.g. CSE (for fast filtering/reports)

    @Column(name = "department_name", length = 255)
    private String departmentName; // free-text department name typed directly on the booking

    @Column(name = "additional_departments", length = 255)
    private String additionalDepartments; // free-text for joint events, e.g. "ECE, MECH"

    // ==================== RELATIONS ====================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entered_by")
    private Candidate enteredBy;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PartyExtraItem> extraItemList = new ArrayList<>();

    // ==================== AUDIT ====================

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PAID, PENDING, PARTIAL
    }
}