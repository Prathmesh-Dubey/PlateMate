package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private String id;
    private String staffId;
    private String fullName;
    private String position;
    private String phoneNumber;
    private String email;
    private String address;
    private LocalDate joiningDate;
    private LocalDate leavingDate;
    private BigDecimal baseSalary;
    private String employmentType;
    private String status;
    private String emergencyContact;
    private String emergencyPhone;
    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;
    private String panNumber;
    private String aadharNumber;
    private String notes;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalPaid;
    private BigDecimal outstandingAdvance;
}