package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {
    private LocalDate menuDate;
    private String mealType; // BREAKFAST, LUNCH, DINNER
    private String menuName;
    private String description;
    private boolean isSpecial;
    private String specialRemarks;
    private String createdByCandidateId;
    private List<MenuItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemRequest {
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
}