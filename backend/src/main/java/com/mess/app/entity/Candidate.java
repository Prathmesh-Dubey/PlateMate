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

@Entity
@Table(name = "candidates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private String id;  // ✅ CHANGE FROM UUID TO String

    @Column(unique = true, nullable = false, length = 10)
    private String candidateId;

    @Column(nullable = false)
    private String fullName;

    private String phoneNumber;
    private String email;
    private String address;

    private LocalDate joiningDate;
    private LocalDate leavingDate;

    @Column(nullable = false)
    private BigDecimal monthlyRate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String emergencyContact;
    private String emergencyPhone;

    private String notes;

    /** Profile picture: uploaded file reference (/uploads/...) or an external image URL. */
    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * ACTIVE   - dining member, counted in expected mess fees
     * INACTIVE - membership paused (renewal due), not counted in expected fees
     * LEFT     - permanently left the mess
     */
    public enum Status {
        ACTIVE, INACTIVE, LEFT
    }
}
