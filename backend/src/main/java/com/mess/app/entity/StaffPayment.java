package com.mess.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMonth; // JANUARY_2024, FEBRUARY_2024, etc.

    @Column(nullable = false)
    private int paymentYear;

    @Column(nullable = false)
    private int paymentMonthNumber; // 1-12

    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal netAmount;

    private String paymentMethod; // CASH, UPI, BANK_TRANSFER, CHEQUE

    private String transactionId;
    private String referenceNumber;

    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entered_by")
    private Candidate enteredBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}