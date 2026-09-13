package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeBillingSummaryResponse {

    private BigDecimal totalBillableAmount;
    private BigDecimal totalAmountReceived;
    private BigDecimal totalOutstandingBalance;
    private long totalRecords;
    private long paidCount;
    private long partiallyPaidCount;
    private long unpaidCount;
}
