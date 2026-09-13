package com.mess.app.dto.response;

import com.mess.app.entity.CollegeBillingPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeBillingResponse {

    private String id;
    private String college;
    private int month;
    private int year;
    private String monthName;
    private java.time.LocalDate billingDate;
    private Integer totalStudentAttendance;
    private Integer totalThalisServed;
    private BigDecimal ratePerThali;
    private BigDecimal totalBillableAmount;
    private BigDecimal amountReceived;
    private BigDecimal outstandingBalance;
    private CollegeBillingPaymentStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
