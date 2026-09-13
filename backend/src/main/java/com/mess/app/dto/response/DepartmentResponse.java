package com.mess.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private String id;
    private String code;
    private String name;
    private String headName;
    private String contactEmail;
    private String contactPhone;
    private Boolean active;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}