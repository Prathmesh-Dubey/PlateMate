package com.mess.app.service;

import com.mess.app.dto.request.*;
import com.mess.app.dto.response.AuthResponse;
import com.mess.app.dto.response.OtpResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    // Registration
    OtpResponse register(RegisterRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    void resendOtp(String email, String purpose);

    // Login
    AuthResponse login(LoginRequest request);

    // Forgot Password
    OtpResponse forgotPassword(ForgotPasswordRequest request);
    AuthResponse resetPassword(ResetPasswordRequest request);

    // Token Management
    AuthResponse refreshToken(String refreshToken);
    void logout(String token);

    // User Management
    AuthResponse getCurrentUser(String token);
    AuthResponse updateProfile(String token, UpdateProfileRequest request);
    void changePassword(String token, ChangePasswordRequest request);

    // Profile picture (local upload). URL based pictures go through updateProfile().
    AuthResponse uploadProfilePicture(String token, MultipartFile file);
    AuthResponse removeProfilePicture(String token);
}
