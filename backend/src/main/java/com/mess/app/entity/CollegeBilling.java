package com.mess.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "college_billing", uniqueConstraints = {
        @UniqueConstraint(name = "uk_college_billing_college_month_year", columnNames = {"college", "month", "year"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 255)
    private String college;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Column(name = "billing_date")
    private java.time.LocalDate billingDate;

    @Column(name = "total_student_attendance")
    private Integer totalStudentAttendance;

    @Column(name = "total_thalis_served")
    private Integer totalThalisServed;

    @Column(name = "rate_per_thali", precision = 10, scale = 2)
    private BigDecimal ratePerThali;

    @Column(name = "total_billable_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBillableAmount;

    @Column(name = "amount_received", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountReceived;

    @Column(name = "outstanding_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CollegeBillingPaymentStatus status;

    @Column(length = 1000)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
