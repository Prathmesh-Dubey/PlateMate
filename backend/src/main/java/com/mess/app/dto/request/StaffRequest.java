package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffRequest {
    private String fullName;
    private String position;
    private String phoneNumber;
    private String email;
    private String address;
    private LocalDate joiningDate;
    private LocalDate leavingDate;
    private BigDecimal baseSalary;
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT
    private String status; // ACTIVE, LEFT, ON_LEAVE
    private String emergencyContact;
    private String emergencyPhone;
    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;
    private String panNumber;
    private String aadharNumber;
    private String notes;
    private List<MultipartFile> images;
    private String primaryImageIndex;
    private String enteredByCandidateId;
}