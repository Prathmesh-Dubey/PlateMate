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
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private String unit; // kg, gm, liter, ml, piece, dozen, etc.

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String category; // Grocery, Vegetables, Meat, Dairy, Oil, Spices, etc.

    private String subCategory;

    private String vendorName;
    private String vendorPhone;
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate expenseDate;

    private String paymentMethod; // CASH, UPI, CARD, CREDIT

    private String paymentStatus; // PAID, PENDING, PARTIAL

    private String attachmentUrl; // URL or relative path to uploaded receipt/invoice image or PDF
    private String attachmentName; // Original file name of uploaded receipt/invoice

    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entered_by")
    private Candidate enteredBy; // Changed from User to Candidate

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}