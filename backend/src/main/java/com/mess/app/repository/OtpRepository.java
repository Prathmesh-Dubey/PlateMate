package com.mess.app.repository;

import com.mess.app.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    
    Optional<Otp> findByEmailAndOtpCodeAndPurposeAndIsUsedFalse(
            @Param("email") String email,
            @Param("otpCode") String otpCode,
            @Param("purpose") String purpose
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE Otp o SET o.isUsed = true WHERE o.email = :email AND o.purpose = :purpose")
    void markAllAsUsed(@Param("email") String email, @Param("purpose") String purpose);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Otp o WHERE o.expiryTime < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
    
    long countByEmailAndPurposeAndIsUsedFalseAndExpiryTimeAfter(
            @Param("email") String email,
            @Param("purpose") String purpose,
            @Param("time") LocalDateTime time
    );
}