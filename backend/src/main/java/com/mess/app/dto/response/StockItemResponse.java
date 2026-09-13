package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemResponse {
    private String id;
    private String itemName;
    private String category;
    private String subCategory;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal minimumStockLevel;
    private BigDecimal maximumStockLevel;
    private BigDecimal unitPrice;
    private boolean active;
    private String description;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private String barcode;
    private String sku;
    private String supplierName;
    private String supplierContact;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isLowStock;
    private boolean isOverStock;
}