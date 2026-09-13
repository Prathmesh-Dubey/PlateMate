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
public class PartySummaryResponse {
    private int totalParties;
    private BigDecimal totalBilling;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;
    private BigDecimal totalOutstanding;
    private Map<String, Integer> mealTypeWiseCount;
    private Map<String, BigDecimal> mealTypeWiseRevenue;
    private List<PartyResponse> recentParties;
}