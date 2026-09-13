package com.mess.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceRequest {
    private LocalDate attendanceDate;
    private List<String> presentCandidateIds; // List of candidate IDs marked as present
    private List<String> absentCandidateIds; // List of candidate IDs marked as absent
}