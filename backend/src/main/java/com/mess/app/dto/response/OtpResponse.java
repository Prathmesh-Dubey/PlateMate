package com.mess.app.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpResponse {
    private String email;
    private String purpose;
    private String message;
    private int expiresInMinutes;
}