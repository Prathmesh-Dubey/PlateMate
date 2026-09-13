package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendanceResponse {
    private String id;
    private String candidateId;
    private String candidateName;
    private LocalDate attendanceDate;
    private String status;
    private LocalTime inTime;
    private LocalTime outTime;
    private boolean isPresent;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}