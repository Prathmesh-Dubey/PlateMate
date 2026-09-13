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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String itemName;

    @Column(nullable = false)
    private String category; // Grocery, Vegetables, Meat, Dairy, Oil, Spices, etc.

    private String subCategory;

    @Column(nullable = false)
    private String unit; // kg, gm, liter, ml, piece, dozen, etc.

    @Column(nullable = false)
    private BigDecimal currentStock; // Available stock

    @Column(nullable = false)
    private BigDecimal minimumStockLevel; // Alert when stock goes below this

    @Column(nullable = false)
    private BigDecimal maximumStockLevel; // Maximum stock to maintain

    private BigDecimal unitPrice; // Current unit price

    @Column(nullable = false)
    private boolean active = true;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER) 
    @CollectionTable(name = "stock_item_images", joinColumns = @JoinColumn(name = "stock_item_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    private String primaryImageUrl; // Main image

    private String barcode;
    private String sku; // Stock Keeping Unit

    private String supplierName;
    private String supplierContact;

    private String remarks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entered_by")
    private Candidate enteredBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}