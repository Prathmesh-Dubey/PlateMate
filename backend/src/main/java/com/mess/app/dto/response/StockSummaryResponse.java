package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryResponse {
    private int totalItems;
    private int activeItems;
    private int lowStockItems;
    private int overStockItems;
    private BigDecimal totalStockValue;
    private Map<String, Integer> categoryWiseCount;
    private Map<String, BigDecimal> categoryWiseValue;
    private List<StockItemResponse> lowStockList;
    private List<StockItemResponse> overStockList;
}