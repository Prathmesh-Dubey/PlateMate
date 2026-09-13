package com.mess.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "staff")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 10)
    private String staffId; // STF01, STF02, etc.

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String position; // Cook, Helper, Cleaner, Manager, etc.

    private String phoneNumber;
    private String email;
    private String address;

    @Column(nullable = false)
    private LocalDate joiningDate;

    private LocalDate leavingDate;

    @Column(nullable = false)
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType; // FULL_TIME, PART_TIME, CONTRACT

    @Enumerated(EnumType.STRING)
    private StaffStatus status; // ACTIVE, LEFT, ON_LEAVE

    private String emergencyContact;
    private String emergencyPhone;

    private String bankName;
    private String bankAccountNumber;
    private String ifscCode;
    private String panNumber;
    private String aadharNumber;

    private String notes;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StaffPayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StaffAdvance> advances = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER) 
    @CollectionTable(name = "staff_images", joinColumns = @JoinColumn(name = "staff_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    private String primaryImageUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT
    }

    public enum StaffStatus {
        ACTIVE, LEFT, ON_LEAVE
    }
}