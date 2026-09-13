package com.mess.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeBillingRequest {

    @NotBlank(message = "College name is required")
    @Size(max = 255, message = "College name must not exceed 255 characters")
    private String college;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    @Max(value = 2100, message = "Year must be at most 2100")
    private Integer year;

    private java.time.LocalDate billingDate;

    @Min(value = 0, message = "Total student attendance cannot be negative")
    private Integer totalStudentAttendance;

    @Min(value = 0, message = "Total thalis served cannot be negative")
    private Integer totalThalisServed;

    @DecimalMin(value = "0.0", inclusive = true, message = "Rate per thali cannot be negative")
    private BigDecimal ratePerThali;

    @NotNull(message = "Total billable amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total billable amount cannot be negative")
    private BigDecimal totalBillableAmount;

    @NotNull(message = "Amount received is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount received cannot be negative")
    private BigDecimal amountReceived;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;
}
