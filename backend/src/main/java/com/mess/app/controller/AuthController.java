package com.mess.app.controller;

import com.mess.app.dto.request.*;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.AuthResponse;
import com.mess.app.dto.response.OtpResponse;
import com.mess.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    // ==================== REGISTRATION ====================

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OtpResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        OtpResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful. OTP sent to your email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<OtpResponse>> resendOtp(
            @RequestParam String email,
            @RequestParam(defaultValue = "REGISTER") String purpose) {
        authService.resendOtp(email, purpose);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP resent successfully"));
    }

    // ==================== LOGIN ====================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    // ==================== FORGOT PASSWORD ====================

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<OtpResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        OtpResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(response, "OTP sent to your email for password reset"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        AuthResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Password reset successfully"));
    }

    // ==================== TOKEN MANAGEMENT ====================

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestParam String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        authService.logout(bearerToken(authHeader));
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    // ==================== USER PROFILE ====================

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        AuthResponse response = authService.getCurrentUser(bearerToken(authHeader));
        return ResponseEntity.ok(ApiResponse.success(response, "User profile retrieved successfully"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        AuthResponse response = authService.updateProfile(bearerToken(authHeader), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    /** Upload a profile picture from the local device (multipart field "file"). */
    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> uploadProfileImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        AuthResponse response = authService.uploadProfilePicture(bearerToken(authHeader), file);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile picture updated successfully"));
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<AuthResponse>> removeProfileImage(
            @RequestHeader("Authorization") String authHeader) {
        AuthResponse response = authService.removeProfilePicture(bearerToken(authHeader));
        return ResponseEntity.ok(ApiResponse.success(response, "Profile picture removed successfully"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(bearerToken(authHeader), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    private static String bearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new io.jsonwebtoken.JwtException("Missing or malformed Authorization header");
        }
        return authHeader.substring(7).trim();
    }
}
