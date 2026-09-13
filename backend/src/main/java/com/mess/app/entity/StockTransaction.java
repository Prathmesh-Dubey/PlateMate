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
@Table(name = "stock_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // PURCHASE, USAGE, RETURN, ADJUSTMENT

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private BigDecimal stockBefore;

    private BigDecimal stockAfter;

    private LocalDate transactionDate;

    private String referenceNumber; // Invoice number, bill number, etc.

    private String referenceType; // EXPENSE, DIRECT_PURCHASE, etc.

    private String referenceId; // ID of related expense or purchase

    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entered_by")
    private Candidate enteredBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum TransactionType {
        PURCHASE, USAGE, RETURN, ADJUSTMENT
    }
}