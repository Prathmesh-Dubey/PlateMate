package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private String id;
    private String candidateId;
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
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
