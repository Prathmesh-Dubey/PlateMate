package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {
    private String itemName;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean isVegetarian;
    private boolean isAvailable;
    private String preparationTime;
    private String servingSize;
    private String imageUrl;
    private Integer displayOrder;
    private String remarks;
}