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
@Table(name = "fee_collections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false)
    private LocalDate collectionDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentMonth; // JANUARY_2024, FEBRUARY_2024, etc.

    @Column(nullable = false)
    private int paymentYear;

    @Column(nullable = false)
    private int paymentMonthNumber; // 1-12

    private String paymentMethod; // CASH, UPI, BANK_TRANSFER, CHEQUE, ONLINE

    private String transactionId;
    private String receiptNumber;

    @Column(nullable = false)
    private boolean isPaid = false;

    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collected_by")
    private Candidate collectedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}