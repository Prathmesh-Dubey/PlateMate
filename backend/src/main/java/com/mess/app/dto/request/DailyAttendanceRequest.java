package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendanceRequest {
    private String candidateId;
    private LocalDate attendanceDate;
    private String status; // IN, OUT, HALF_DAY, LEAVE
    private LocalTime inTime;
    private LocalTime outTime;
    private String remarks;
}