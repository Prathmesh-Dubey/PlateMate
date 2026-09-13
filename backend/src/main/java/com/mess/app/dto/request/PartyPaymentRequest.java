package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyPaymentRequest {
    private String partyId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String remarks;
    private String enteredByCandidateId;
}