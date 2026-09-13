package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {
    private String id;
    private LocalDate menuDate;
    private String mealType;
    private String menuName;
    private String description;
    private boolean isActive;
    private boolean isSpecial;
    private String specialRemarks;
    private String createdBy;
    private List<MenuItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemResponse {
        private String id;
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
        private LocalDateTime createdAt;
    }
}