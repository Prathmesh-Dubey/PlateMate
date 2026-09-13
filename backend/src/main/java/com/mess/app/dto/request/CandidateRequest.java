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
public class CandidateRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String address;
    private LocalDate joiningDate;
    private LocalDate leavingDate;
    private BigDecimal monthlyRate;
    private String status;
    private String emergencyContact;
    private String emergencyPhone;
    private String notes;
    /** Optional. When omitted on update the existing picture is kept; empty string clears it. */
    private String profileImageUrl;
}
