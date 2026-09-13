package com.mess.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "low_stock_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Column(nullable = false)
    private String alertMessage;

    @Column(nullable = false)
    private boolean isResolved = false;

    private LocalDateTime resolvedAt;

    private String resolvedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}