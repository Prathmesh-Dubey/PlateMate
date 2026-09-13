package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemRequest {
    private String itemName;
    private String category;
    private String subCategory;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal minimumStockLevel;
    private BigDecimal maximumStockLevel;
    private BigDecimal unitPrice;
    private String description;
    private String barcode;
    private String sku;
    private String supplierName;
    private String supplierContact;
    private String remarks;
    private List<MultipartFile> images;
    private String primaryImageIndex;
    private String enteredByCandidateId;
}