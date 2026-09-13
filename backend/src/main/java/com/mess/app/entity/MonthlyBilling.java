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

@Entity
@Table(name = "monthly_billing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private String monthName;

    @Column(nullable = false)
    private LocalDate monthStartDate;

    @Column(nullable = false)
    private LocalDate monthEndDate;

    // Income
    @Column(nullable = false)
    private BigDecimal messFeesIncome = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal partyIncome = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal otherIncome = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    // Expenses
    @Column(nullable = false)
    private BigDecimal stockExpenses = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal staffSalaryExpenses = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal otherExpenses = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    // Profit/Loss
    @Column(nullable = false)
    private BigDecimal profit = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal loss = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal netProfitLoss = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal profitMarginPercentage = BigDecimal.ZERO;

    // Additional Details
    private int totalCandidates;
    private int totalStaff;
    private int totalParties;

    private String remarks;

    @Column(nullable = false)
    private boolean isGenerated = false;

    @Column(nullable = false)
    private LocalDate generatedDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}