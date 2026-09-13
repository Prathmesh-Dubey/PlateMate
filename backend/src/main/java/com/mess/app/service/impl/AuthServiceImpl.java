package com.mess.app.service.impl;

import com.mess.app.dto.request.*;
import com.mess.app.dto.response.AuthResponse;
import com.mess.app.dto.response.OtpResponse;
import com.mess.app.entity.Otp;
import com.mess.app.entity.RefreshToken;
import com.mess.app.entity.User;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.OtpRepository;
import com.mess.app.repository.RefreshTokenRepository;
import com.mess.app.repository.UserRepository;
import com.mess.app.service.AuthService;
import com.mess.app.service.EmailService;
import com.mess.app.util.FileUploadUtil;
import com.mess.app.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FileUploadUtil fileUploadUtil;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int OTP_LENGTH = 6;

    @Override
    @Transactional
    public OtpResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .emailVerified(false)
                .phoneVerified(false)
                .role("USER")
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        return generateAndSendOtp(request.getEmail(), "REGISTER");
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {} for purpose: {}", request.getEmail(), request.getPurpose());

        Otp otp = otpRepository.findByEmailAndOtpCodeAndPurposeAndIsUsedFalse(
                request.getEmail(), request.getOtpCode(), request.getPurpose())
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        otpRepository.markAllAsUsed(request.getEmail(), request.getPurpose());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("REGISTER".equals(request.getPurpose())) {
            user.setEnabled(true);
            user.setEmailVerified(true);
            userRepository.save(user);
            log.info("User account verified and enabled: {}", user.getEmail());
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public void resendOtp(String email, String purpose) {
        log.info("Resending OTP for email: {} for purpose: {}", email, purpose);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if ("REGISTER".equals(purpose) && user.isEnabled()) {
            throw new RuntimeException("User is already verified");
        }

        generateAndSendOtp(email, purpose);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        User user = userRepository.findByEmailOrPhoneNumber(
                request.getEmailOrPhone(), request.getEmailOrPhone())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is not verified. Please verify your email.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in successfully: {}", user.getEmail());

        return issueTokens(user);
    }

    @Override
    public OtpResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is not verified. Please contact support.");
        }

        return generateAndSendOtp(request.getEmail(), "FORGOT_PASSWORD");
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getEmail());

        Otp otp = otpRepository.findByEmailAndOtpCodeAndPurposeAndIsUsedFalse(
                request.getEmail(), request.getOtpCode(), "FORGOT_PASSWORD")
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        otpRepository.markAllAsUsed(request.getEmail(), "FORGOT_PASSWORD");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successfully for: {}", user.getEmail());

        // Invalidate every previous session
        refreshTokenRepository.deleteByUserId(user.getId());

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        // Validate refresh token
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshTokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new RuntimeException("Refresh token has expired");
        }

        // Rotate: delete old refresh token and create a new one
        refreshTokenRepository.delete(refreshTokenEntity);
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String token) {
        log.info("Logging out user");
        try {
            String email = jwtUtil.extractEmail(token);
            userRepository.findByEmail(email)
                    .ifPresent(user -> refreshTokenRepository.deleteByUserId(user.getId()));
        } catch (Exception e) {
            // An expired / malformed token on logout is not an error for the client
            log.debug("Logout with invalid token ignored: {}", e.getMessage());
        }
    }

    @Override
    public AuthResponse getCurrentUser(String token) {
        return buildResponse(currentUser(token), null, null);
    }

    @Override
    @Transactional
    public AuthResponse updateProfile(String token, UpdateProfileRequest request) {
        User user = currentUser(token);

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            String fullName = request.getFullName().trim();
            if (fullName.length() < 2 || fullName.length() > 100) {
                throw new IllegalArgumentException("Full name must be between 2 and 100 characters");
            }
            user.setFullName(fullName);
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String phone = request.getPhoneNumber().trim();
            if (!phone.matches("^\\+?[0-9]{10,15}$")) {
                throw new IllegalArgumentException("Phone number must contain 10 to 15 digits");
            }
            if (!phone.equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(phone)) {
                throw new RuntimeException("Phone number already in use");
            }
            user.setPhoneNumber(phone);
        }

        if (request.getProfilePictureUrl() != null) {
            String value = request.getProfilePictureUrl().trim();
            String previous = user.getProfilePictureUrl();
            if (value.isEmpty()) {
                user.setProfilePictureUrl(null);
            } else {
                if (!FileUploadUtil.isValidImageReference(value)) {
                    throw new IllegalArgumentException(
                            "Profile picture must be a valid http(s) image URL");
                }
                user.setProfilePictureUrl(value);
            }
            if (previous != null && !previous.equals(user.getProfilePictureUrl())) {
                fileUploadUtil.deleteStoredImageQuietly(previous);
            }
        }

        userRepository.save(user);
        log.info("Profile updated for user: {}", user.getEmail());

        return buildResponse(user, null, null);
    }

    @Override
    @Transactional
    public void changePassword(String token, ChangePasswordRequest request) {
        User user = currentUser(token);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (request.getConfirmPassword() != null
                && !request.getConfirmPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    // ==================== PROFILE PICTURE ====================

    @Override
    @Transactional
    public AuthResponse uploadProfilePicture(String token, MultipartFile file) {
        User user = currentUser(token);

        String previous = user.getProfilePictureUrl();
        String imageUrl;
        try {
            imageUrl = fileUploadUtil.uploadProfileImage(file, "user", user.getId().toString());
        } catch (IOException e) {
            log.error("Failed to store profile image for {}", user.getEmail(), e);
            throw new RuntimeException("Failed to store profile image: " + e.getMessage());
        }

        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);
        fileUploadUtil.deleteStoredImageQuietly(previous);

        log.info("Profile picture updated for user: {}", user.getEmail());
        return buildResponse(user, null, null);
    }

    @Override
    @Transactional
    public AuthResponse removeProfilePicture(String token) {
        User user = currentUser(token);
        String previous = user.getProfilePictureUrl();
        user.setProfilePictureUrl(null);
        userRepository.save(user);
        fileUploadUtil.deleteStoredImageQuietly(previous);
        log.info("Profile picture removed for user: {}", user.getEmail());
        return buildResponse(user, null, null);
    }

    // ==================== HELPER METHODS ====================

    private User currentUser(String token) {
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /** Creates a new access + refresh token pair for the user and persists the refresh token. */
    private AuthResponse issueTokens(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return buildResponse(user, token, refreshToken);
    }

    /** Single place that maps a User to the API response so every endpoint returns the same fields. */
    private AuthResponse buildResponse(User user, String token, String refreshToken) {
        return AuthResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .token(token)
                .refreshToken(refreshToken)
                .expiresAt(token != null ? LocalDateTime.now().plusSeconds(jwtUtil.getExpirationMillis() / 1000) : null)
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .enabled(user.isEnabled())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private OtpResponse generateAndSendOtp(String email, String purpose) {
        log.info("Generating OTP for email: {}, purpose: {}", email, purpose);

        otpRepository.deleteExpiredOtps(LocalDateTime.now());

        long recentOtps = otpRepository.countByEmailAndPurposeAndIsUsedFalseAndExpiryTimeAfter(
                email, purpose, LocalDateTime.now().minusMinutes(1));

        if (recentOtps >= 3) {
            throw new RuntimeException("Too many OTP requests. Please wait a minute.");
        }

        String otpCode = generateOtpCode();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        Otp otp = Otp.builder()
                .email(email)
                .otpCode(otpCode)
                .purpose(purpose)
                .expiryTime(expiryTime)
                .isUsed(false)
                .build();

        otpRepository.save(otp);
        log.info("OTP saved successfully: {} for email: {}", otpCode, email);

        try {
            emailService.sendOtpEmail(email, otpCode, purpose);
            log.info("OTP sent to: {} for purpose: {}", email, purpose);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
            // Continue even if email fails
        }

        return OtpResponse.builder()
                .email(email)
                .purpose(purpose)
                .message(getPurposeMessage(purpose))
                .expiresInMinutes(OTP_EXPIRY_MINUTES)
                .build();
    }

    private String getPurposeMessage(String purpose) {
        switch (purpose) {
            case "REGISTER":
                return "OTP sent to your email. Please verify to complete registration.";
            case "FORGOT_PASSWORD":
                return "OTP sent to your email. Please verify to reset your password.";
            default:
                return "OTP sent successfully";
        }
    }

    private String generateOtpCode() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
