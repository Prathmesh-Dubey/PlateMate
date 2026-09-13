package com.mess.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String token;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean enabled;
    /** Persistent profile picture reference (uploaded file path or external URL). */
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
