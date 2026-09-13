package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeSummaryResponse {
    private int month;
    private int year;
    private String monthName;
    private int totalCandidates;
    private int paidCandidates;
    private int pendingCandidates;
    private BigDecimal totalExpectedFees;
    private BigDecimal totalCollectedFees;
    private BigDecimal pendingAmount;
    private BigDecimal collectionPercentage;
    private List<CandidateFeeDetail> candidateFees;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateFeeDetail {
        private String candidateId;
        private String candidateName;
        private BigDecimal monthlyRate;
        private BigDecimal paidAmount;
        private BigDecimal pendingAmount;
        private boolean isPaid;
        private String paymentDate;
    }
}