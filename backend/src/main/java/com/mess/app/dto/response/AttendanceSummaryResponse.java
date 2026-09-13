package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {
    private LocalDate date;
    private long totalCandidates;
    private long present;
    private long absent;
    private long halfDay;
    private long onLeave;
    private double attendancePercentage;
}