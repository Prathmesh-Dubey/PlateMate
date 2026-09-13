package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkFeeCollectionRequest {
    private LocalDate collectionDate;
    private String paymentMonth;
    private int paymentYear;
    private int paymentMonthNumber;
    private String paymentMethod;
    private List<CandidateFee> candidateFees;
    private String collectedByCandidateId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateFee {
        private String candidateId;
        private BigDecimal amount;
        private String remarks;
    }
}