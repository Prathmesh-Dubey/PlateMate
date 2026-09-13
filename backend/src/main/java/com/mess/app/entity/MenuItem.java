package com.mess.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private String itemName;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String category; // Main, Side, Dessert, Beverage, etc.

    private boolean isVegetarian = true;
    private boolean isAvailable = true;

    private String preparationTime; // e.g., "30 mins"
    private String servingSize; // e.g., "1 plate"

    private String imageUrl;

    private Integer displayOrder = 0;

    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;
}