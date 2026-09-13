package com.mess.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "Department code is required")
    @Size(max = 20, message = "Department code must be at most 20 characters")
    private String code;

    @NotBlank(message = "Department name is required")
    @Size(max = 255, message = "Department name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Head name must be at most 255 characters")
    private String headName;

    @Size(max = 255, message = "Contact email must be at most 255 characters")
    private String contactEmail;

    @Size(max = 50, message = "Contact phone must be at most 50 characters")
    private String contactPhone;

    private Boolean active;

    @Size(max = 500, message = "Remarks must be at most 500 characters")
    private String remarks;
}