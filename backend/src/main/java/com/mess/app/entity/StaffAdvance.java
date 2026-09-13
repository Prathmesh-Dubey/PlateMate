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
@Table(name = "staff_advances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAdvance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false)
    private LocalDate advanceDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    private AdvanceStatus status; // PENDING, APPROVED, REJECTED, REPAID

    private LocalDate repaymentDate;
    private BigDecimal repaymentAmount;
    private BigDecimal remainingAmount;

    private int installmentMonths;
    private BigDecimal monthlyDeduction;

    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private Candidate approvedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entered_by")
    private Candidate enteredBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum AdvanceStatus {
        PENDING, APPROVED, REJECTED, REPAID
    }
}